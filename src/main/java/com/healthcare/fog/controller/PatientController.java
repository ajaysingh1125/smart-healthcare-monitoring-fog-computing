package com.healthcare.fog.controller;

import com.healthcare.fog.dto.PatientRequestDto;
import com.healthcare.fog.dto.PatientResponseDto;
import com.healthcare.fog.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing Patient CRUD operations for the Smart Healthcare Fog Monitoring Node.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * GET /api/patients
     * Retrieve list of all registered patients.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientResponseDto>> getAllPatients() {
        List<PatientResponseDto> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * GET /api/patients/{id}
     * Retrieve patient by MongoDB ObjectId or business patientId.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable("id") String id) {
        PatientResponseDto patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * POST /api/patients
     * Register a new patient.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponseDto> createPatient(@Valid @RequestBody PatientRequestDto requestDto) {
        PatientResponseDto created = patientService.createPatient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/patients/{id}
     * Update an existing patient by ID.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponseDto> updatePatient(@PathVariable("id") String id,
                                                            @Valid @RequestBody PatientRequestDto requestDto) {
        PatientResponseDto updated = patientService.updatePatient(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/patients/{id}
     * Remove a patient record from MongoDB.
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deletePatient(@PathVariable("id") String id) {
        patientService.deletePatient(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Patient record deleted successfully");
        response.put("deletedIdentifier", id);
        return ResponseEntity.ok(response);
    }
}
