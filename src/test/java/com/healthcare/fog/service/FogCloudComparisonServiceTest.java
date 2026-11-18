package com.healthcare.fog.service;

import com.healthcare.fog.model.ComparisonExperiment;
import com.healthcare.fog.repository.ComparisonExperimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class FogCloudComparisonServiceTest {

    private ComparisonExperimentRepository repository;
    private FogCloudComparisonService comparisonService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ComparisonExperimentRepository.class);
        Mockito.when(repository.save(any(ComparisonExperiment.class))).thenAnswer(i -> i.getArgument(0));
        comparisonService = new FogCloudComparisonService(repository);
    }

    @Test
    @DisplayName("runComparison should execute both Mode A and Mode B on the same dataset")
    void testRunComparison() {
        int datasetSize = 50;
        ComparisonExperiment experiment = comparisonService.runComparison(datasetSize);

        assertNotNull(experiment);
        assertEquals("COMPLETED", experiment.getStatus());
        assertEquals(datasetSize, experiment.getDatasetSize());
        assertNotNull(experiment.getExperimentId());
        assertTrue(experiment.getExperimentId().startsWith("COMP-"));

        // Mode A: Cloud-Only verification
        ComparisonExperiment.ModeMetrics cloudMetrics = experiment.getCloudOnlyMetrics();
        assertNotNull(cloudMetrics);
        assertEquals("CLOUD_ONLY", cloudMetrics.getMode());
        assertEquals(datasetSize, cloudMetrics.getTotalReadings());
        assertEquals(datasetSize, cloudMetrics.getTotalCloudTransmissions());
        assertEquals(0, cloudMetrics.getLocallyFilteredReadings());
        assertEquals(0.0, cloudMetrics.getFilterPercentage());
        assertTrue(cloudMetrics.getTotalJavaProcessingNs() > 0);
        assertTrue(cloudMetrics.getEstimatedTotalNetworkLatencyMs() > 0);
        assertTrue(cloudMetrics.getEstimatedBandwidthBytes() > 0);

        // Mode B: Fog-Based verification
        ComparisonExperiment.ModeMetrics fogMetrics = experiment.getFogBasedMetrics();
        assertNotNull(fogMetrics);
        assertEquals("FOG_BASED", fogMetrics.getMode());
        assertEquals(datasetSize, fogMetrics.getTotalReadings());
        assertTrue(fogMetrics.getLocallyFilteredReadings() > 0);
        assertTrue(fogMetrics.getFilterPercentage() > 0);
        assertTrue(fogMetrics.getTotalCloudTransmissions() < datasetSize);
        assertEquals(datasetSize, fogMetrics.getLocallyFilteredReadings() + fogMetrics.getTotalCloudTransmissions());

        // Comparison Summary verification
        ComparisonExperiment.ComparisonSummary summary = experiment.getSummary();
        assertNotNull(summary);
        assertTrue(summary.getNetworkLatencySavingsMs() > 0);
        assertTrue(summary.getBandwidthSavingsBytes() > 0);
        assertTrue(summary.getNetworkLatencySavingsPercent() > 0);
        assertTrue(summary.getBandwidthSavingsPercent() > 0);
        assertNotNull(summary.getJavaProcessingNote());
        assertNotNull(summary.getComparisonNote());
    }

    @Test
    @DisplayName("getLatestResult should retrieve from repository")
    void testGetLatestResult() {
        ComparisonExperiment exp = new ComparisonExperiment();
        exp.setExperimentId("COMP-0001");
        Mockito.when(repository.findFirstByOrderByTimestampDesc()).thenReturn(Optional.of(exp));

        Optional<ComparisonExperiment> latest = comparisonService.getLatestResult();
        assertTrue(latest.isPresent());
        assertEquals("COMP-0001", latest.get().getExperimentId());
    }
}
