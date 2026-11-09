package com.healthcare.fog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a Fog Computing Node deployed at the local network edge
 * for proximate healthcare telemetry aggregation and latency-sensitive alerting.
 */
@Document(collection = "fog_nodes")
public class FogNode {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nodeId;

    private String name;
    private String location;
    private String ipAddress;
    private Integer port;
    private String status; // ONLINE, OFFLINE, DEGRADED, MAINTENANCE
    private String nodeType; // HOSPITAL, CLINIC, EMERGENCY

    // Workload & Capacity Tracking
    private Integer maxCapacity = 50;
    private Integer currentWorkload = 0;

    // Edge Performance Telemetry
    private Double cpuUsagePercent = 0.0;
    private Double memoryUsagePercent = 0.0;
    private Long activeSensorConnections = 0L;
    private Instant lastHeartbeat;

    // Fog Processing Statistics
    private Long processedReadingsCount = 0L;
    private Long filteredReadingsCount = 0L;
    private Long criticalAlertsCount = 0L;
    private Double averageProcessingTimeMs = 0.0;
    private Double totalProcessingTimeMs = 0.0;

    private List<String> managedPatientIds = new ArrayList<>();

    public FogNode() {
        this.status = "ONLINE";
        this.lastHeartbeat = Instant.now();
    }

    public FogNode(String nodeId, String name, String location, String ipAddress,
                   Integer port, String status, String nodeType, Integer maxCapacity) {
        this.nodeId = nodeId;
        this.name = name;
        this.location = location;
        this.ipAddress = ipAddress;
        this.port = port;
        this.status = status;
        this.nodeType = nodeType;
        this.maxCapacity = maxCapacity;
        this.currentWorkload = 0;
        this.lastHeartbeat = Instant.now();
        this.cpuUsagePercent = 5.0;
        this.memoryUsagePercent = 12.0;
        this.activeSensorConnections = 0L;
        this.processedReadingsCount = 0L;
        this.filteredReadingsCount = 0L;
        this.criticalAlertsCount = 0L;
        this.averageProcessingTimeMs = 0.0;
        this.totalProcessingTimeMs = 0.0;
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
