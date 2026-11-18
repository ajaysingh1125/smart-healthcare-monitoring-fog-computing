package com.healthcare.fog.controller;

import com.healthcare.fog.model.ComparisonExperiment;
import com.healthcare.fog.service.FogCloudComparisonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComparisonController.class)
class ComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FogCloudComparisonService comparisonService;

    @Test
    @DisplayName("POST /api/comparison/run should execute experiment and return result")
    void testRunComparison() throws Exception {
        ComparisonExperiment exp = new ComparisonExperiment();
        exp.setExperimentId("COMP-0001");
        exp.setDatasetSize(100);
        exp.setStatus("COMPLETED");
        exp.setTimestamp(Instant.now());

        Mockito.when(comparisonService.runComparison(anyInt())).thenReturn(exp);

        mockMvc.perform(post("/api/comparison/run?datasetSize=100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experimentId", is("COMP-0001")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.datasetSize", is(100)));
    }

    @Test
    @DisplayName("GET /api/comparison/latest should return latest experiment")
    void testGetLatest() throws Exception {
        ComparisonExperiment exp = new ComparisonExperiment();
        exp.setExperimentId("COMP-0001");
        exp.setStatus("COMPLETED");

        Mockito.when(comparisonService.getLatestResult()).thenReturn(Optional.of(exp));

        mockMvc.perform(get("/api/comparison/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experimentId", is("COMP-0001")))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("GET /api/comparison/methodology should return transparency documentation")
    void testGetMethodology() throws Exception {
        mockMvc.perform(get("/api/comparison/methodology")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase", is("Phase 7 – Fog vs Cloud Performance Comparison")))
                .andExpect(jsonPath("$.measuredMetrics").isArray())
                .andExpect(jsonPath("$.simulatedEstimates").isArray());
    }
}
