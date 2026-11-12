package com.healthcare.fog.controller;

import com.healthcare.fog.dto.FogNodeResponseDto;
import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.PatientResponseDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.model.HealthReading;
import com.healthcare.fog.repository.EmergencyAlertRepository;
import com.healthcare.fog.service.FogLoadBalancer;
import com.healthcare.fog.service.FogLocalCache;
import com.healthcare.fog.service.FogNodeService;
import com.healthcare.fog.service.FogProcessingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Fog Computing Node Operations, Telemetry Ingestion,
 * Edge Processing, Load Balancing, and Status Management.
 */
@RestController
@RequestMapping("/api/fog-nodes")
public class FogNodeController {

    private final FogNodeService fogNodeService;
    private final FogProcessingService fogProcessingService;
    private final FogLoadBalancer fogLoadBalancer;
    private final FogLocalCache fogLocalCache;
    private final EmergencyAlertRepository emergencyAlertRepository;

    public FogNodeController(FogNodeService fogNodeService,
                             FogProcessingService fogProcessingService,
                             FogLoadBalancer fogLoadBalancer,
                             FogLocalCache fogLocalCache,
                             EmergencyAlertRepository emergencyAlertRepository) {
        this.fogNodeService = fogNodeService;
        this.fogProcessingService = fogProcessingService;
        this.fogLoadBalancer = fogLoadBalancer;
        this.fogLocalCache = fogLocalCache;
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    /**
     * GET /api/fog-nodes
     * Returns list and status of all configured Fog Nodes.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FogNodeResponseDto>> getAllNodes() {
        return ResponseEntity.ok(fogNodeService.getAllNodes());
    }

    /**
     * GET /api/fog-nodes/{nodeId}
     * Returns detailed telemetry and workload of a specific Fog Node.
     */
    @GetMapping(value = "/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FogNodeResponseDto> getNodeById(@PathVariable("nodeId") String nodeId) {
        return ResponseEntity.ok(fogNodeService.getNodeById(nodeId));
    }

    /**
     * GET /api/fog-nodes/{nodeId}/patients
     * Returns patients assigned to a specific Fog Node.
     */
    @GetMapping(value = "/{nodeId}/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientResponseDto>> getPatientsForNode(@PathVariable("nodeId") String nodeId) {
        return ResponseEntity.ok(fogNodeService.getPatientsForNode(nodeId));
    }

    /**
     * PUT /api/fog-nodes/{nodeId}/status?status=OFFLINE
     * Changes operational status of a Fog Node.
     * When set to OFFLINE, automatically executes dynamic failover for all managed patients.
     */
    @PutMapping(value = "/{nodeId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateNodeStatus(
            @PathVariable("nodeId") String nodeId,
            @RequestParam("status") String status) {
        Map<String, Object> result = fogNodeService.updateNodeStatus(nodeId, status);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/fog-nodes/ingest
     * Ingests biomedical sensor telemetry for local Fog Node processing.
     * Executes edge evaluation, calculates processing time, caches locally,
     * filters routine readings, or triggers immediate critical alerts.
     */
    @PostMapping(value = "/ingest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FogProcessingResultDto> ingestTelemetry(
            @Valid @RequestBody TelemetryIngestRequestDto requestDto) {
        FogProcessingResultDto result = fogProcessingService.processTelemetry(requestDto);
        HttpStatus status = "CRITICAL".equalsIgnoreCase(result.getHealthStatus()) 
                ? HttpStatus.ACCEPTED 
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * GET /api/fog-nodes/{nodeId}/cache?limit=20
     * Inspects local in-memory cache of recent readings at the Fog layer.
     */
    @GetMapping(value = "/{nodeId}/cache", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getLocalCache(
            @PathVariable("nodeId") String nodeId,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<HealthReading> cached = fogLocalCache.getRecentReadingsForNode(nodeId, limit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("fogNodeId", nodeId);
        response.put("cachedCount", cached.size());
        response.put("readings", cached);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/fog-nodes/rebalance
     * Invokes the Fog Load Balancer to re-optimize patient distribution across ONLINE nodes.
     */
    @PostMapping(value = "/rebalance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> rebalanceWorkload() {
        Map<String, Object> rebalanceResult = fogLoadBalancer.rebalanceAllPatients();
        return ResponseEntity.ok(rebalanceResult);
    }

    /**
     * POST /api/fog-nodes/assign?patientId=PAT-1001&nodeId=FOG-HOSPITAL-01
     * Explicitly assigns a patient to a target Fog Node.
     */
    @PostMapping(value = "/assign", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> assignPatient(
            @RequestParam("patientId") String patientId,
            @RequestParam("nodeId") String nodeId) {
        var updatedNode = fogLoadBalancer.assignPatientToNode(patientId, nodeId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("message", "Patient assigned successfully");
        response.put("patientId", patientId);
        response.put("assignedFogNodeId", updatedNode.getNodeId());
        response.put("nodeWorkload", updatedNode.getCurrentWorkload() + "/" + updatedNode.getMaxCapacity());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/fog-nodes/alerts
     * Retrieves emergency alerts generated across the Fog Computing Layer.
     */
    @GetMapping(value = "/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmergencyAlert>> getEmergencyAlerts() {
        return ResponseEntity.ok(emergencyAlertRepository.findAll());
    }

    /**
     * GET /api/fog-nodes/cache/stats
     * Returns aggregate cache statistics.
     */
    @GetMapping(value = "/cache/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(fogLocalCache.getCacheStats());
    }
}
