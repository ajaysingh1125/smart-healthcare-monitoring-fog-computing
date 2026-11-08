package com.healthcare.fog.dto;

import java.io.Serializable;

public class DatabaseHealth implements Serializable {

    private String status;
    private String database;
    private String host;
    private long responseTimeMs;
    private String message;

    public DatabaseHealth() {
    }

    public DatabaseHealth(String status, String database, String host, long responseTimeMs, String message) {
        this.status = status;
        this.database = database;
        this.host = host;
        this.responseTimeMs = responseTimeMs;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
