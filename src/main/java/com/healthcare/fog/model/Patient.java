package com.healthcare.fog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document entity representing a registered patient in the Smart Healthcare Monitoring System.
 */
@Document(collection = "patients")
public class Patient {

    @Id
    private String id;

    @Indexed(unique = true)
    private String patientId;

    private String name;
    private Integer age;
    private String gender;

    @Indexed(unique = true)
    private String contactNumber;

    private List<String> medicalHistory = new ArrayList<>();
    private LocalDateTime registrationDate;

    // Fog Computing Architecture Attributes
    private String assignedFogNodeId;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean active = true;

    public Patient() {
        this.registrationDate = LocalDateTime.now();
    }

    public Patient(String patientId, String name, Integer age, String gender,
                   String contactNumber, List<String> medicalHistory,
                   LocalDateTime registrationDate, String assignedFogNodeId,
                   String emergencyContactName, String emergencyContactPhone) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.medicalHistory = (medicalHistory != null) ? medicalHistory : new ArrayList<>();
        this.registrationDate = (registrationDate != null) ? registrationDate : LocalDateTime.now();
        this.assignedFogNodeId = assignedFogNodeId;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.active = true;
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
