package com.healthcare.fog.service;

import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.exception.ResourceNotFoundException;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.model.SensorDevice;
import com.healthcare.fog.repository.PatientRepository;
import com.healthcare.fog.repository.SensorDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Healthcare IoT Sensor Simulation Service.
 * Simulates biomedical sensor devices attached to registered patients,
 * generating telemetry streams at configurable intervals and routing every
 * reading strictly through the Fog Computing Layer (FogProcessingService).
 *
 * Processing Flow:
 * IoT Sensor -> Fog Node -> Local Processing -> Cloud Forwarding Decision -> MongoDB Storage.
 */
@Service
public class SensorSimulationService {

    private static final Logger log = LoggerFactory.getLogger(SensorSimulationService.class);

    private final PatientRepository patientRepository;
    private final SensorDeviceRepository sensorDeviceRepository;
    private final FogProcessingService fogProcessingService;
    private final ThreadPoolTaskScheduler taskScheduler;

    // Simulation Operational State
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong intervalMs = new AtomicLong(3000);
    private final AtomicReference<String> simulationMode = new AtomicReference<>("MIXED");
    private final AtomicReference<Instant> startedAt = new AtomicReference<>(null);

    // Telemetry Statistics
    private final AtomicLong totalReadingsGenerated = new AtomicLong(0);
    private final AtomicLong routineFilteredCount = new AtomicLong(0);
    private final AtomicLong warningsEvaluatedCount = new AtomicLong(0);
    private final AtomicLong criticalAlertsGenerated = new AtomicLong(0);

    // In-memory buffer of recent simulation outputs
    private final Deque<FogProcessingResultDto> recentSimulationResults = new ConcurrentLinkedDeque<>();
    private static final int MAX_HISTORY_SIZE = 50;

    private ScheduledFuture<?> scheduledTask;
    private final Random random = new Random();

    public SensorSimulationService(PatientRepository patientRepository,
                                   SensorDeviceRepository sensorDeviceRepository,
                                   FogProcessingService fogProcessingService,
                                   ThreadPoolTaskScheduler taskScheduler) {
        this.patientRepository = patientRepository;
        this.sensorDeviceRepository = sensorDeviceRepository;
        this.fogProcessingService = fogProcessingService;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Start the automated periodic healthcare sensor simulation.
     */
    public synchronized Map<String, Object> startSimulation(Long interval, String mode) {
        if (interval != null && interval >= 500) {
            this.intervalMs.set(interval);
        }
        if (mode != null && !mode.isBlank()) {
            this.simulationMode.set(mode.trim().toUpperCase());
        }

        if (isRunning.get() && scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }

        isRunning.set(true);
        startedAt.set(Instant.now());

        scheduledTask = taskScheduler.scheduleWithFixedDelay(
                this::runSimulationTick,
                Duration.ofMillis(intervalMs.get())
        );

        log.info("Started Healthcare IoT Sensor Simulation: interval = {} ms, mode = {}",
                intervalMs.get(), simulationMode.get());

        return getSimulationStatus();
    }

    /**
     * Stop the automated healthcare sensor simulation.
     */
    public synchronized Map<String, Object> stopSimulation() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        isRunning.set(false);
        log.info("Stopped Healthcare IoT Sensor Simulation.");
        return getSimulationStatus();
    }

