package com.healthcare.fog.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Incoming biomedical telemetry sent from patient IoT sensors to the Fog Computing Layer.
 */
public class TelemetryIngestRequestDto implements Serializable {

    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @JsonProperty("sensorDeviceId")
    @JsonAlias({"deviceId", "sensorId"})
    private String sensorDeviceId;

    @NotNull(message = "Heart rate is required")
    @DecimalMin(value = "20.0", message = "Heart rate must be at least 20 BPM")
    @DecimalMax(value = "260.0", message = "Heart rate cannot exceed 260 BPM")
    private Double heartRate;

    @NotNull(message = "Blood Oxygen (SpO2) is required")
    @DecimalMin(value = "50.0", message = "SpO2 must be at least 50%")
    @DecimalMax(value = "100.0", message = "SpO2 cannot exceed 100%")
    @JsonProperty("bloodOxygenSpO2")
    @JsonAlias({"spo2", "bloodOxygen", "spO2"})
    private Double bloodOxygenSpO2;

    @NotNull(message = "Body temperature is required")
    @DecimalMin(value = "30.0", message = "Body temperature must be at least 30.0°C")
    @DecimalMax(value = "45.0", message = "Body temperature cannot exceed 45.0°C")
    @JsonProperty("bodyTemperature")
    @JsonAlias({"temperature", "temp"})
    private Double bodyTemperature;

    @NotNull(message = "Systolic blood pressure is required")
    @DecimalMin(value = "50.0", message = "Systolic BP must be at least 50 mmHg")
    @DecimalMax(value = "260.0", message = "Systolic BP cannot exceed 260 mmHg")
    @JsonProperty("systolicBloodPressure")
    @JsonAlias({"systolicBp", "systolic", "systolicPressure"})
    private Double systolicBloodPressure;

    @NotNull(message = "Diastolic blood pressure is required")
    @DecimalMin(value = "30.0", message = "Diastolic BP must be at least 30 mmHg")
    @DecimalMax(value = "160.0", message = "Diastolic BP cannot exceed 160 mmHg")
    @JsonProperty("diastolicBloodPressure")
    @JsonAlias({"diastolicBp", "diastolic", "diastolicPressure"})
    private Double diastolicBloodPressure;

    private String preferredFogNodeId;
    private String notes;

    public TelemetryIngestRequestDto() {
    }

    public TelemetryIngestRequestDto(String patientId, String sensorDeviceId, Double heartRate,
                                     Double bloodOxygenSpO2, Double bodyTemperature,
                                     Double systolicBloodPressure, Double diastolicBloodPressure,
                                     String preferredFogNodeId, String notes) {
        this.patientId = patientId;
        this.sensorDeviceId = sensorDeviceId;
        this.heartRate = heartRate;
        this.bloodOxygenSpO2 = bloodOxygenSpO2;
        this.bodyTemperature = bodyTemperature;
        this.systolicBloodPressure = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
        this.preferredFogNodeId = preferredFogNodeId;
        this.notes = notes;
    }

    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSensorDeviceId() {
        return sensorDeviceId;
    }

    public void setSensorDeviceId(String sensorDeviceId) {
        this.sensorDeviceId = sensorDeviceId;
    }

    public Double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Double heartRate) {
        this.heartRate = heartRate;
    }

    public Double getBloodOxygenSpO2() {
        return bloodOxygenSpO2;
    }

    public void setBloodOxygenSpO2(Double bloodOxygenSpO2) {
        this.bloodOxygenSpO2 = bloodOxygenSpO2;
    }

    public Double getBodyTemperature() {
        return bodyTemperature;
    }

    public void setBodyTemperature(Double bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }

    public Double getSystolicBloodPressure() {
        return systolicBloodPressure;
    }

    public void setSystolicBloodPressure(Double systolicBloodPressure) {
        this.systolicBloodPressure = systolicBloodPressure;
    }

    public Double getDiastolicBloodPressure() {
        return diastolicBloodPressure;
    }

    public void setDiastolicBloodPressure(Double diastolicBloodPressure) {
        this.diastolicBloodPressure = diastolicBloodPressure;
    }

    public String getPreferredFogNodeId() {
        return preferredFogNodeId;
    }

    public void setPreferredFogNodeId(String preferredFogNodeId) {
        this.preferredFogNodeId = preferredFogNodeId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Convenience aliases
    public Double getSpo2() { return bloodOxygenSpO2; }
    public void setSpo2(Double spo2) { this.bloodOxygenSpO2 = spo2; }

    public Double getTemperature() { return bodyTemperature; }
    public void setTemperature(Double temp) { this.bodyTemperature = temp; }

    public Double getSystolicBp() { return systolicBloodPressure; }
    public void setSystolicBp(Double sys) { this.systolicBloodPressure = sys; }

    public Double getDiastolicBp() { return diastolicBloodPressure; }
    public void setDiastolicBp(Double dia) { this.diastolicBloodPressure = dia; }

    public String getDeviceId() { return sensorDeviceId; }
    public void setDeviceId(String devId) { this.sensorDeviceId = devId; }
}
