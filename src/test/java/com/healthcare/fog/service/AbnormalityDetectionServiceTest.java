package com.healthcare.fog.service;

import com.healthcare.fog.dto.AbnormalityAssessment;
import com.healthcare.fog.dto.AbnormalityThresholds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbnormalityDetectionServiceTest {

    private AbnormalityDetectionService detectionService;

    @BeforeEach
    void setUp() {
        detectionService = new AbnormalityDetectionService();
    }

    @Test
    @DisplayName("evaluateVitals should classify normal parameters as NORMAL")
    void testNormalVitals() {
        AbnormalityAssessment result = detectionService.evaluateVitals(
                72.0,  // Heart rate (60 - 100)
                98.0,  // SpO2 (>= 95)
                36.8,  // Temp (36.5 - 37.5)
                115.0, // Systolic BP (90 - 120)
                75.0   // Diastolic BP (60 - 80)
        );

        assertThat(result.getStatus()).isEqualTo("NORMAL");
        assertThat(result.getLeadingVitalType()).isEqualTo("NONE");
        assertThat(result.getViolatedParameters()).isEmpty();
        assertThat(result.getAcademicNotice()).contains("academic demonstration");
    }

    @Test
    @DisplayName("evaluateVitals should classify pre-hypertension and mild hypoxia as WARNING")
    void testWarningVitals() {
        AbnormalityAssessment result = detectionService.evaluateVitals(
                105.0, // Elevated HR
                93.0,  // Mild hypoxia (90 - 94.9)
                37.8,  // Low grade fever
                130.0, // Pre-hypertension
                85.0
        );

        assertThat(result.getStatus()).isEqualTo("WARNING");
        assertThat(result.getViolatedParameters()).isNotEmpty();
    }

    @Test
    @DisplayName("evaluateVitals should classify severe hypoxia as CRITICAL")
    void testCriticalHypoxia() {
        AbnormalityAssessment result = detectionService.evaluateVitals(
                95.0,
                87.0,  // Severe Hypoxia (< 90.0)
                37.0,
                120.0,
                80.0
        );

        assertThat(result.getStatus()).isEqualTo("CRITICAL");
        assertThat(result.getLeadingVitalType()).isEqualTo("SPO2");
        assertThat(result.getThresholdLimit()).isEqualTo(90.0);
        assertThat(result.getThresholdExceeded()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("evaluateVitals should classify severe tachycardia as CRITICAL")
    void testCriticalTachycardia() {
        AbnormalityAssessment result = detectionService.evaluateVitals(
                142.0, // Severe Tachycardia (> 120.0)
                97.0,
                37.0,
                120.0,
                80.0
        );

        assertThat(result.getStatus()).isEqualTo("CRITICAL");
        assertThat(result.getLeadingVitalType()).isEqualTo("HEART_RATE");
    }

    @Test
    @DisplayName("evaluateVitals should classify hypertensive crisis as CRITICAL")
    void testCriticalHypertensiveCrisis() {
        AbnormalityAssessment result = detectionService.evaluateVitals(
                85.0,
                97.0,
                37.0,
                165.0, // Systolic >= 140.0
                105.0  // Diastolic >= 90.0
        );

        assertThat(result.getStatus()).isEqualTo("CRITICAL");
        assertThat(result.getLeadingVitalType()).isEqualTo("SYSTOLIC_BP");
    }
}
