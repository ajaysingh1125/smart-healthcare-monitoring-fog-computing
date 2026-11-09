package com.healthcare.fog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing an IoT medical sensor device attached to a patient
 * and paired with a fog node.
 */
@Document(collection = "sensor_devices")
public class SensorDevice {

    @Id
    private String id;

    @Indexed(unique = true)
    private String deviceId;

    private String name;
    private String deviceType; // PULSE_OXIMETER, ECG_MONITOR, TEMPERATURE_SENSOR, BLOOD_PRESSURE_CUFF
    private String manufacturer;

    @Indexed
    private String assignedPatientId;

    @Indexed
    private String pairedFogNodeId;

    private String status; // ACTIVE, STANDBY, FAULTY, CALIBRATING
    private Integer batteryPercent;
    private Instant lastActiveTimestamp;

    public SensorDevice() {
        this.status = "ACTIVE";
        this.batteryPercent = 100;
        this.lastActiveTimestamp = Instant.now();
    }

    public SensorDevice(String deviceId, String name, String deviceType,
                        String manufacturer, String assignedPatientId,
                        String pairedFogNodeId, Integer batteryPercent) {
        this.deviceId = deviceId;
        this.name = name;
        this.deviceType = deviceType;
        this.manufacturer = manufacturer;
        this.assignedPatientId = assignedPatientId;
        this.pairedFogNodeId = pairedFogNodeId;
        this.batteryPercent = batteryPercent;
        this.status = "ACTIVE";
        this.lastActiveTimestamp = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getAssignedPatientId() {
        return assignedPatientId;
    }

    public void setAssignedPatientId(String assignedPatientId) {
        this.assignedPatientId = assignedPatientId;
    }

    public String getPairedFogNodeId() {
        return pairedFogNodeId;
    }

    public void setPairedFogNodeId(String pairedFogNodeId) {
        this.pairedFogNodeId = pairedFogNodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(Integer batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public Instant getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public void setLastActiveTimestamp(Instant lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }
}
