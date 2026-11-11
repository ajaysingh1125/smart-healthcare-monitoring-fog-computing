package com.healthcare.fog.dto;

import com.healthcare.fog.model.EmergencyAlert;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Result payload produced by the Fog Computing Layer after executing edge analysis.
 */
public class FogProcessingResultDto implements Serializable {

    private String readingId;
    private String patientId;
    private String patientName;
    private String fogNodeId;
    private String fogNodeName;
    private String fogNodeType;
    private String timestamp;

    // Physiological Metrics Summary
    private Map<String, Object> vitals;

    // Fog Edge Evaluation & Decision
    private String healthStatus;         // NORMAL, WARNING, CRITICAL
    private String fogAction;            // ROUTINE_FILTERED_LOCALLY, EVALUATED_WARNING, CRITICAL_IMMEDIATE_FORWARD
    private boolean forwardedToCloud;    // True if transmitted to cloud, false if filtered locally
    private boolean cachedInFog;         // True if stored in local fast cache
    private double processingTimeMs;     // Actual execution latency in milliseconds

    // Emergency Alert (if critical threshold violated)
    private EmergencyAlert generatedAlert;

    // Fog Node Operational Status
    private String nodeStatus;
    private int nodeCurrentWorkload;
    private int nodeMaxCapacity;

    public FogProcessingResultDto() {
        this.timestamp = Instant.now().toString();
    }

    // Getters and Setters
    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getFogNodeId() {
        return fogNodeId;
    }

    public void setFogNodeId(String fogNodeId) {
        this.fogNodeId = fogNodeId;
    }

    public String getFogNodeName() {
        return fogNodeName;
    }

    public void setFogNodeName(String fogNodeName) {
        this.fogNodeName = fogNodeName;
    }

    public String getFogNodeType() {
        return fogNodeType;
    }

    public void setFogNodeType(String fogNodeType) {
        this.fogNodeType = fogNodeType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getVitals() {
        return vitals;
    }

    public void setVitals(Map<String, Object> vitals) {
        this.vitals = vitals;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getFogAction() {
        return fogAction;
    }

    public void setFogAction(String fogAction) {
        this.fogAction = fogAction;
    }

    public boolean isForwardedToCloud() {
        return forwardedToCloud;
    }

    public void setForwardedToCloud(boolean forwardedToCloud) {
        this.forwardedToCloud = forwardedToCloud;
    }

    public boolean isCachedInFog() {
        return cachedInFog;
    }

    public void setCachedInFog(boolean cachedInFog) {
        this.cachedInFog = cachedInFog;
    }

    public double getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(double processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public EmergencyAlert getGeneratedAlert() {
        return generatedAlert;
    }

    public void setGeneratedAlert(EmergencyAlert generatedAlert) {
        this.generatedAlert = generatedAlert;
    }

    public String getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(String nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public int getNodeCurrentWorkload() {
        return nodeCurrentWorkload;
    }

    public void setNodeCurrentWorkload(int nodeCurrentWorkload) {
        this.nodeCurrentWorkload = nodeCurrentWorkload;
    }

    public int getNodeMaxCapacity() {
        return nodeMaxCapacity;
    }

    public void setNodeMaxCapacity(int nodeMaxCapacity) {
        this.nodeMaxCapacity = nodeMaxCapacity;
    }
}
