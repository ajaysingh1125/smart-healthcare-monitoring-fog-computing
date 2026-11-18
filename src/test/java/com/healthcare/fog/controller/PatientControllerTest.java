package com.healthcare.fog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.fog.dto.PatientRequestDto;
import com.healthcare.fog.dto.PatientResponseDto;
import com.healthcare.fog.exception.DuplicateResourceException;
import com.healthcare.fog.exception.ResourceNotFoundException;
import com.healthcare.fog.service.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @Test
    @DisplayName("GET /api/patients returns list of patients")
    void getAllPatients_shouldReturnList() throws Exception {
        PatientResponseDto p1 = new PatientResponseDto(
                "60f1", "PAT-1001", "Eleanor Vance", 68, "FEMALE", "+1-555-0101",
                Arrays.asList("Hypertension"), LocalDateTime.now(), "FOG-NODE-ICU-01",
                "Thomas Vance", "+1-555-0102", true
        );
        Mockito.when(patientService.getAllPatients()).thenReturn(Collections.singletonList(p1));

        mockMvc.perform(get("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientId", is("PAT-1001")))
                .andExpect(jsonPath("$[0].name", is("Eleanor Vance")));
    }

    @Test
    @DisplayName("GET /api/patients/{id} returns single patient when found")
    void getPatientById_shouldReturnPatient_whenFound() throws Exception {
        PatientResponseDto p1 = new PatientResponseDto(
                "60f1", "PAT-1001", "Eleanor Vance", 68, "FEMALE", "+1-555-0101",
                Arrays.asList("Hypertension"), LocalDateTime.now(), "FOG-NODE-ICU-01",
                "Thomas Vance", "+1-555-0102", true
        );
        Mockito.when(patientService.getPatientById("PAT-1001")).thenReturn(p1);

        mockMvc.perform(get("/api/patients/PAT-1001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId", is("PAT-1001")))
                .andExpect(jsonPath("$.name", is("Eleanor Vance")))
                .andExpect(jsonPath("$.age", is(68)));
    }

    @Test
    @DisplayName("GET /api/patients/{id} returns 404 when patient not found")
    void getPatientById_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(patientService.getPatientById("PAT-9999"))
                .thenThrow(new ResourceNotFoundException("Patient not found with identifier: PAT-9999"));

        mockMvc.perform(get("/api/patients/PAT-9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("POST /api/patients creates patient and returns 201 CREATED")
    void createPatient_shouldReturn201_whenValid() throws Exception {
        PatientRequestDto request = new PatientRequestDto(
                "PAT-1011", "Alice Wonder", 40, "FEMALE", "+1-555-1111",
                Arrays.asList("Asthma"), "FOG-NODE-GEN-01", "Bob Wonder", "+1-555-1112"
        );
        PatientResponseDto response = new PatientResponseDto(
                "60f2", "PAT-1011", "Alice Wonder", 40, "FEMALE", "+1-555-1111",
                Arrays.asList("Asthma"), LocalDateTime.now(), "FOG-NODE-GEN-01",
                "Bob Wonder", "+1-555-1112", true
        );

        Mockito.when(patientService.createPatient(any(PatientRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId", is("PAT-1011")))
                .andExpect(jsonPath("$.name", is("Alice Wonder")));
    }

    @Test
    @DisplayName("POST /api/patients returns 400 BAD REQUEST when validation fails")
    void createPatient_shouldReturn400_whenInvalid() throws Exception {
        // Missing name and invalid negative age
        PatientRequestDto invalidRequest = new PatientRequestDto();
        invalidRequest.setPatientId("PAT-1012");
        invalidRequest.setName(""); // Invalid: blank
        invalidRequest.setAge(-5);  // Invalid: min 1
        invalidRequest.setGender("UNKNOWN_GENDER"); // Invalid regex

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("POST /api/patients returns 409 CONFLICT when duplicate patient")
    void createPatient_shouldReturn409_whenDuplicate() throws Exception {
        PatientRequestDto request = new PatientRequestDto(
                "PAT-1001", "Duplicate Person", 50, "MALE", "+1-555-0101",
                Collections.emptyList(), "FOG-NODE-ICU-01", "Contact", "+1-555-9999"
        );

        Mockito.when(patientService.createPatient(any(PatientRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Patient already exists with Patient ID: PAT-1001"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    @DisplayName("PUT /api/patients/{id} updates patient and returns 200 OK")
    void updatePatient_shouldReturn200_whenValid() throws Exception {
        PatientRequestDto updateRequest = new PatientRequestDto(
                "PAT-1001", "Eleanor Vance Updated", 69, "FEMALE", "+1-555-0101",
                Arrays.asList("Hypertension", "Atrial Fibrillation", "Controlled"),
                "FOG-NODE-ICU-01", "Thomas Vance", "+1-555-0102"
        );
        PatientResponseDto updatedResponse = new PatientResponseDto(
                "60f1", "PAT-1001", "Eleanor Vance Updated", 69, "FEMALE", "+1-555-0101",
                Arrays.asList("Hypertension", "Atrial Fibrillation", "Controlled"),
                LocalDateTime.now(), "FOG-NODE-ICU-01", "Thomas Vance", "+1-555-0102", true
        );

        Mockito.when(patientService.updatePatient(eq("PAT-1001"), any(PatientRequestDto.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/patients/PAT-1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Eleanor Vance Updated")))
                .andExpect(jsonPath("$.age", is(69)));
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} deletes patient and returns 200 OK")
    void deletePatient_shouldReturn200() throws Exception {
        Mockito.doNothing().when(patientService).deletePatient("PAT-1001");

        mockMvc.perform(delete("/api/patients/PAT-1001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Patient record deleted successfully")));
    }
}
