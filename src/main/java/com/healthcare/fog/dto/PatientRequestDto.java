package com.healthcare.fog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Patient creation and update requests with strict validation.
 */
public class PatientRequestDto implements Serializable {

    @NotBlank(message = "Patient ID is required (e.g., PAT-1001)")
    @Size(min = 3, max = 30, message = "Patient ID must be between 3 and 30 characters")
    private String patientId;

    @NotBlank(message = "Patient name is required")
    @Size(min = 2, max = 100, message = "Patient name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1 year")
    @Max(value = 130, message = "Age cannot exceed 130 years")
    private Integer age;

    @NotBlank(message = "Gender is required (MALE, FEMALE, OTHER)")
    @Pattern(regexp = "^(?i)(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{7,20}$", message = "Contact number must be a valid phone number (7-20 digits)")
    private String contactNumber;

    @com.fasterxml.jackson.annotation.JsonFormat(with = com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> medicalHistory = new ArrayList<>();

    private String assignedFogNodeId;
    private String emergencyContactName;
    private String emergencyContactPhone;

    public PatientRequestDto() {
    }

    public PatientRequestDto(String patientId, String name, Integer age, String gender,
                             String contactNumber, List<String> medicalHistory,
                             String assignedFogNodeId, String emergencyContactName,
                             String emergencyContactPhone) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.medicalHistory = (medicalHistory != null) ? medicalHistory : new ArrayList<>();
        this.assignedFogNodeId = assignedFogNodeId;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
    }

    // Getters and Setters
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
        this.medicalHistory = (medicalHistory != null) ? medicalHistory : new ArrayList<>();
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
}
