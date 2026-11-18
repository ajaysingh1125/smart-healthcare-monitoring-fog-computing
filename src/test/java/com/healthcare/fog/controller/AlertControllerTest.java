package com.healthcare.fog.controller;

import com.healthcare.fog.dto.AbnormalityThresholds;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.service.AbnormalityDetectionService;
import com.healthcare.fog.service.EmergencyAlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmergencyAlertService alertService;

    @MockBean
    private AbnormalityDetectionService abnormalityDetectionService;

    @Test
    @DisplayName("GET /api/alerts/active should return active alerts")
    void testGetActiveAlerts() throws Exception {
        EmergencyAlert alert = new EmergencyAlert();
        alert.setAlertId("ALT-1001");
        alert.setPatientId("PAT-1001");
        alert.setPatientName("Eleanor Vance");
        alert.setAlertLevel("CRITICAL");
        alert.setStatus("ACTIVE");

        Mockito.when(alertService.getActiveAlerts()).thenReturn(Collections.singletonList(alert));

        mockMvc.perform(get("/api/alerts/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].alertId", is("ALT-1001")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    @DisplayName("PUT /api/alerts/{id}/acknowledge should acknowledge alert")
    void testAcknowledgeAlert() throws Exception {
        EmergencyAlert alert = new EmergencyAlert();
        alert.setAlertId("ALT-1001");
        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy("Dr. John Watson");

        Mockito.when(alertService.acknowledgeAlert(eq("ALT-1001"), eq("Dr. John Watson"), anyString()))
                .thenReturn(alert);

        mockMvc.perform(put("/api/alerts/ALT-1001/acknowledge?clinicianName=Dr. John Watson&notes=Reviewing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACKNOWLEDGED")))
                .andExpect(jsonPath("$.acknowledgedBy", is("Dr. John Watson")));
    }

    @Test
    @DisplayName("PUT /api/alerts/{id}/resolve should resolve alert")
    void testResolveAlert() throws Exception {
        EmergencyAlert alert = new EmergencyAlert();
        alert.setAlertId("ALT-1001");
        alert.setStatus("RESOLVED");
        alert.setResolved(true);
        alert.setResolvedBy("Dr. John Watson");

        Mockito.when(alertService.resolveAlert(eq("ALT-1001"), eq("Dr. John Watson"), anyString(), anyBoolean()))
                .thenReturn(alert);

        mockMvc.perform(put("/api/alerts/ALT-1001/resolve?clinicianName=Dr. John Watson&notes=Resolved")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedBy", is("Dr. John Watson")));
    }

    @Test
    @DisplayName("GET /api/alerts/thresholds should return demonstration thresholds")
    void testGetThresholds() throws Exception {
        AbnormalityThresholds thresholds = new AbnormalityThresholds();
        Mockito.when(abnormalityDetectionService.getThresholds()).thenReturn(thresholds);

        mockMvc.perform(get("/api/alerts/thresholds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicNotice").exists())
                .andExpect(jsonPath("$.thresholds.spo2CriticalLow", is(90.0)));
    }
}
