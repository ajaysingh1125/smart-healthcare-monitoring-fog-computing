package com.healthcare.fog.controller;

import com.healthcare.fog.dto.FogProcessingResultDto;
import com.healthcare.fog.dto.TelemetryIngestRequestDto;
import com.healthcare.fog.service.SensorSimulationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for Healthcare IoT Sensor Simulation Engine.
 * Allows starting, stopping, configuring and manually submitting biomedical sensor telemetry.
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SensorSimulationService simulationService;

    public SimulationController(SensorSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * POST /api/simulation/start?intervalMs=3000&mode=MIXED
     * Start automated scheduled healthcare sensor simulation.
     */
    @PostMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> startSimulation(
            @RequestParam(name = "intervalMs", defaultValue = "3000") Long intervalMs,
            @RequestParam(name = "mode", defaultValue = "MIXED") String mode) {
        Map<String, Object> status = simulationService.startSimulation(intervalMs, mode);
        return ResponseEntity.ok(status);
    }

    /**
     * POST /api/simulation/stop
     * Stop automated healthcare sensor simulation.
     */
    @PostMapping(value = "/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> stopSimulation() {
        Map<String, Object> status = simulationService.stopSimulation();
        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/simulation/status
     * Retrieve live operational status of the sensor simulation engine.
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(simulationService.getSimulationStatus());
    }

    /**
     * POST /api/simulation/generate?patientId=PAT-1001&scenario=CRITICAL
     * Triggers simulation of a single reading for a patient in a specified scenario (NORMAL, WARNING, CRITICAL, MIXED).
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FogProcessingResultDto> generateSingleReading(
            @RequestParam("patientId") String patientId,
            @RequestParam(name = "scenario", defaultValue = "MIXED") String scenario) {
        FogProcessingResultDto result = simulationService.generateSingleReading(patientId, scenario);
        HttpStatus status = "CRITICAL".equalsIgnoreCase(result.getHealthStatus()) 
                ? HttpStatus.ACCEPTED 
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * POST /api/simulation/submit-reading
     * Manual health-reading submission through the Fog Computing pipeline.
     */
    @PostMapping(value = "/submit-reading", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FogProcessingResultDto> submitManualReading(
            @Valid @RequestBody TelemetryIngestRequestDto requestDto) {
        FogProcessingResultDto result = simulationService.submitManualReading(requestDto);
        HttpStatus status = "CRITICAL".equalsIgnoreCase(result.getHealthStatus()) 
                ? HttpStatus.ACCEPTED 
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * GET /api/simulation/history?limit=20
     * Retrieve recent simulation output stream.
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FogProcessingResultDto>> getHistory(
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(simulationService.getRecentSimulationHistory(limit));
    }
}
