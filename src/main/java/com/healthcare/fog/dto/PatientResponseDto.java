package com.healthcare.fog.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard response DTO for Patient representations.
 */
public class PatientResponseDto implements Serializable {

    private String id;
    private String patientId;
    private String name;
    private Integer age;
    private String gender;
    private String contactNumber;
    private List<String> medicalHistory;
    private LocalDateTime registrationDate;

    private String assignedFogNodeId;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean active;

    public PatientResponseDto() {
    }

    public PatientResponseDto(String id, String patientId, String name, Integer age,
                              String gender, String contactNumber, List<String> medicalHistory,
                              LocalDateTime registrationDate, String assignedFogNodeId,
                              String emergencyContactName, String emergencyContactPhone,
                              Boolean active) {
        this.id = id;
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.medicalHistory = medicalHistory;
        this.registrationDate = registrationDate;
        this.assignedFogNodeId = assignedFogNodeId;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.active = active;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public List<String> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<String> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getAssignedFogNodeId() {
        return assignedFogNodeId;
    }

    public void setAssignedFogNodeId(String assignedFogNodeId) {
        this.assignedFogNodeId = assignedFogNodeId;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
