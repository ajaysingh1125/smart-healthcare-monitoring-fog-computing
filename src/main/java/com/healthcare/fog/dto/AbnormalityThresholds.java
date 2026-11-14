package com.healthcare.fog.dto;

import java.io.Serializable;

/**
 * Configurable demonstration thresholds for multi-vital abnormality detection.
 *
 * DISCLAIMER: These thresholds are established exclusively for academic demonstration
 * and evaluation of edge computing architectures. They do NOT constitute medical diagnostic rules.
 */
public class AbnormalityThresholds implements Serializable {

    public static final String ACADEMIC_DISCLAIMER = 
            "DISCLAIMER: Threshold parameters are configured strictly for academic demonstration " +
            "and evaluation of Fog Computing algorithms. They do NOT represent clinical medical diagnostic guidelines.";

    // Heart Rate (BPM)
    private double heartRateCriticalLow = 50.0;
    private double heartRateWarningLow = 60.0;
    private double heartRateWarningHigh = 100.0;
    private double heartRateCriticalHigh = 120.0;

    // Blood Oxygen SpO2 (%)
    private double spo2CriticalLow = 90.0;
    private double spo2WarningLow = 95.0;

    // Body Temperature (°C)
    private double tempCriticalLow = 35.5;
    private double tempWarningLow = 36.5;
    private double tempWarningHigh = 37.5;
    private double tempCriticalHigh = 38.5;

    // Systolic Blood Pressure (mmHg)
    private double systolicCriticalLow = 85.0;
    private double systolicWarningLow = 90.0;
    private double systolicWarningHigh = 120.0;
    private double systolicCriticalHigh = 140.0;

    // Diastolic Blood Pressure (mmHg)
    private double diastolicCriticalLow = 55.0;
    private double diastolicWarningLow = 60.0;
    private double diastolicWarningHigh = 80.0;
    private double diastolicCriticalHigh = 90.0;

    public AbnormalityThresholds() {
    }

    public String getAcademicDisclaimer() {
        return ACADEMIC_DISCLAIMER;
    }

    // Getters and Setters
    public double getHeartRateCriticalLow() {
        return heartRateCriticalLow;
    }

    public void setHeartRateCriticalLow(double heartRateCriticalLow) {
        this.heartRateCriticalLow = heartRateCriticalLow;
    }

    public double getHeartRateWarningLow() {
        return heartRateWarningLow;
    }

    public void setHeartRateWarningLow(double heartRateWarningLow) {
        this.heartRateWarningLow = heartRateWarningLow;
    }

    public double getHeartRateWarningHigh() {
        return heartRateWarningHigh;
    }

    public void setHeartRateWarningHigh(double heartRateWarningHigh) {
        this.heartRateWarningHigh = heartRateWarningHigh;
    }

    public double getHeartRateCriticalHigh() {
        return heartRateCriticalHigh;
    }

    public void setHeartRateCriticalHigh(double heartRateCriticalHigh) {
        this.heartRateCriticalHigh = heartRateCriticalHigh;
    }

    public double getSpo2CriticalLow() {
        return spo2CriticalLow;
    }

    public void setSpo2CriticalLow(double spo2CriticalLow) {
        this.spo2CriticalLow = spo2CriticalLow;
    }

    public double getSpo2WarningLow() {
        return spo2WarningLow;
    }

    public void setSpo2WarningLow(double spo2WarningLow) {
        this.spo2WarningLow = spo2WarningLow;
    }

    public double getTempCriticalLow() {
        return tempCriticalLow;
    }

    public void setTempCriticalLow(double tempCriticalLow) {
        this.tempCriticalLow = tempCriticalLow;
    }

    public double getTempWarningLow() {
        return tempWarningLow;
    }

    public void setTempWarningLow(double tempWarningLow) {
        this.tempWarningLow = tempWarningLow;
    }

    public double getTempWarningHigh() {
        return tempWarningHigh;
    }

    public void setTempWarningHigh(double tempWarningHigh) {
        this.tempWarningHigh = tempWarningHigh;
    }

    public double getTempCriticalHigh() {
        return tempCriticalHigh;
    }

    public void setTempCriticalHigh(double tempCriticalHigh) {
        this.tempCriticalHigh = tempCriticalHigh;
    }

    public double getSystolicCriticalLow() {
        return systolicCriticalLow;
    }

    public void setSystolicCriticalLow(double systolicCriticalLow) {
        this.systolicCriticalLow = systolicCriticalLow;
    }

    public double getSystolicWarningLow() {
        return systolicWarningLow;
    }

    public void setSystolicWarningLow(double systolicWarningLow) {
        this.systolicWarningLow = systolicWarningLow;
    }

    public double getSystolicWarningHigh() {
        return systolicWarningHigh;
    }

    public void setSystolicWarningHigh(double systolicWarningHigh) {
        this.systolicWarningHigh = systolicWarningHigh;
    }

    public double getSystolicCriticalHigh() {
        return systolicCriticalHigh;
    }

    public void setSystolicCriticalHigh(double systolicCriticalHigh) {
        this.systolicCriticalHigh = systolicCriticalHigh;
    }

    public double getDiastolicCriticalLow() {
        return diastolicCriticalLow;
    }

    public void setDiastolicCriticalLow(double diastolicCriticalLow) {
        this.diastolicCriticalLow = diastolicCriticalLow;
    }

    public double getDiastolicWarningLow() {
        return diastolicWarningLow;
    }

    public void setDiastolicWarningLow(double diastolicWarningLow) {
        this.diastolicWarningLow = diastolicWarningLow;
    }

    public double getDiastolicWarningHigh() {
        return diastolicWarningHigh;
    }

    public void setDiastolicWarningHigh(double diastolicWarningHigh) {
        this.diastolicWarningHigh = diastolicWarningHigh;
    }

    public double getDiastolicCriticalHigh() {
        return diastolicCriticalHigh;
    }

    public void setDiastolicCriticalHigh(double diastolicCriticalHigh) {
        this.diastolicCriticalHigh = diastolicCriticalHigh;
    }
}
