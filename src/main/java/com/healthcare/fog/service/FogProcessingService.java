package com.healthcare.fog.service;

import com.healthcare.fog.dto.AbnormalityAssessment;
import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.exception.ResourceNotFoundException;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.HealthReading;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.FogNodeRepository;
import com.healthcare.fog.repository.HealthReadingRepository;
import com.healthcare.fog.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Core Fog Computing Processing Service.
 * Performs proximate, sub-millisecond edge analytics on patient biomedical telemetry:
 * 1. Low-latency edge ingestion
 * 2. Clinical abnormality detection using AbnormalityDetectionService
 * 3. Local routine reading filtering (bandwidth conservation)
 * 4. Critical anomaly detection and immediate forwarding
 * 5. High-speed localized caching
 * 6. Dynamic Fog node workload tracking
 */
@Service
public class FogProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FogProcessingService.class);

    private final FogNodeRepository fogNodeRepository;
    private final PatientRepository patientRepository;
    private final HealthReadingRepository healthReadingRepository;
    private final AbnormalityDetectionService abnormalityDetectionService;
    private final EmergencyAlertService emergencyAlertService;
    private final FogLocalCache fogLocalCache;
    private final FogLoadBalancer fogLoadBalancer;

    public FogProcessingService(FogNodeRepository fogNodeRepository,
                                PatientRepository patientRepository,
                                HealthReadingRepository healthReadingRepository,
                                AbnormalityDetectionService abnormalityDetectionService,
                                EmergencyAlertService emergencyAlertService,
                                FogLocalCache fogLocalCache,
                                FogLoadBalancer fogLoadBalancer) {
        this.fogNodeRepository = fogNodeRepository;
        this.patientRepository = patientRepository;
        this.healthReadingRepository = healthReadingRepository;
        this.abnormalityDetectionService = abnormalityDetectionService;
        this.emergencyAlertService = emergencyAlertService;
        this.fogLocalCache = fogLocalCache;
        this.fogLoadBalancer = fogLoadBalancer;
    }

    /**
     * Process an incoming patient telemetry payload locally at the fog node.
     */
    public FogProcessingResultDto processTelemetry(TelemetryIngestRequestDto requestDto) {
        long startNano = System.nanoTime();

        // 1. Locate patient
        Patient patient = patientRepository.findByPatientId(requestDto.getPatientId())
                .or(() -> patientRepository.findById(requestDto.getPatientId()))
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + requestDto.getPatientId()));

        // 2. Determine target Fog Node (with automatic failover if assigned node is OFFLINE)
        FogNode fogNode = resolveActiveFogNode(patient, requestDto.getPreferredFogNodeId());

        // 3. Clinical Abnormality Evaluation at the Fog Node (Phase 5 AbnormalityDetectionService)
        AbnormalityAssessment assessment = abnormalityDetectionService.evaluateVitals(
                requestDto.getHeartRate(),
                requestDto.getBloodOxygenSpO2(),
                requestDto.getBodyTemperature(),
                requestDto.getSystolicBloodPressure(),
                requestDto.getDiastolicBloodPressure()
        );

        // 4. Fog Action Decision (Filtering vs Immediate Forwarding)
        String fogAction;
        boolean forwardedToCloud;
        EmergencyAlert alert = null;

        if ("CRITICAL".equals(assessment.getStatus())) {
            // High-priority immediate forwarding & local emergency alert creation
            fogAction = "CRITICAL_IMMEDIATE_FORWARD";
            forwardedToCloud = true;

            alert = emergencyAlertService.createEmergencyAlert(
                    patient,
                    fogNode,
                    requestDto.getSensorDeviceId(),
                    assessment
            );

            fogNode.setCriticalAlertsCount((fogNode.getCriticalAlertsCount() != null ? fogNode.getCriticalAlertsCount() : 0) + 1);

            log.warn("CRITICAL EMERGENCY DETECTED at Fog Node {}: Patient {} - {}",
                    fogNode.getNodeId(), patient.getPatientId(), assessment.getDiagnosticMessage());

        } else if ("WARNING".equals(assessment.getStatus())) {
            // Warning status: flagged for cloud synchronization
            fogAction = "EVALUATED_WARNING";
            forwardedToCloud = true;
        } else {
            // NORMAL routine telemetry: Filtered locally at the Fog Node!
            fogAction = "ROUTINE_FILTERED_LOCALLY";
            forwardedToCloud = false;
            fogNode.setFilteredReadingsCount((fogNode.getFilteredReadingsCount() != null ? fogNode.getFilteredReadingsCount() : 0) + 1);
            log.debug("Routine reading for patient {} filtered locally at Fog Node {}",
                    patient.getPatientId(), fogNode.getNodeId());
        }

        // 5. Calculate precise execution duration
        long endNano = System.nanoTime();
        double durationMs = (endNano - startNano) / 1_000_000.0;

        // 6. Persist HealthReading Document in MongoDB
        HealthReading reading = new HealthReading(
                patient.getPatientId(),
                fogNode.getNodeId(),
                requestDto.getSensorDeviceId(),
                requestDto.getHeartRate(),
                requestDto.getBloodOxygenSpO2(),
                requestDto.getBodyTemperature(),
                requestDto.getSystolicBloodPressure(),
                requestDto.getDiastolicBloodPressure(),
                assessment.getStatus(),
                requestDto.getNotes() != null ? requestDto.getNotes() : assessment.getDiagnosticMessage()
        );
        reading.setFogAction(fogAction);
        reading.setProcessingDurationMs(durationMs);
        reading.setForwardedToCloud(forwardedToCloud);

        HealthReading savedReading = healthReadingRepository.save(reading);

        // 7. Store in Fog Local In-Memory Cache
        fogLocalCache.cacheReading(savedReading);

        // 8. Update Fog Node Operational Statistics in MongoDB
        long processedCount = (fogNode.getProcessedReadingsCount() != null ? fogNode.getProcessedReadingsCount() : 0) + 1;
        double currentTotal = (fogNode.getTotalProcessingTimeMs() != null ? fogNode.getTotalProcessingTimeMs() : 0.0) + durationMs;
        fogNode.setProcessedReadingsCount(processedCount);
        fogNode.setTotalProcessingTimeMs(currentTotal);
        fogNode.setAverageProcessingTimeMs(currentTotal / processedCount);
        fogNode.setLastHeartbeat(Instant.now());

        // Dynamic resource telemetry based on load
        int workload = (fogNode.getCurrentWorkload() != null) ? fogNode.getCurrentWorkload() : 1;
        fogNode.setCpuUsagePercent(Math.min(95.0, 5.0 + (workload * 1.5) + (Math.random() * 2.0)));
        fogNode.setMemoryUsagePercent(Math.min(92.0, 15.0 + (workload * 1.2)));

        fogNodeRepository.save(fogNode);

        // 9. Construct Response DTO
        FogProcessingResultDto result = new FogProcessingResultDto();
        result.setReadingId(savedReading.getId());
        result.setPatientId(patient.getPatientId());
        result.setPatientName(patient.getName());
        result.setFogNodeId(fogNode.getNodeId());
        result.setFogNodeName(fogNode.getName());
        result.setFogNodeType(fogNode.getNodeType());
        result.setHealthStatus(assessment.getStatus());
        result.setFogAction(fogAction);
        result.setForwardedToCloud(forwardedToCloud);
        result.setCachedInFog(true);
        result.setProcessingTimeMs(durationMs);
        result.setGeneratedAlert(alert);
        result.setNodeStatus(fogNode.getStatus());
        result.setNodeCurrentWorkload(fogNode.getCurrentWorkload() != null ? fogNode.getCurrentWorkload() : 0);
        result.setNodeMaxCapacity(fogNode.getMaxCapacity() != null ? fogNode.getMaxCapacity() : 50);

        Map<String, Object> vitalsMap = new LinkedHashMap<>();
        vitalsMap.put("heartRate", requestDto.getHeartRate());
        vitalsMap.put("bloodOxygenSpO2", requestDto.getBloodOxygenSpO2());
        vitalsMap.put("bodyTemperature", requestDto.getBodyTemperature());
        vitalsMap.put("systolicBloodPressure", requestDto.getSystolicBloodPressure());
        vitalsMap.put("diastolicBloodPressure", requestDto.getDiastolicBloodPressure());
        result.setVitals(vitalsMap);

        return result;
    }

    /**
     * Resolves the target Fog Node, initiating automatic failover if the assigned node is OFFLINE.
     */
    private FogNode resolveActiveFogNode(Patient patient, String preferredNodeId) {
        String targetNodeId = preferredNodeId;
        if (targetNodeId == null || targetNodeId.isBlank()) {
            targetNodeId = patient.getAssignedFogNodeId();
        }

        FogNode node = null;
        if (targetNodeId != null && !targetNodeId.isBlank()) {
            node = fogNodeRepository.findByNodeId(targetNodeId).orElse(null);
        }

        // Automatic Node Failover: If no node assigned or current node is OFFLINE
        if (node == null || !"ONLINE".equalsIgnoreCase(node.getStatus())) {
            log.warn("Assigned Fog Node '{}' is inactive or unassigned for patient {}. Executing dynamic failover...",
                    targetNodeId, patient.getPatientId());
            node = fogLoadBalancer.selectBestNode(patient);
            fogLoadBalancer.assignPatientToNode(patient.getPatientId(), node.getNodeId());
            log.info("Patient {} automatically reassigned to healthy Fog Node '{}'",
                    patient.getPatientId(), node.getNodeId());
        }

        return node;
    }
}
