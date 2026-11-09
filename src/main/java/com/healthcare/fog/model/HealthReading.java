package com.healthcare.fog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing physiological vital signs telemetry collected by edge sensors
 * and ingested by local fog nodes.
 */
@Document(collection = "health_readings")
@CompoundIndexes({
    @CompoundIndex(name = "patient_timestamp_idx", def = "{'patientId': 1, 'timestamp': -1}"),
    @CompoundIndex(name = "fognode_timestamp_idx", def = "{'fogNodeId': 1, 'timestamp': -1}")
})
public class HealthReading {

    @Id
    private String id;

    @Indexed
    private String patientId;

    @Indexed
    private String fogNodeId;

    private String sensorDeviceId;

    // Physiological Vital Parameters
    private Double heartRate;              // Beats Per Minute (BPM)
    private Double bloodOxygenSpO2;         // Percentage (%)
    private Double bodyTemperature;         // Celsius (°C)
    private Double systolicBloodPressure;  // mmHg
    private Double diastolicBloodPressure; // mmHg

    private Instant timestamp;
    private String status; // NORMAL, WARNING, CRITICAL
    private String notes;

    // Fog Computing Edge Processing Fields
    private String fogAction;              // ROUTINE_FILTERED_LOCALLY, EVALUATED_WARNING, CRITICAL_IMMEDIATE_FORWARD
    private Double processingDurationMs;   // Latency of fog computation in milliseconds
    private Boolean forwardedToCloud;      // True if transmitted to cloud, False if filtered locally

    public HealthReading() {
        this.timestamp = Instant.now();
        this.status = "NORMAL";
        this.fogAction = "ROUTINE_FILTERED_LOCALLY";
        this.forwardedToCloud = false;
        this.processingDurationMs = 0.0;
    }

    public HealthReading(String patientId, String fogNodeId, String sensorDeviceId,
                         Double heartRate, Double bloodOxygenSpO2, Double bodyTemperature,
                         Double systolicBloodPressure, Double diastolicBloodPressure,
                         String status, String notes) {
        this.patientId = patientId;
        this.fogNodeId = fogNodeId;
        this.sensorDeviceId = sensorDeviceId;
        this.heartRate = heartRate;
        this.bloodOxygenSpO2 = bloodOxygenSpO2;
        this.bodyTemperature = bodyTemperature;
        this.systolicBloodPressure = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
        this.timestamp = Instant.now();
        this.status = status;
        this.notes = notes;
        this.fogAction = "ROUTINE_FILTERED_LOCALLY";
        this.forwardedToCloud = false;
        this.processingDurationMs = 0.0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFogNodeId() {
        return fogNodeId;
    }

    public void setFogNodeId(String fogNodeId) {
        this.fogNodeId = fogNodeId;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFogAction() {
        return fogAction;
    }

    public void setFogAction(String fogAction) {
        this.fogAction = fogAction;
    }

    public Double getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(Double processingDurationMs) {
        this.processingDurationMs = processingDurationMs;
    }

    public Boolean getForwardedToCloud() {
        return forwardedToCloud;
    }

    public void setForwardedToCloud(Boolean forwardedToCloud) {
        this.forwardedToCloud = forwardedToCloud;
    }
}
