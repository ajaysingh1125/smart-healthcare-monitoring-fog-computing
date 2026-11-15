package com.healthcare.fog.service;

import com.healthcare.fog.dto.AbnormalityAssessment;
import com.healthcare.fog.exception.ResourceNotFoundException;
import com.healthcare.fog.model.EmergencyAlert;
import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.EmergencyAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * Service managing the lifecycle of Emergency Medical Alerts in the Fog Computing Layer:
 * - Generation upon critical telemetry anomaly detection
 * - Immediate cloud escalation flag
 * - Clinician acknowledgement
 * - Clinician manual resolution (CRITICAL alerts strictly protected against auto-resolution)
 * - Historical query auditing
 */
@Service
public class EmergencyAlertService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyAlertService.class);

    private final EmergencyAlertRepository alertRepository;
    private final Random random = new Random();

    public EmergencyAlertService(EmergencyAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Create and store an emergency alert in MongoDB with complete patient and fog node context.
     */
    public EmergencyAlert createEmergencyAlert(Patient patient, FogNode fogNode,
                                              String sensorDeviceId, AbnormalityAssessment assessment) {
        String alertId = "ALT-" + System.currentTimeMillis() % 1000000 + "-" + (1000 + random.nextInt(9000));

        EmergencyAlert alert = new EmergencyAlert(
                alertId,
                patient != null ? patient.getPatientId() : "UNKNOWN",
                patient != null ? patient.getName() : "Unknown Patient",
                (patient != null && patient.getAge() != null) ? patient.getAge() : Integer.valueOf(0),
                patient != null ? patient.getGender() : "UNKNOWN",
                fogNode != null ? fogNode.getNodeId() : "UNKNOWN_FOG",
                fogNode != null ? fogNode.getName() : "Unknown Fog Gateway",
                sensorDeviceId != null ? sensorDeviceId : "SENS-UNKNOWN",
                assessment.getStatus(),
                assessment.getLeadingVitalType(),
                assessment.getMeasuredValue(),
                assessment.getThresholdLimit(),
                assessment.getThresholdExceeded(),
                assessment.getDiagnosticMessage()
        );

        // Immediate Cloud Forwarding flag
        alert.setForwardedToCloud(true);

        EmergencyAlert saved = alertRepository.save(alert);
        log.warn("EMERGENCY ALERT GENERATED: [{}] Level: {} | Patient: {} | Param: {} | Node: {}",
                saved.getAlertId(), saved.getAlertLevel(), saved.getPatientId(),
                saved.getVitalType(), saved.getFogNodeId());

        return saved;
    }

    /**
     * Retrieve all active (unresolved) alerts.
     */
    public List<EmergencyAlert> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByTimestampDesc();
    }

    /**
     * Retrieve historical resolved alerts.
     */
    public List<EmergencyAlert> getHistoricalAlerts() {
        return alertRepository.findByResolvedTrueOrderByTimestampDesc();
    }

    /**
     * Retrieve all alerts.
     */
    public List<EmergencyAlert> getAllAlerts() {
        return alertRepository.findAll();
    }

    /**
     * Retrieve a specific alert by alertId or MongoDB ID.
     */
    public EmergencyAlert getAlertById(String identifier) {
        return alertRepository.findByAlertId(identifier)
                .or(() -> alertRepository.findById(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Emergency Alert not found with identifier: " + identifier));
    }

    /**
     * Acknowledge an emergency alert.
     */
    public EmergencyAlert acknowledgeAlert(String alertIdentifier, String clinicianName, String notes) {
        EmergencyAlert alert = getAlertById(alertIdentifier);

        if (clinicianName == null || clinicianName.trim().isBlank()) {
            throw new IllegalArgumentException("Clinician name is required to acknowledge an emergency alert");
        }

        alert.setAcknowledged(true);
        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledgedAt(Instant.now());
        alert.setAcknowledgedBy(clinicianName.trim());
        alert.setAcknowledgementNotes(notes != null ? notes.trim() : "Acknowledged by clinical staff");

        EmergencyAlert updated = alertRepository.save(alert);
        log.info("Alert [{}] acknowledged by clinician '{}'", updated.getAlertId(), clinicianName);
        return updated;
    }

    /**
     * Resolve an emergency alert.
     * REQUIREMENT: Critical alerts CANNOT be automatically resolved.
     * They mandate explicit manual clinician authorization.
     */
    public EmergencyAlert resolveAlert(String alertIdentifier, String clinicianName,
                                       String notes, boolean isManualClinicianAction) {
        EmergencyAlert alert = getAlertById(alertIdentifier);

        // Enforcement of Requirement 10: Critical alerts cannot be automatically resolved
        if ("CRITICAL".equalsIgnoreCase(alert.getAlertLevel())) {
            if (!isManualClinicianAction || clinicianName == null || clinicianName.trim().isBlank()) {
                log.error("ATTEMPT BLOCKED: Automatic or unverified resolution of CRITICAL alert [{}] rejected.",
                        alert.getAlertId());
                throw new IllegalStateException("SAFETY POLICY VIOLATION: CRITICAL Emergency Alert [" +
                        alert.getAlertId() + "] cannot be automatically resolved. Explicit manual clinician " +
                        "resolution with valid clinician identification is mandatory.");
            }
        }

        alert.setResolved(true);
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(Instant.now());
        alert.setResolvedBy(clinicianName != null ? clinicianName.trim() : "SYSTEM");
        alert.setResolutionNotes(notes != null ? notes.trim() : "Alert marked resolved following clinical assessment");

        EmergencyAlert updated = alertRepository.save(alert);
        log.info("Alert [{}] resolved by clinician '{}'", updated.getAlertId(), alert.getResolvedBy());
        return updated;
    }
}
