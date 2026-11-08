package com.healthcare.fog.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

public class HealthStatusResponse implements Serializable {

    private String status;
    private String service;
    private String timestamp;
    private String architectureRole;
    private DatabaseHealth database;
    private Map<String, Object> systemDetails;

    public HealthStatusResponse() {
        this.timestamp = Instant.now().toString();
    }

    public HealthStatusResponse(String status, String service, String architectureRole,
                                DatabaseHealth database, Map<String, Object> systemDetails) {
        this.status = status;
        this.service = service;
        this.architectureRole = architectureRole;
        this.timestamp = Instant.now().toString();
        this.database = database;
        this.systemDetails = systemDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getArchitectureRole() {
        return architectureRole;
    }

    public void setArchitectureRole(String architectureRole) {
        this.architectureRole = architectureRole;
    }

    public DatabaseHealth getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseHealth database) {
        this.database = database;
    }

    public Map<String, Object> getSystemDetails() {
        return systemDetails;
    }

    public void setSystemDetails(Map<String, Object> systemDetails) {
        this.systemDetails = systemDetails;
    }
}
