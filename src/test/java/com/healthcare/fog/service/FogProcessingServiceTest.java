package com.healthcare.fog.service;

import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.HealthReading;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.EmergencyAlertRepository;
import com.healthcare.fog.repository.FogNodeRepository;
import com.healthcare.fog.repository.HealthReadingRepository;
import com.healthcare.fog.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FogProcessingServiceTest {

    private FogNodeRepository fogNodeRepository;
    private PatientRepository patientRepository;
    private HealthReadingRepository healthReadingRepository;
    private EmergencyAlertRepository emergencyAlertRepository;
    private AbnormalityDetectionService abnormalityDetectionService;
    private EmergencyAlertService emergencyAlertService;
    private FogLocalCache fogLocalCache;
    private FogLoadBalancer fogLoadBalancer;
    private FogProcessingService fogProcessingService;

    @BeforeEach
    void setUp() {
        fogNodeRepository = Mockito.mock(FogNodeRepository.class);
        patientRepository = Mockito.mock(PatientRepository.class);
        healthReadingRepository = Mockito.mock(HealthReadingRepository.class);
        emergencyAlertRepository = Mockito.mock(EmergencyAlertRepository.class);
        abnormalityDetectionService = new AbnormalityDetectionService();
        emergencyAlertService = new EmergencyAlertService(emergencyAlertRepository);
        fogLocalCache = new FogLocalCache();
        fogLoadBalancer = Mockito.mock(FogLoadBalancer.class);

        fogProcessingService = new FogProcessingService(
                fogNodeRepository, patientRepository, healthReadingRepository,
                abnormalityDetectionService, emergencyAlertService, fogLocalCache, fogLoadBalancer
        );
    }

    @Test
    @DisplayName("processTelemetry should filter routine healthy reading locally without cloud forward")
    void processTelemetry_shouldFilterRoutineReadingLocally() {
        Patient patient = new Patient();
        patient.setPatientId("PAT-1001");
        patient.setName("Eleanor Vance");
        patient.setAssignedFogNodeId("FOG-HOSPITAL-01");

        FogNode node = new FogNode("FOG-HOSPITAL-01", "Hospital Core", "Ward B", "IP", 8081, "ONLINE", "HOSPITAL", 50);

        when(patientRepository.findByPatientId("PAT-1001")).thenReturn(Optional.of(patient));
        when(fogNodeRepository.findByNodeId("FOG-HOSPITAL-01")).thenReturn(Optional.of(node));
        when(healthReadingRepository.save(any(HealthReading.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fogNodeRepository.save(any(FogNode.class))).thenAnswer(inv -> inv.getArgument(0));

        TelemetryIngestRequestDto routineDto = new TelemetryIngestRequestDto(
                "PAT-1001", "SENS-01", 72.0, 98.0, 36.8, 115.0, 75.0, null, "Normal routine"
        );

        FogProcessingResultDto result = fogProcessingService.processTelemetry(routineDto);

        assertThat(result.getHealthStatus()).isEqualTo("NORMAL");
        assertThat(result.getFogAction()).isEqualTo("ROUTINE_FILTERED_LOCALLY");
        assertThat(result.isForwardedToCloud()).isFalse();
        assertThat(result.isCachedInFog()).isTrue();
        assertThat(result.getGeneratedAlert()).isNull();
        assertThat(result.getProcessingTimeMs()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("processTelemetry should immediately forward critical reading and create EmergencyAlert")
    void processTelemetry_shouldForwardCriticalAndCreateAlert() {
        Patient patient = new Patient();
        patient.setPatientId("PAT-1001");
        patient.setName("Eleanor Vance");
        patient.setAssignedFogNodeId("FOG-HOSPITAL-01");

        FogNode node = new FogNode("FOG-HOSPITAL-01", "Hospital Core", "Ward B", "IP", 8081, "ONLINE", "HOSPITAL", 50);

        when(patientRepository.findByPatientId("PAT-1001")).thenReturn(Optional.of(patient));
        when(fogNodeRepository.findByNodeId("FOG-HOSPITAL-01")).thenReturn(Optional.of(node));
        when(healthReadingRepository.save(any(HealthReading.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emergencyAlertRepository.save(any(EmergencyAlert.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fogNodeRepository.save(any(FogNode.class))).thenAnswer(inv -> inv.getArgument(0));

        // Critical hypoxia: SpO2 = 86.0% (Critical threshold is < 90.0%)
        TelemetryIngestRequestDto criticalDto = new TelemetryIngestRequestDto(
                "PAT-1001", "SENS-01", 145.0, 86.0, 37.1, 155.0, 95.0, null, "Emergency critical telemetry"
        );

        FogProcessingResultDto result = fogProcessingService.processTelemetry(criticalDto);

        assertThat(result.getHealthStatus()).isEqualTo("CRITICAL");
        assertThat(result.getFogAction()).isEqualTo("CRITICAL_IMMEDIATE_FORWARD");
        assertThat(result.isForwardedToCloud()).isTrue();
        assertThat(result.getGeneratedAlert()).isNotNull();
        assertThat(result.getGeneratedAlert().getAlertLevel()).isEqualTo("CRITICAL");
        assertThat(result.getProcessingTimeMs()).isGreaterThan(0.0);
    }
}