    /**
     * Periodic scheduled tick executed by the scheduler.
     * Iterates through active registered patients, simulates sensor data,
     * and streams it to the Fog Node.
     */
    public void runSimulationTick() {
        if (!isRunning.get()) return;

        try {
            List<Patient> patients = patientRepository.findAll();
            if (patients.isEmpty()) {
                log.debug("No patients registered for sensor simulation.");
                return;
            }

            // Pick a rotating patient or simulate for a small batch per tick
            int index = (int) (totalReadingsGenerated.get() % patients.size());
            Patient targetPatient = patients.get(index);

            generateAndProcessReading(targetPatient, simulationMode.get());
        } catch (Exception ex) {
            log.error("Error during sensor simulation tick: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Generate a single reading for a specific patient in a specified clinical scenario.
     */
    public FogProcessingResultDto generateSingleReading(String patientIdentifier, String scenario) {
        Patient patient = patientRepository.findByPatientId(patientIdentifier)
                .or(() -> patientRepository.findById(patientIdentifier))
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientIdentifier));

        String effectiveScenario = (scenario != null && !scenario.isBlank()) 
                ? scenario.trim().toUpperCase() 
                : simulationMode.get();

        return generateAndProcessReading(patient, effectiveScenario);
    }

    /**
     * Submit a manual health reading through the Fog Computing pipeline.
     */
    public FogProcessingResultDto submitManualReading(TelemetryIngestRequestDto requestDto) {
        log.info("Manual health reading submitted for patient {}", requestDto.getPatientId());
        
        // Execute through FogProcessingService (Never bypass the fog layer)
        FogProcessingResultDto result = fogProcessingService.processTelemetry(requestDto);
        
        // Update simulation telemetry statistics
        recordSimulationResult(result);
        return result;
    }

    /**
     * Internal generator: synthesizes realistic physiological parameters and routes to Fog layer.
     */
    private FogProcessingResultDto generateAndProcessReading(Patient patient, String mode) {
        String scenario = resolveScenario(mode);
        TelemetryIngestRequestDto telemetry = createSyntheticTelemetry(patient, scenario);

        // Core Fog Requirement: Route through Fog Node local processing
        FogProcessingResultDto result = fogProcessingService.processTelemetry(telemetry);

        recordSimulationResult(result);
        return result;
    }

    private void recordSimulationResult(FogProcessingResultDto result) {
        totalReadingsGenerated.incrementAndGet();

        if ("CRITICAL_IMMEDIATE_FORWARD".equalsIgnoreCase(result.getFogAction())) {
            criticalAlertsGenerated.incrementAndGet();
        } else if ("EVALUATED_WARNING".equalsIgnoreCase(result.getFogAction())) {
            warningsEvaluatedCount.incrementAndGet();
        } else {
            routineFilteredCount.incrementAndGet();
        }

        recentSimulationResults.addFirst(result);
        while (recentSimulationResults.size() > MAX_HISTORY_SIZE) {
            recentSimulationResults.pollLast();
        }
    }

    /**
     * Resolves scenario mode (NORMAL, WARNING, CRITICAL, or MIXED distribution).
     */
    private String resolveScenario(String mode) {
        if ("NORMAL".equalsIgnoreCase(mode)) return "NORMAL";
        if ("WARNING".equalsIgnoreCase(mode)) return "WARNING";
        if ("CRITICAL".equalsIgnoreCase(mode)) return "CRITICAL";

        // MIXED: 70% Normal, 20% Warning, 10% Critical
        int roll = random.nextInt(100);
        if (roll < 70) return "NORMAL";
        if (roll < 90) return "WARNING";
        return "CRITICAL";
    }

    /**
     * Synthesizes clinically realistic biomedical vital readings based on scenario.
     */
    private TelemetryIngestRequestDto createSyntheticTelemetry(Patient patient, String scenario) {
        double hr, spo2, temp, sysBp, diaBp;
        String notes;

        String sensorDeviceId = resolveSensorDeviceId(patient.getPatientId());

        switch (scenario) {
            case "CRITICAL":
                // Random critical anomaly
                int criticalType = random.nextInt(3);
                if (criticalType == 0) {
                    // Hypoxia crisis
                    hr = round1(120.0 + random.nextDouble() * 25.0); // 120 - 145 BPM
                    spo2 = round1(82.0 + random.nextDouble() * 6.5);  // 82.0 - 88.5%
                    temp = round1(37.0 + random.nextDouble() * 0.8);
                    sysBp = round1(140.0 + random.nextDouble() * 20.0);
                    diaBp = round1(90.0 + random.nextDouble() * 12.0);
                    notes = "SIMULATED: Acute Respiratory Distress / Severe Hypoxia";
                } else if (criticalType == 1) {
                    // Hypertensive crisis
                    hr = round1(135.0 + random.nextDouble() * 20.0); // Tachycardia
                    spo2 = round1(94.0 + random.nextDouble() * 3.0);
                    temp = round1(36.8 + random.nextDouble() * 0.5);
                    sysBp = round1(160.0 + random.nextDouble() * 25.0); // 160 - 185 mmHg
                    diaBp = round1(102.0 + random.nextDouble() * 15.0); // 102 - 117 mmHg
                    notes = "SIMULATED: Hypertensive Emergency Spike";
                } else {
                    // Severe Hyperthermia / High Fever
                    hr = round1(115.0 + random.nextDouble() * 20.0);
                    spo2 = round1(93.0 + random.nextDouble() * 4.0);
                    temp = round1(39.4 + random.nextDouble() * 1.0);  // 39.4 - 40.4 °C
                    sysBp = round1(130.0 + random.nextDouble() * 15.0);
                    diaBp = round1(85.0 + random.nextDouble() * 10.0);
                    notes = "SIMULATED: Severe Febrile Hyperthermia Episode";
                }
                break;

            case "WARNING":
                // Moderate clinical deviations
                int warnType = random.nextInt(2);
                if (warnType == 0) {
                    hr = round1(102.0 + random.nextDouble() * 15.0); // Mild tachycardia
                    spo2 = round1(91.0 + random.nextDouble() * 3.5);  // 91 - 94.5%
                    temp = round1(37.6 + random.nextDouble() * 0.6);
                    sysBp = round1(126.0 + random.nextDouble() * 10.0);
                    diaBp = round1(84.0 + random.nextDouble() * 6.0);
                    notes = "SIMULATED: Mild Hypoxia & Borderline Tachycardia";
                } else {
                    hr = round1(75.0 + random.nextDouble() * 15.0);
                    spo2 = round1(95.0 + random.nextDouble() * 2.0);
                    temp = round1(36.7 + random.nextDouble() * 0.4);
                    sysBp = round1(130.0 + random.nextDouble() * 12.0); // Pre-hypertension
                    diaBp = round1(86.0 + random.nextDouble() * 6.0);
                    notes = "SIMULATED: Stage 1 Pre-Hypertension Telemetry";
                }
                break;

            case "NORMAL":
            default:
                // Routine healthy physiological parameters
                hr = round1(65.0 + random.nextDouble() * 22.0);       // 65 - 87 BPM
                spo2 = round1(96.0 + random.nextDouble() * 3.5);     // 96.0 - 99.5%
                temp = round1(36.5 + random.nextDouble() * 0.7);     // 36.5 - 37.2 °C
                sysBp = round1(112.0 + random.nextDouble() * 10.0);  // 112 - 122 mmHg
                diaBp = round1(72.0 + random.nextDouble() * 8.0);    // 72 - 80 mmHg
                notes = "SIMULATED: Routine Physiological Vital Signs";
                break;
        }

        TelemetryIngestRequestDto dto = new TelemetryIngestRequestDto();
        dto.setPatientId(patient.getPatientId());
        dto.setSensorDeviceId(sensorDeviceId);
        dto.setHeartRate(hr);
        dto.setBloodOxygenSpO2(spo2);
        dto.setBodyTemperature(temp);
        dto.setSystolicBloodPressure(sysBp);
        dto.setDiastolicBloodPressure(diaBp);
        dto.setPreferredFogNodeId(patient.getAssignedFogNodeId());
        dto.setNotes(notes);

        return dto;
    }

    private String resolveSensorDeviceId(String patientId) {
        return sensorDeviceRepository.findByAssignedPatientId(patientId).stream()
                .findFirst()
                .map(SensorDevice::getDeviceId)
                .orElse("SENS-BIO-" + patientId);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Retrieve the current status of the sensor simulation engine.
     */
    public Map<String, Object> getSimulationStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("timestamp", Instant.now().toString());
        status.put("isRunning", isRunning.get());
        status.put("running", isRunning.get());
        status.put("intervalMs", intervalMs.get());
        status.put("simulationMode", simulationMode.get());
        status.put("startedAt", startedAt.get() != null ? startedAt.get().toString() : null);
        status.put("totalReadingsGenerated", totalReadingsGenerated.get());
        status.put("routineFilteredLocallyCount", routineFilteredCount.get());
        status.put("warningsEvaluatedCount", warningsEvaluatedCount.get());
        status.put("criticalAlertsGeneratedCount", criticalAlertsGenerated.get());
        status.put("totalRegisteredPatients", patientRepository.count());
        status.put("totalConfiguredSensorDevices", sensorDeviceRepository.count());
        status.put("cachedHistoryCount", recentSimulationResults.size());
        return status;
    }

    /**
     * Retrieve recent simulation output history.
     */
    public List<FogProcessingResultDto> getRecentSimulationHistory(int limit) {
        List<FogProcessingResultDto> list = new ArrayList<>(recentSimulationResults);
        if (limit > 0 && limit < list.size()) {
            return list.subList(0, limit);
        }
        return list;
    }

    public void resetStatistics() {
        totalReadingsGenerated.set(0);
        routineFilteredCount.set(0);
        warningsEvaluatedCount.set(0);
        criticalAlertsGenerated.set(0);
        recentSimulationResults.clear();
    }
}
