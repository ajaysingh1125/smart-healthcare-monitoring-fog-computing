package com.healthcare.fog.controller;

import com.healthcare.fog.dto.DatabaseHealth;
import com.healthcare.fog.dto.HealthStatusResponse;
import com.healthcare.fog.service.HealthCheckService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckService healthCheckService;

    @Test
    @DisplayName("GET /api/health returns 200 OK when service and MongoDB are UP")
    void getHealth_shouldReturn200_whenHealthy() throws Exception {
        DatabaseHealth dbHealth = new DatabaseHealth(
                "UP",
                "smart_healthcare",
                "localhost:27017",
                5L,
                "MongoDB Community Server connection verified and responsive"
        );
        HealthStatusResponse response = new HealthStatusResponse(
                "UP",
                "smart-healthcare-fog-node",
                "Fog Computing Monitoring Gateway",
                dbHealth,
                new HashMap<>()
        );

        Mockito.when(healthCheckService.performHealthCheck()).thenReturn(response);

        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("smart-healthcare-fog-node")))
                .andExpect(jsonPath("$.database.status", is("UP")))
                .andExpect(jsonPath("$.database.database", is("smart_healthcare")));
    }

    @Test
    @DisplayName("GET /api/health returns 503 SERVICE_UNAVAILABLE when MongoDB is DOWN")
    void getHealth_shouldReturn503_whenDatabaseDown() throws Exception {
        DatabaseHealth dbHealth = new DatabaseHealth(
                "DOWN",
                "smart_healthcare",
                "localhost:27017",
                100L,
                "MongoDB connectivity failure: connection refused"
        );
        HealthStatusResponse response = new HealthStatusResponse(
                "DOWN",
                "smart-healthcare-fog-node",
                "Fog Computing Monitoring Gateway",
                dbHealth,
                new HashMap<>()
        );

        Mockito.when(healthCheckService.performHealthCheck()).thenReturn(response);

        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is("DOWN")))
                .andExpect(jsonPath("$.database.status", is("DOWN")));
    }
}
