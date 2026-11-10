package com.healthcare.fog.service;

import com.healthcare.fog.dto.PatientRequestDto;
import com.healthcare.fog.dto.PatientResponseDto;
import com.healthcare.fog.exception.DuplicateResourceException;
import com.healthcare.fog.exception.ResourceNotFoundException;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Retrieve all registered patients.
     */
    public List<PatientResponseDto> getAllPatients() {
        log.debug("Fetching all patients from MongoDB");
        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single patient by MongoDB internal ID or business patientId.
     */
    public PatientResponseDto getPatientById(String identifier) {
        log.debug("Fetching patient with identifier: {}", identifier);
        Patient patient = findPatientByIdentifier(identifier);
        return mapToResponseDto(patient);
    }

    /**
     * Register a new patient in the fog system.
     */
    public PatientResponseDto createPatient(PatientRequestDto requestDto) {
        log.info("Creating new patient record with ID: {}", requestDto.getPatientId());

        if (patientRepository.existsByPatientId(requestDto.getPatientId())) {
            throw new DuplicateResourceException("Patient already exists with Patient ID: " + requestDto.getPatientId());
        }

        if (patientRepository.existsByContactNumber(requestDto.getContactNumber())) {
            throw new DuplicateResourceException("Patient with contact number " + requestDto.getContactNumber() + " is already registered");
        }

        Patient patient = new Patient();
        patient.setPatientId(requestDto.getPatientId());
        patient.setName(requestDto.getName());
        patient.setAge(requestDto.getAge());
        patient.setGender(requestDto.getGender().toUpperCase());
        patient.setContactNumber(requestDto.getContactNumber());
        patient.setMedicalHistory(requestDto.getMedicalHistory());
        patient.setRegistrationDate(LocalDateTime.now());
        patient.setAssignedFogNodeId(requestDto.getAssignedFogNodeId());
        patient.setEmergencyContactName(requestDto.getEmergencyContactName());
        patient.setEmergencyContactPhone(requestDto.getEmergencyContactPhone());
        patient.setActive(true);

        Patient saved = patientRepository.save(patient);
        log.info("Patient '{}' successfully persisted with MongoDB ID: {}", saved.getPatientId(), saved.getId());
        return mapToResponseDto(saved);
    }

    /**
     * Update an existing patient record.
     */
    public PatientResponseDto updatePatient(String identifier, PatientRequestDto requestDto) {
        log.info("Updating patient record for identifier: {}", identifier);
        Patient existing = findPatientByIdentifier(identifier);

        // Check if updating patientId conflicts with another record
        if (patientRepository.existsByPatientIdAndIdNot(requestDto.getPatientId(), existing.getId())) {
            throw new DuplicateResourceException("Another patient is already registered with Patient ID: " + requestDto.getPatientId());
        }

        // Check if updating contactNumber conflicts with another record
        if (patientRepository.existsByContactNumberAndIdNot(requestDto.getContactNumber(), existing.getId())) {
            throw new DuplicateResourceException("Another patient is already registered with Contact Number: " + requestDto.getContactNumber());
        }

        existing.setPatientId(requestDto.getPatientId());
        existing.setName(requestDto.getName());
        existing.setAge(requestDto.getAge());
        existing.setGender(requestDto.getGender().toUpperCase());
        existing.setContactNumber(requestDto.getContactNumber());
        existing.setMedicalHistory(requestDto.getMedicalHistory());
        existing.setAssignedFogNodeId(requestDto.getAssignedFogNodeId());
        existing.setEmergencyContactName(requestDto.getEmergencyContactName());
        existing.setEmergencyContactPhone(requestDto.getEmergencyContactPhone());

        Patient updated = patientRepository.save(existing);
        log.info("Patient '{}' successfully updated in MongoDB", updated.getPatientId());
        return mapToResponseDto(updated);
    }

    /**
     * Delete a patient record.
     */
    public void deletePatient(String identifier) {
        log.info("Deleting patient record for identifier: {}", identifier);
        Patient patient = findPatientByIdentifier(identifier);
        patientRepository.delete(patient);
        log.info("Patient '{}' deleted successfully", patient.getPatientId());
    }

    /**
     * Helper to find patient by either MongoDB primary key (Id) or business patientId.
     */
    private Patient findPatientByIdentifier(String identifier) {
        return patientRepository.findById(identifier)
                .or(() -> patientRepository.findByPatientId(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with identifier: " + identifier));
    }

    /**
     * Maps Patient domain model to PatientResponseDto.
     */
    public PatientResponseDto mapToResponseDto(Patient patient) {
        return new PatientResponseDto(
                patient.getId(),
                patient.getPatientId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getContactNumber(),
                patient.getMedicalHistory(),
                patient.getRegistrationDate(),
                patient.getAssignedFogNodeId(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getActive()
        );
    }
}
