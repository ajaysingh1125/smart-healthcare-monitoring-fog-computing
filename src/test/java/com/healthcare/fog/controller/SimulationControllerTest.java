package com.healthcare.fog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.service.SensorSimulationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SensorSimulationService simulationService;

    @Test
    @DisplayName("POST /api/simulation/start should start simulation and return status")
    void testStartSimulation() throws Exception {
        Map<String, Object> statusMap = new LinkedHashMap<>();
        statusMap.put("isRunning", true);
        statusMap.put("intervalMs", 3000L);
        statusMap.put("simulationMode", "MIXED");

        Mockito.when(simulationService.startSimulation(eq(3000L), eq("MIXED"))).thenReturn(statusMap);

        mockMvc.perform(post("/api/simulation/start?intervalMs=3000&mode=MIXED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRunning", is(true)))
                .andExpect(jsonPath("$.intervalMs", is(3000)))
                .andExpect(jsonPath("$.simulationMode", is("MIXED")));
    }

    @Test
    @DisplayName("POST /api/simulation/stop should stop simulation")
    void testStopSimulation() throws Exception {
        Map<String, Object> statusMap = new LinkedHashMap<>();
        statusMap.put("isRunning", false);

        Mockito.when(simulationService.stopSimulation()).thenReturn(statusMap);

        mockMvc.perform(post("/api/simulation/stop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRunning", is(false)));
    }

    @Test
    @DisplayName("GET /api/simulation/status should return simulation status")
    void testGetStatus() throws Exception {
        Map<String, Object> statusMap = new LinkedHashMap<>();
        statusMap.put("isRunning", true);
        statusMap.put("totalReadingsGenerated", 42L);

        Mockito.when(simulationService.getSimulationStatus()).thenReturn(statusMap);

        mockMvc.perform(get("/api/simulation/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRunning", is(true)))
                .andExpect(jsonPath("$.totalReadingsGenerated", is(42)));
    }

    @Test
    @DisplayName("POST /api/simulation/generate should trigger scenario reading")
    void testGenerateSingleReading() throws Exception {
        FogProcessingResultDto result = new FogProcessingResultDto();
        result.setPatientId("PAT-1001");
        result.setHealthStatus("CRITICAL");
        result.setFogAction("CRITICAL_IMMEDIATE_FORWARD");

        Mockito.when(simulationService.generateSingleReading(eq("PAT-1001"), eq("CRITICAL"))).thenReturn(result);

        mockMvc.perform(post("/api/simulation/generate?patientId=PAT-1001&scenario=CRITICAL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.patientId", is("PAT-1001")))
                .andExpect(jsonPath("$.healthStatus", is("CRITICAL")));
    }

    @Test
    @DisplayName("POST /api/simulation/submit-reading should process manual reading")
    void testSubmitManualReading() throws Exception {
        TelemetryIngestRequestDto manualDto = new TelemetryIngestRequestDto(
                "PAT-1001", "SENS-01", 75.0, 98.0, 36.8, 118.0, 78.0, "FOG-HOSPITAL-01", "Manual"
        );

        FogProcessingResultDto result = new FogProcessingResultDto();
        result.setPatientId("PAT-1001");
        result.setHealthStatus("NORMAL");

        Mockito.when(simulationService.submitManualReading(any(TelemetryIngestRequestDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/simulation/submit-reading")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manualDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId", is("PAT-1001")))
                .andExpect(jsonPath("$.healthStatus", is("NORMAL")));
    }
}
