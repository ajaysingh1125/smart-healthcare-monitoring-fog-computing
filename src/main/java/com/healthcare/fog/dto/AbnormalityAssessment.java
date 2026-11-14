package com.healthcare.fog.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of edge abnormality detection on multi-vital telemetry.
 */
public class AbnormalityAssessment implements Serializable {

    private String status;               // NORMAL, WARNING, CRITICAL
    private String leadingVitalType;      // SPO2, HEART_RATE, BODY_TEMPERATURE, SYSTOLIC_BP, DIASTOLIC_BP
    private Double measuredValue;
    private Double thresholdLimit;
    private Double thresholdExceeded;
    private String diagnosticMessage;
    private List<String> violatedParameters = new ArrayList<>();
    private String academicNotice = AbnormalityThresholds.ACADEMIC_DISCLAIMER;

    public AbnormalityAssessment() {
        this.status = "NORMAL";
        this.diagnosticMessage = "Vitals within normal demonstration thresholds";
    }

    public AbnormalityAssessment(String status, String leadingVitalType, Double measuredValue,
                                 Double thresholdLimit, Double thresholdExceeded,
                                 String diagnosticMessage, List<String> violatedParameters) {
        this.status = status;
        this.leadingVitalType = leadingVitalType;
        this.measuredValue = measuredValue;
        this.thresholdLimit = thresholdLimit;
        this.thresholdExceeded = thresholdExceeded;
        this.diagnosticMessage = diagnosticMessage;
        this.violatedParameters = (violatedParameters != null) ? violatedParameters : new ArrayList<>();
        this.academicNotice = AbnormalityThresholds.ACADEMIC_DISCLAIMER;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLeadingVitalType() {
        return leadingVitalType;
    }

    public void setLeadingVitalType(String leadingVitalType) {
        this.leadingVitalType = leadingVitalType;
    }

    public Double getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(Double measuredValue) {
        this.measuredValue = measuredValue;
    }

    public Double getThresholdLimit() {
        return thresholdLimit;
    }

    public void setThresholdLimit(Double thresholdLimit) {
        this.thresholdLimit = thresholdLimit;
    }

    public Double getThresholdExceeded() {
        return thresholdExceeded;
    }

    public void setThresholdExceeded(Double thresholdExceeded) {
        this.thresholdExceeded = thresholdExceeded;
    }

    public String getDiagnosticMessage() {
        return diagnosticMessage;
    }

    public void setDiagnosticMessage(String diagnosticMessage) {
        this.diagnosticMessage = diagnosticMessage;
    }

    public List<String> getViolatedParameters() {
        return violatedParameters;
    }

    public void setViolatedParameters(List<String> violatedParameters) {
        this.violatedParameters = violatedParameters;
    }

    public String getAcademicNotice() {
        return academicNotice;
    }

    public void setAcademicNotice(String academicNotice) {
        this.academicNotice = academicNotice;
    }
}
