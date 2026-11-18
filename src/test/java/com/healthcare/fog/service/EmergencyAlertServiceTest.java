package com.healthcare.fog.service;

import com.healthcare.fog.dto.AbnormalityAssessment;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.EmergencyAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmergencyAlertServiceTest {

    private EmergencyAlertRepository alertRepository;
    private EmergencyAlertService alertService;

    @BeforeEach
    void setUp() {
        alertRepository = Mockito.mock(EmergencyAlertRepository.class);
        alertService = new EmergencyAlertService(alertRepository);
    }

    @Test
    @DisplayName("createEmergencyAlert should populate patient details and vital type")
    void testCreateEmergencyAlert() {
        Patient patient = new Patient();
        patient.setPatientId("PAT-1001");
        patient.setName("Eleanor Vance");
        patient.setAge(68);
        patient.setGender("FEMALE");

        FogNode fogNode = new FogNode();
        fogNode.setNodeId("FOG-HOSPITAL-01");
        fogNode.setName("Hospital Core Fog Node");

        AbnormalityAssessment assessment = new AbnormalityAssessment();
        assessment.setStatus("CRITICAL");
        assessment.setLeadingVitalType("SPO2");
        assessment.setMeasuredValue(86.5);
        assessment.setThresholdLimit(90.0);
        assessment.setThresholdExceeded(3.5);
        assessment.setDiagnosticMessage("Critical Hypoxia");

        when(alertRepository.save(any(EmergencyAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyAlert alert = alertService.createEmergencyAlert(patient, fogNode, "SENS-01", assessment);

        assertThat(alert.getAlertId()).startsWith("ALT-");
        assertThat(alert.getPatientId()).isEqualTo("PAT-1001");
        assertThat(alert.getPatientName()).isEqualTo("Eleanor Vance");
        assertThat(alert.getPatientAge()).isEqualTo(68);
        assertThat(alert.getPatientGender()).isEqualTo("FEMALE");
        assertThat(alert.getFogNodeId()).isEqualTo("FOG-HOSPITAL-01");
        assertThat(alert.getAlertLevel()).isEqualTo("CRITICAL");
        assertThat(alert.getVitalType()).isEqualTo("SPO2");
        assertThat(alert.isForwardedToCloud()).isTrue();
        assertThat(alert.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("acknowledgeAlert should mark acknowledged with clinician name")
    void testAcknowledgeAlert() {
        EmergencyAlert alert = new EmergencyAlert();
        alert.setAlertId("ALT-12345");
        alert.setAlertLevel("CRITICAL");

        when(alertRepository.findByAlertId("ALT-12345")).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(EmergencyAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyAlert acknowledged = alertService.acknowledgeAlert("ALT-12345", "Dr. Gregory House", "Reviewing ICU vitals");

        assertThat(acknowledged.isAcknowledged()).isTrue();
        assertThat(acknowledged.getStatus()).isEqualTo("ACKNOWLEDGED");
        assertThat(acknowledged.getAcknowledgedBy()).isEqualTo("Dr. Gregory House");
        assertThat(acknowledged.getAcknowledgedAt()).isNotNull();
    }

    @Test
    @DisplayName("resolveAlert should succeed when manual clinician action is provided")
    void testResolveAlert_ManualClinicianSuccess() {
        EmergencyAlert alert = new EmergencyAlert();
        alert.setAlertId("ALT-12345");
        alert.setAlertLevel("CRITICAL");

        when(alertRepository.findByAlertId("ALT-12345")).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(EmergencyAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyAlert resolved = alertService.resolveAlert("ALT-12345", "Dr. Allison Cameron", "Patient stabilized", true);

        assertThat(resolved.isResolved()).isTrue();
        assertThat(resolved.getStatus()).isEqualTo("RESOLVED");
        assertThat(resolved.getResolvedBy()).isEqualTo("Dr. Allison Cameron");
        assertThat(resolved.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("CRITICAL alert CANNOT be automatically resolved without clinician attribution")
    void testCriticalAlert_CannotBeAutoResolved() {
        EmergencyAlert criticalAlert = new EmergencyAlert();
        criticalAlert.setAlertId("ALT-99999");
        criticalAlert.setAlertLevel("CRITICAL");

        when(alertRepository.findByAlertId("ALT-99999")).thenReturn(Optional.of(criticalAlert));

        // Attempt automatic system resolution (isManual = false)
        assertThrows(IllegalStateException.class, () -> {
            alertService.resolveAlert("ALT-99999", null, "Auto-cleared", false);
        });

        // Attempt manual resolution with empty clinician name
        assertThrows(IllegalStateException.class, () -> {
            alertService.resolveAlert("ALT-99999", "   ", "Manual clear", true);
        });
    }
}
