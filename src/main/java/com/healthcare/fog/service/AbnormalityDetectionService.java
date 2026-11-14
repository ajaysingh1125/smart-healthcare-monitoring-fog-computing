package com.healthcare.fog.service;

import com.healthcare.fog.dto.AbnormalityAssessment;
import com.healthcare.fog.dto.AbnormalityThresholds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Edge-based Abnormality Detection Service.
 * Evaluates patient biomedical telemetry locally at Fog Nodes against multi-parameter
 * clinical demonstration thresholds.
 *
 * IMPORTANT ACADEMIC NOTICE:
 * The thresholds and classification logic in this service are designed strictly for
 * academic demonstration and simulation of distributed Fog Computing architectures.
 * They do NOT constitute medical diagnostic rules or clinical practice standards.
 */
@Service
public class AbnormalityDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AbnormalityDetectionService.class);

    private AbnormalityThresholds thresholds = new AbnormalityThresholds();

    /**
     * Evaluates all 5 physiological parameters locally at the Fog Node.
     * Classifies telemetry into NORMAL, WARNING, or CRITICAL severity.
     */
    public AbnormalityAssessment evaluateVitals(Double heartRate, Double bloodOxygenSpO2,
                                                Double bodyTemperature, Double systolicBP,
                                                Double diastolicBP) {
        List<String> criticalIssues = new ArrayList<>();
        List<String> warningIssues = new ArrayList<>();

        String leadingMetric = "NONE";
        double leadingValue = 0.0;
        double thresholdLimit = 0.0;
        double thresholdDiff = 0.0;
        String primaryMessage = "Vitals within normal physiological demonstration thresholds";

        // ====================================================================
        // 1. Blood Oxygen (SpO2 %) Evaluation
        // ====================================================================
        if (bloodOxygenSpO2 != null) {
            if (bloodOxygenSpO2 < thresholds.getSpo2CriticalLow()) {
                criticalIssues.add(String.format("Critical Hypoxia: SpO2 %.1f%% < %.1f%%",
                        bloodOxygenSpO2, thresholds.getSpo2CriticalLow()));
                leadingMetric = "SPO2";
                leadingValue = bloodOxygenSpO2;
                thresholdLimit = thresholds.getSpo2CriticalLow();
                thresholdDiff = thresholds.getSpo2CriticalLow() - bloodOxygenSpO2;
                primaryMessage = String.format("Critical Hypoxia Detected: Blood oxygen saturation %.1f%% has dropped below demonstration safety threshold (%.1f%%)",
                        bloodOxygenSpO2, thresholds.getSpo2CriticalLow());
            } else if (bloodOxygenSpO2 < thresholds.getSpo2WarningLow()) {
                warningIssues.add(String.format("Mild Hypoxia: SpO2 %.1f%% < %.1f%%",
                        bloodOxygenSpO2, thresholds.getSpo2WarningLow()));
            }
        }

        // ====================================================================
        // 2. Heart Rate (BPM) Evaluation
        // ====================================================================
        if (heartRate != null) {
            if (heartRate > thresholds.getHeartRateCriticalHigh()) {
                criticalIssues.add(String.format("Severe Tachycardia: Heart rate %.1f BPM > %.1f BPM",
                        heartRate, thresholds.getHeartRateCriticalHigh()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "HEART_RATE";
                    leadingValue = heartRate;
                    thresholdLimit = thresholds.getHeartRateCriticalHigh();
                    thresholdDiff = heartRate - thresholds.getHeartRateCriticalHigh();
                    primaryMessage = String.format("Severe Tachycardia Detected: Heart rate %.1f BPM exceeds critical limit (%.1f BPM)",
                            heartRate, thresholds.getHeartRateCriticalHigh());
                }
            } else if (heartRate < thresholds.getHeartRateCriticalLow()) {
                criticalIssues.add(String.format("Severe Bradycardia: Heart rate %.1f BPM < %.1f BPM",
                        heartRate, thresholds.getHeartRateCriticalLow()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "HEART_RATE";
                    leadingValue = heartRate;
                    thresholdLimit = thresholds.getHeartRateCriticalLow();
                    thresholdDiff = thresholds.getHeartRateCriticalLow() - heartRate;
                    primaryMessage = String.format("Severe Bradycardia Detected: Heart rate %.1f BPM is dangerously low (below %.1f BPM)",
                            heartRate, thresholds.getHeartRateCriticalLow());
                }
            } else if (heartRate > thresholds.getHeartRateWarningHigh()) {
                warningIssues.add(String.format("Elevated Heart Rate: %.1f BPM > %.1f BPM",
                        heartRate, thresholds.getHeartRateWarningHigh()));
            } else if (heartRate < thresholds.getHeartRateWarningLow()) {
                warningIssues.add(String.format("Borderline Low Heart Rate: %.1f BPM < %.1f BPM",
                        heartRate, thresholds.getHeartRateWarningLow()));
            }
        }

        // ====================================================================
        // 3. Body Temperature (°C) Evaluation
        // ====================================================================
        if (bodyTemperature != null) {
            if (bodyTemperature > thresholds.getTempCriticalHigh()) {
                criticalIssues.add(String.format("Severe Hyperthermia: Temp %.1f°C > %.1f°C",
                        bodyTemperature, thresholds.getTempCriticalHigh()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "BODY_TEMPERATURE";
                    leadingValue = bodyTemperature;
                    thresholdLimit = thresholds.getTempCriticalHigh();
                    thresholdDiff = bodyTemperature - thresholds.getTempCriticalHigh();
                    primaryMessage = String.format("Severe Hyperthermia Detected: Body temperature %.1f°C exceeds fever safety threshold (%.1f°C)",
                            bodyTemperature, thresholds.getTempCriticalHigh());
                }
            } else if (bodyTemperature < thresholds.getTempCriticalLow()) {
                criticalIssues.add(String.format("Severe Hypothermia: Temp %.1f°C < %.1f°C",
                        bodyTemperature, thresholds.getTempCriticalLow()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "BODY_TEMPERATURE";
                    leadingValue = bodyTemperature;
                    thresholdLimit = thresholds.getTempCriticalLow();
                    thresholdDiff = thresholds.getTempCriticalLow() - bodyTemperature;
                    primaryMessage = String.format("Severe Hypothermia Detected: Body temperature %.1f°C below critical limit (%.1f°C)",
                            bodyTemperature, thresholds.getTempCriticalLow());
                }
            } else if (bodyTemperature > thresholds.getTempWarningHigh()) {
                warningIssues.add(String.format("Low-Grade Fever: Temp %.1f°C > %.1f°C",
                        bodyTemperature, thresholds.getTempWarningHigh()));
            } else if (bodyTemperature < thresholds.getTempWarningLow()) {
                warningIssues.add(String.format("Mild Hypothermia: Temp %.1f°C < %.1f°C",
                        bodyTemperature, thresholds.getTempWarningLow()));
            }
        }

        // ====================================================================
        // 4. Systolic Blood Pressure (mmHg) Evaluation
        // ====================================================================
        if (systolicBP != null) {
            if (systolicBP >= thresholds.getSystolicCriticalHigh()) {
                criticalIssues.add(String.format("Hypertensive Crisis: Systolic BP %.1f mmHg >= %.1f mmHg",
                        systolicBP, thresholds.getSystolicCriticalHigh()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "SYSTOLIC_BP";
                    leadingValue = systolicBP;
                    thresholdLimit = thresholds.getSystolicCriticalHigh();
                    thresholdDiff = systolicBP - thresholds.getSystolicCriticalHigh();
                    primaryMessage = String.format("Hypertensive Crisis Detected: Systolic pressure %.1f mmHg exceeds critical ceiling (%.1f mmHg)",
                            systolicBP, thresholds.getSystolicCriticalHigh());
                }
            } else if (systolicBP < thresholds.getSystolicCriticalLow()) {
                criticalIssues.add(String.format("Hypotensive Shock Risk: Systolic BP %.1f mmHg < %.1f mmHg",
                        systolicBP, thresholds.getSystolicCriticalLow()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "SYSTOLIC_BP";
                    leadingValue = systolicBP;
                    thresholdLimit = thresholds.getSystolicCriticalLow();
                    thresholdDiff = thresholds.getSystolicCriticalLow() - systolicBP;
                    primaryMessage = String.format("Hypotensive Collapse Risk: Systolic pressure %.1f mmHg below critical floor (%.1f mmHg)",
                            systolicBP, thresholds.getSystolicCriticalLow());
                }
            } else if (systolicBP > thresholds.getSystolicWarningHigh()) {
                warningIssues.add(String.format("Pre-Hypertension Systolic: %.1f mmHg > %.1f mmHg",
                        systolicBP, thresholds.getSystolicWarningHigh()));
            } else if (systolicBP < thresholds.getSystolicWarningLow()) {
                warningIssues.add(String.format("Borderline Low Systolic: %.1f mmHg < %.1f mmHg",
                        systolicBP, thresholds.getSystolicWarningLow()));
            }
        }

        // ====================================================================
        // 5. Diastolic Blood Pressure (mmHg) Evaluation
        // ====================================================================
        if (diastolicBP != null) {
            if (diastolicBP >= thresholds.getDiastolicCriticalHigh()) {
                criticalIssues.add(String.format("Severe Diastolic Spike: Diastolic BP %.1f mmHg >= %.1f mmHg",
                        diastolicBP, thresholds.getDiastolicCriticalHigh()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "DIASTOLIC_BP";
                    leadingValue = diastolicBP;
                    thresholdLimit = thresholds.getDiastolicCriticalHigh();
                    thresholdDiff = diastolicBP - thresholds.getDiastolicCriticalHigh();
                    primaryMessage = String.format("Diastolic Hypertensive Spike: %.1f mmHg >= %.1f mmHg",
                            diastolicBP, thresholds.getDiastolicCriticalHigh());
                }
            } else if (diastolicBP < thresholds.getDiastolicCriticalLow()) {
                criticalIssues.add(String.format("Severe Diastolic Drop: Diastolic BP %.1f mmHg < %.1f mmHg",
                        diastolicBP, thresholds.getDiastolicCriticalLow()));
                if ("NONE".equals(leadingMetric)) {
                    leadingMetric = "DIASTOLIC_BP";
                    leadingValue = diastolicBP;
                    thresholdLimit = thresholds.getDiastolicCriticalLow();
                    thresholdDiff = thresholds.getDiastolicCriticalLow() - diastolicBP;
                    primaryMessage = String.format("Severe Diastolic Hypotension: %.1f mmHg < %.1f mmHg",
                            diastolicBP, thresholds.getDiastolicCriticalLow());
                }
            } else if (diastolicBP > thresholds.getDiastolicWarningHigh()) {
                warningIssues.add(String.format("Elevated Diastolic BP: %.1f mmHg > %.1f mmHg",
                        diastolicBP, thresholds.getDiastolicWarningHigh()));
            } else if (diastolicBP < thresholds.getDiastolicWarningLow()) {
                warningIssues.add(String.format("Borderline Low Diastolic BP: %.1f mmHg < %.1f mmHg",
                        diastolicBP, thresholds.getDiastolicWarningLow()));
            }
        }

        // Determine Final Classification
        if (!criticalIssues.isEmpty()) {
            return new AbnormalityAssessment(
                    "CRITICAL", leadingMetric, leadingValue, thresholdLimit, thresholdDiff,
                    primaryMessage, criticalIssues
            );
        } else if (!warningIssues.isEmpty()) {
            return new AbnormalityAssessment(
                    "WARNING",
                    !warningIssues.isEmpty() ? warningIssues.get(0).split(":")[0] : "WARNING",
                    0.0, 0.0, 0.0,
                    "Warning: Physiological parameters outside standard demonstration bounds: " + String.join("; ", warningIssues),
                    warningIssues
            );
        } else {
            return new AbnormalityAssessment(
                    "NORMAL", "NONE", 0.0, 0.0, 0.0,
                    "Vitals within normal demonstration physiological thresholds",
                    List.of()
            );
        }
    }

    public AbnormalityThresholds getThresholds() {
        return thresholds;
    }

    public synchronized void updateThresholds(AbnormalityThresholds newThresholds) {
        if (newThresholds != null) {
            this.thresholds = newThresholds;
            log.info("Demonstration abnormality thresholds updated.");
        }
    }
}
