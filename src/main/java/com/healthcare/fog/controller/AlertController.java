package com.healthcare.fog.controller;

import com.healthcare.fog.dto.AbnormalityThresholds;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.service.AbnormalityDetectionService;
import com.healthcare.fog.service.EmergencyAlertService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * REST API Controller for Healthcare Abnormality Detection and Emergency Alert Management.
 * Provides endpoints for active alerts, historical auditing, clinician acknowledgement,
 * resolution, and academic threshold inspections.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final EmergencyAlertService alertService;
    private final AbnormalityDetectionService abnormalityDetectionService;

    public AlertController(EmergencyAlertService alertService,
                           AbnormalityDetectionService abnormalityDetectionService) {
        this.alertService = alertService;
        this.abnormalityDetectionService = abnormalityDetectionService;
    }

    /**
     * GET /api/alerts
     * Retrieve all alerts.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmergencyAlert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    /**
     * GET /api/alerts/active
     * Retrieve active (unresolved) emergency alerts.
     */
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmergencyAlert>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    /**
     * GET /api/alerts/history
     * Retrieve resolved historical emergency alerts.
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmergencyAlert>> getHistoricalAlerts() {
        return ResponseEntity.ok(alertService.getHistoricalAlerts());
    }

    /**
     * GET /api/alerts/{id}
     * Retrieve details of a specific alert by alertId or MongoDB ID.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmergencyAlert> getAlertById(@PathVariable("id") String id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    /**
     * PUT /api/alerts/{id}/acknowledge?clinicianName={NAME}&notes={NOTES}
     * Clinician acknowledgement of an emergency alert.
     */
    @PutMapping(value = "/{id}/acknowledge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmergencyAlert> acknowledgeAlert(
            @PathVariable("id") String id,
            @RequestParam("clinicianName") String clinicianName,
            @RequestParam(name = "notes", required = false) String notes) {
        EmergencyAlert acknowledged = alertService.acknowledgeAlert(id, clinicianName, notes);
        return ResponseEntity.ok(acknowledged);
    }

    /**
     * PUT /api/alerts/{id}/resolve?clinicianName={NAME}&notes={NOTES}
     * Clinician resolution of an emergency alert.
     * Note: Critical alerts strictly mandate explicit manual clinician authorization.
     */
    @PutMapping(value = "/{id}/resolve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmergencyAlert> resolveAlert(
            @PathVariable("id") String id,
            @RequestParam("clinicianName") String clinicianName,
            @RequestParam(name = "notes", required = false) String notes,
            @RequestParam(name = "manual", defaultValue = "true") boolean manual) {
        EmergencyAlert resolved = alertService.resolveAlert(id, clinicianName, notes, manual);
        return ResponseEntity.ok(resolved);
    }

    /**
     * GET /api/alerts/thresholds
     * Retrieve the configurable demonstration thresholds and academic disclaimer.
     */
    @GetMapping(value = "/thresholds", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getThresholds() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("academicNotice", AbnormalityThresholds.ACADEMIC_DISCLAIMER);
        response.put("thresholds", abnormalityDetectionService.getThresholds());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/alerts/thresholds
     * Update demonstration thresholds at runtime.
     */
    @PutMapping(value = "/thresholds", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateThresholds(@RequestBody AbnormalityThresholds newThresholds) {
        abnormalityDetectionService.updateThresholds(newThresholds);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", "SUCCESS");
        response.put("message", "Demonstration abnormality thresholds successfully updated");
        response.put("academicNotice", AbnormalityThresholds.ACADEMIC_DISCLAIMER);
        response.put("thresholds", abnormalityDetectionService.getThresholds());
        return ResponseEntity.ok(response);
    }
}
