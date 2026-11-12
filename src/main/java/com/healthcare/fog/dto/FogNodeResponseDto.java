package com.healthcare.fog.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class FogNodeResponseDto implements Serializable {

    private String id;
    private String nodeId;
    private String name;
    private String location;
    private String ipAddress;
    private Integer port;
    private String status;
    private String nodeType;

    private Integer maxCapacity;
    private Integer currentWorkload;
    private Double workloadPercentage;

    private Double cpuUsagePercent;
    private Double memoryUsagePercent;
    private Long activeSensorConnections;
    private Instant lastHeartbeat;

    private Long processedReadingsCount;
    private Long filteredReadingsCount;
    private Long criticalAlertsCount;
    private Double averageProcessingTimeMs;
    private Double totalProcessingTimeMs;

    private List<String> managedPatientIds;

    public FogNodeResponseDto() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(Integer currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public Double getWorkloadPercentage() {
        return workloadPercentage;
    }

    public void setWorkloadPercentage(Double workloadPercentage) {
        this.workloadPercentage = workloadPercentage;
    }

    public Double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(Double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public Double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public void setMemoryUsagePercent(Double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public Long getActiveSensorConnections() {
        return activeSensorConnections;
    }

    public void setActiveSensorConnections(Long activeSensorConnections) {
        this.activeSensorConnections = activeSensorConnections;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Long getProcessedReadingsCount() {
        return processedReadingsCount;
    }

    public void setProcessedReadingsCount(Long processedReadingsCount) {
        this.processedReadingsCount = processedReadingsCount;
    }

    public Long getFilteredReadingsCount() {
        return filteredReadingsCount;
    }

    public void setFilteredReadingsCount(Long filteredReadingsCount) {
        this.filteredReadingsCount = filteredReadingsCount;
    }

    public Long getCriticalAlertsCount() {
        return criticalAlertsCount;
    }

    public void setCriticalAlertsCount(Long criticalAlertsCount) {
        this.criticalAlertsCount = criticalAlertsCount;
    }

    public Double getAverageProcessingTimeMs() {
        return averageProcessingTimeMs;
    }

    public void setAverageProcessingTimeMs(Double averageProcessingTimeMs) {
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }

    public Double getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }

    public void setTotalProcessingTimeMs(Double totalProcessingTimeMs) {
        this.totalProcessingTimeMs = totalProcessingTimeMs;
    }

    public List<String> getManagedPatientIds() {
        return managedPatientIds;
    }

    public void setManagedPatientIds(List<String> managedPatientIds) {
        this.managedPatientIds = managedPatientIds;
    }
}
