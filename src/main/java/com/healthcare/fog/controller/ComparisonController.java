package com.healthcare.fog.controller;

import com.healthcare.fog.model.ComparisonExperiment;
import com.healthcare.fog.service.FogCloudComparisonService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 7 REST API: Fog Computing vs Cloud Computing Performance Comparison.
 *
 * <p>Endpoints to trigger experiments and retrieve results.
 * <ul>
 *   <li>POST /api/comparison/run          – Run a new experiment</li>
 *   <li>GET  /api/comparison/latest       – Latest result</li>
 *   <li>GET  /api/comparison/history      – All results</li>
 *   <li>GET  /api/comparison/{id}         – Specific experiment</li>
 *   <li>GET  /api/comparison/methodology  – Measurement transparency docs</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/comparison")
public class ComparisonController {

    private final FogCloudComparisonService comparisonService;

    public ComparisonController(FogCloudComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    /**
     * POST /api/comparison/run?datasetSize=100
     * Runs a new Fog vs Cloud comparison experiment on a reproducible synthetic dataset.
     */
    @PostMapping(value = "/run", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComparisonExperiment> runComparison(
            @RequestParam(name = "datasetSize", defaultValue = "100") int datasetSize) {
        int size = Math.max(10, Math.min(datasetSize, 1000)); // clamp to [10, 1000]
        return ResponseEntity.ok(comparisonService.runComparison(size));
    }

    /**
     * GET /api/comparison/latest
     * Returns the most recently completed experiment result.
     */
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLatest() {
        return comparisonService.getLatestResult()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(noResultYet()));
    }

    /**
     * GET /api/comparison/history
     * Returns all experiment results, newest first.
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ComparisonExperiment>> getHistory() {
        return ResponseEntity.ok(comparisonService.getAllResults());
    }

    /**
     * GET /api/comparison/{experimentId}
     * Returns a specific result by experimentId (e.g., COMP-0001).
     */
    @GetMapping(value = "/{experimentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("experimentId") String experimentId) {
        return comparisonService.getByExperimentId(experimentId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/comparison/methodology
     * Returns the measurement transparency explanation for academic review.
     */
    @GetMapping(value = "/methodology", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMethodology() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("timestamp", Instant.now().toString());
        info.put("phase", "Phase 7 – Fog vs Cloud Performance Comparison");
        info.put("measuredMetrics", List.of(
                "Java processing time per reading (System.nanoTime — nanosecond precision)",
                "Total Java processing time for full dataset",
                "Average Java processing time per reading",
                "Cloud transmission count (actual routing decision count)",
                "Locally filtered reading count (actual routing decision count)"
        ));
        info.put("simulatedEstimates", List.of(
                "Network latency per cloud round-trip (constant: " + FogCloudComparisonService.CLOUD_RTT_MS + " ms)",
                "Total estimated network overhead (transmissions × constant)",
                "Bandwidth consumption (transmissions × " + FogCloudComparisonService.PAYLOAD_BYTES + " bytes)"
        ));
        info.put("datasetNote",
                "Both modes process the same synthetic dataset generated with Random(seed=42). " +
                "Dataset size is configurable via ?datasetSize= parameter (10–1000).");
        info.put("academicDisclaimer", FogCloudComparisonService.ACADEMIC_DISCLAIMER);
        info.put("networkSimulationConstants", Map.of(
                "cloudRoundTripLatencyMs", FogCloudComparisonService.CLOUD_RTT_MS,
                "fogToCloudForwardLatencyMs", FogCloudComparisonService.FOG_FORWARD_RTT_MS,
                "readingPayloadBytes", FogCloudComparisonService.PAYLOAD_BYTES
        ));
        return ResponseEntity.ok(info);
    }

    private Map<String, Object> noResultYet() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", Instant.now().toString());
        m.put("message", "No comparison experiments run yet. POST /api/comparison/run to begin.");
        return m;
    }
}
