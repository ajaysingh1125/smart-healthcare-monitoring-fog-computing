package com.healthcare.fog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.fog.dto.FogNodeResponseDto;
import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.repository.EmergencyAlertRepository;
import com.healthcare.fog.service.FogLoadBalancer;
import com.healthcare.fog.service.FogLocalCache;
import com.healthcare.fog.service.FogNodeService;
import com.healthcare.fog.service.FogProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FogNodeController.class)
class FogNodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FogNodeService fogNodeService;

    @MockBean
    private FogProcessingService fogProcessingService;

    @MockBean
    private FogLoadBalancer fogLoadBalancer;

    @MockBean
    private FogLocalCache fogLocalCache;

    @MockBean
    private EmergencyAlertRepository emergencyAlertRepository;

    @Test
    @DisplayName("GET /api/fog-nodes should return list of fog nodes")
    void getAllNodes_shouldReturnList() throws Exception {
        FogNodeResponseDto dto = new FogNodeResponseDto();
        dto.setNodeId("FOG-HOSPITAL-01");
        dto.setName("Hospital Core Fog Node");
        dto.setStatus("ONLINE");
        dto.setCurrentWorkload(4);
        dto.setMaxCapacity(50);

        Mockito.when(fogNodeService.getAllNodes()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/fog-nodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nodeId", is("FOG-HOSPITAL-01")))
                .andExpect(jsonPath("$[0].status", is("ONLINE")));
    }

    @Test
    @DisplayName("PUT /api/fog-nodes/{nodeId}/status should update status")
    void updateNodeStatus_shouldReturnStatusMap() throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nodeId", "FOG-CLINIC-01");
        map.put("newStatus", "OFFLINE");
        map.put("failoverTriggered", true);

        Mockito.when(fogNodeService.updateNodeStatus(eq("FOG-CLINIC-01"), eq("OFFLINE"))).thenReturn(map);

        mockMvc.perform(put("/api/fog-nodes/FOG-CLINIC-01/status?status=OFFLINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId", is("FOG-CLINIC-01")))
                .andExpect(jsonPath("$.newStatus", is("OFFLINE")))
                .andExpect(jsonPath("$.failoverTriggered", is(true)));
    }

    @Test
    @DisplayName("POST /api/fog-nodes/ingest should process telemetry")
    void ingestTelemetry_shouldReturnResult() throws Exception {
        TelemetryIngestRequestDto ingestDto = new TelemetryIngestRequestDto(
                "PAT-1001", "SENS-01", 72.0, 98.0, 36.8, 115.0, 75.0, null, "Normal"
        );

        FogProcessingResultDto resultDto = new FogProcessingResultDto();
        resultDto.setPatientId("PAT-1001");
        resultDto.setFogNodeId("FOG-HOSPITAL-01");
        resultDto.setHealthStatus("NORMAL");
        resultDto.setFogAction("ROUTINE_FILTERED_LOCALLY");
        resultDto.setForwardedToCloud(false);
        resultDto.setCachedInFog(true);
        resultDto.setProcessingTimeMs(0.35);

        Mockito.when(fogProcessingService.processTelemetry(any(TelemetryIngestRequestDto.class)))
                .thenReturn(resultDto);

        mockMvc.perform(post("/api/fog-nodes/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId", is("PAT-1001")))
                .andExpect(jsonPath("$.healthStatus", is("NORMAL")))
                .andExpect(jsonPath("$.fogAction", is("ROUTINE_FILTERED_LOCALLY")));
    }
}
