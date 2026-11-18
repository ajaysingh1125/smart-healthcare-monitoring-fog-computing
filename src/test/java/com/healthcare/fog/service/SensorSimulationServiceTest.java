package com.healthcare.fog.service;

import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.PatientRepository;
import com.healthcare.fog.repository.SensorDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorSimulationServiceTest {

    private PatientRepository patientRepository;
    private SensorDeviceRepository sensorDeviceRepository;
    private FogProcessingService fogProcessingService;
    private ThreadPoolTaskScheduler taskScheduler;
    private SensorSimulationService simulationService;

    @BeforeEach
    void setUp() {
        patientRepository = Mockito.mock(PatientRepository.class);
        sensorDeviceRepository = Mockito.mock(SensorDeviceRepository.class);
        fogProcessingService = Mockito.mock(FogProcessingService.class);
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();

        simulationService = new SensorSimulationService(
                patientRepository, sensorDeviceRepository, fogProcessingService, taskScheduler
        );
    }

    @Test
    @DisplayName("startSimulation and stopSimulation should update running status")
    void testStartAndStopSimulation() {
        Map<String, Object> startStatus = simulationService.startSimulation(2000L, "NORMAL");
        assertThat(startStatus.get("isRunning")).isEqualTo(true);
        assertThat(startStatus.get("intervalMs")).isEqualTo(2000L);
        assertThat(startStatus.get("simulationMode")).isEqualTo("NORMAL");

        Map<String, Object> stopStatus = simulationService.stopSimulation();
        assertThat(stopStatus.get("isRunning")).isEqualTo(false);
    }

    @Test
    @DisplayName("generateSingleReading should construct telemetry and invoke FogProcessingService")
    void testGenerateSingleReading_NormalScenario() {
        Patient patient = new Patient();
        patient.setPatientId("PAT-1001");
        patient.setName("Eleanor Vance");
        patient.setAssignedFogNodeId("FOG-HOSPITAL-01");

        when(patientRepository.findByPatientId("PAT-1001")).thenReturn(Optional.of(patient));
        when(sensorDeviceRepository.findByAssignedPatientId("PAT-1001")).thenReturn(Collections.emptyList());

        FogProcessingResultDto mockResult = new FogProcessingResultDto();
        mockResult.setPatientId("PAT-1001");
        mockResult.setHealthStatus("NORMAL");
        mockResult.setFogAction("ROUTINE_FILTERED_LOCALLY");

        when(fogProcessingService.processTelemetry(any(TelemetryIngestRequestDto.class))).thenReturn(mockResult);

        FogProcessingResultDto result = simulationService.generateSingleReading("PAT-1001", "NORMAL");

        assertThat(result.getPatientId()).isEqualTo("PAT-1001");
        assertThat(result.getHealthStatus()).isEqualTo("NORMAL");
        verify(fogProcessingService).processTelemetry(any(TelemetryIngestRequestDto.class));
    }

    @Test
    @DisplayName("submitManualReading should route strictly through FogProcessingService")
    void testSubmitManualReading() {
        TelemetryIngestRequestDto manualDto = new TelemetryIngestRequestDto(
                "PAT-1001", "SENS-01", 80.0, 98.0, 36.8, 120.0, 80.0, "FOG-HOSPITAL-01", "Manual test"
        );

        FogProcessingResultDto mockResult = new FogProcessingResultDto();
        mockResult.setPatientId("PAT-1001");
        mockResult.setHealthStatus("NORMAL");

        when(fogProcessingService.processTelemetry(manualDto)).thenReturn(mockResult);

        FogProcessingResultDto result = simulationService.submitManualReading(manualDto);

        assertThat(result.getPatientId()).isEqualTo("PAT-1001");
        verify(fogProcessingService).processTelemetry(manualDto);
    }
}
