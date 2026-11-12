package com.healthcare.fog.service;

import com.healthcare.fog.exception.ResourceNotFoundException;
import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.FogNodeRepository;
import com.healthcare.fog.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intelligent Fog Computing Load Balancer.
 * Dynamically routes patients and sensor streams to the optimal Fog Node
 * based on operational status, medical priority affinity, and available processing capacity.
 */
@Service
public class FogLoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(FogLoadBalancer.class);

    private final FogNodeRepository fogNodeRepository;
    private final PatientRepository patientRepository;

    public FogLoadBalancer(FogNodeRepository fogNodeRepository, PatientRepository patientRepository) {
        this.fogNodeRepository = fogNodeRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Selects the most suitable active Fog Node for a patient using clinical condition affinity
     * and dynamic least-loaded capacity heuristics.
     */
    public FogNode selectBestNode(Patient patient) {
        List<FogNode> onlineNodes = fogNodeRepository.findByStatus("ONLINE");
        if (onlineNodes.isEmpty()) {
            throw new IllegalStateException("CRITICAL: No active Fog Nodes available in the local network cluster!");
        }
        return selectBestNodeFromCandidates(patient, onlineNodes);
    }

    public FogNode selectBestNodeFromCandidates(Patient patient, List<FogNode> onlineNodes) {
        if (onlineNodes.isEmpty()) {
            throw new IllegalStateException("CRITICAL: No active Fog Nodes available in the local network cluster!");
        }

        // 1. Clinical Priority Affinity Check
        if (patient != null && patient.getMedicalHistory() != null && !patient.getMedicalHistory().isEmpty()) {
            String historyJoined = String.join(" ", patient.getMedicalHistory()).toLowerCase();

            // Emergency / Trauma / Stroke Affinity -> EMERGENCY FOG NODE
            if (historyJoined.contains("stroke") || historyJoined.contains("trauma") 
                    || historyJoined.contains("emergency") || historyJoined.contains("tachycardia")) {
                FogNode emergencyNode = findAvailableNodeByType(onlineNodes, "EMERGENCY");
                if (emergencyNode != null) {
                    log.debug("Routing acute patient {} to Emergency Fog Node", patient.getPatientId());
                    return emergencyNode;
                }
            }

            // Inpatient / High-Acuity Cardiac & ICU Affinity -> HOSPITAL FOG NODE
            if (historyJoined.contains("arrhythmia") || historyJoined.contains("heart failure") 
                    || historyJoined.contains("fibrillation") || historyJoined.contains("appendectomy")) {
                FogNode hospitalNode = findAvailableNodeByType(onlineNodes, "HOSPITAL");
                if (hospitalNode != null) {
                    log.debug("Routing high-acuity cardiac/surgical patient {} to Hospital Fog Node", patient.getPatientId());
                    return hospitalNode;
                }
            }

            // Outpatient / Chronic Disease / Ambulatory Affinity -> CLINIC FOG NODE
            if (historyJoined.contains("diabetes") || historyJoined.contains("asthma") 
                    || historyJoined.contains("sleep apnea") || historyJoined.contains("kidney")
                    || historyJoined.contains("hypertension") || historyJoined.contains("gestational")
                    || historyJoined.contains("hyperlipidemia")) {
                FogNode clinicNode = findAvailableNodeByType(onlineNodes, "CLINIC");
                if (clinicNode != null) {
                    log.debug("Routing ambulatory chronic disease patient {} to Clinic Fog Node", patient.getPatientId());
                    return clinicNode;
                }
            }
        }

        // 2. Dynamic Least-Loaded Heuristic (Load Balancing by Workload Ratio)
        return onlineNodes.stream()
                .min(Comparator.comparingDouble((FogNode node) -> {
                    int cap = (node.getMaxCapacity() != null && node.getMaxCapacity() > 0) ? node.getMaxCapacity() : 50;
                    int load = (node.getCurrentWorkload() != null) ? node.getCurrentWorkload() : 0;
                    return (double) load / cap;
                }))
                .orElse(onlineNodes.get(0));
    }

    private FogNode findAvailableNodeByType(List<FogNode> onlineNodes, String type) {
        return onlineNodes.stream()
                .filter(n -> type.equalsIgnoreCase(n.getNodeType()))
                .filter(n -> n.getCurrentWorkload() == null || n.getCurrentWorkload() < n.getMaxCapacity())
                .findFirst()
                .orElse(null);
    }

    /**
     * Assigns a patient to a specified Fog Node, managing workload counters.
     */
    public FogNode assignPatientToNode(String patientId, String targetNodeId) {
        Patient patient = patientRepository.findByPatientId(patientId)
                .or(() -> patientRepository.findById(patientId))
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));

        FogNode targetNode = fogNodeRepository.findByNodeId(targetNodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fog Node not found: " + targetNodeId));

        if (!"ONLINE".equalsIgnoreCase(targetNode.getStatus())) {
            throw new IllegalStateException("Cannot assign patient to offline/inactive Fog Node: " + targetNodeId);
        }

        String previousNodeId = patient.getAssignedFogNodeId();
        if (previousNodeId != null && !previousNodeId.equalsIgnoreCase(targetNodeId)) {
            fogNodeRepository.findByNodeId(previousNodeId).ifPresent(oldNode -> {
                if (oldNode.getManagedPatientIds() != null) {
                    oldNode.getManagedPatientIds().remove(patient.getPatientId());
                    oldNode.setCurrentWorkload(oldNode.getManagedPatientIds().size());
                    fogNodeRepository.save(oldNode);
                }
            });
        }

        if (targetNode.getManagedPatientIds() == null) {
            targetNode.setManagedPatientIds(new ArrayList<>());
        }
        if (!targetNode.getManagedPatientIds().contains(patient.getPatientId())) {
            targetNode.getManagedPatientIds().add(patient.getPatientId());
        }
        targetNode.setCurrentWorkload(targetNode.getManagedPatientIds().size());
        targetNode.setLastHeartbeat(Instant.now());
        FogNode updatedNode = fogNodeRepository.save(targetNode);

        patient.setAssignedFogNodeId(targetNode.getNodeId());
        patientRepository.save(patient);

        log.info("Assigned patient {} to Fog Node {} (current workload: {}/{})",
                patient.getPatientId(), targetNode.getNodeId(),
                targetNode.getCurrentWorkload(), targetNode.getMaxCapacity());

        return updatedNode;
    }

    /**
     * Automatic Load Rebalancing across all ONLINE fog nodes.
     */
    public Map<String, Object> rebalanceAllPatients() {
        List<FogNode> onlineNodes = fogNodeRepository.findByStatus("ONLINE");
        if (onlineNodes.isEmpty()) {
            throw new IllegalStateException("CRITICAL: Cannot rebalance patients. No active Fog Nodes online.");
        }

        List<Patient> allPatients = patientRepository.findAll();
        log.info("Initiating Fog Load Balancer rebalance for {} patients across {} online nodes",
                allPatients.size(), onlineNodes.size());

        // Reset managed lists for online nodes
        for (FogNode node : onlineNodes) {
            node.setManagedPatientIds(new ArrayList<>());
            node.setCurrentWorkload(0);
        }

        for (Patient patient : allPatients) {
            FogNode selectedNode = selectBestNodeFromCandidates(patient, onlineNodes);
            if (selectedNode.getManagedPatientIds() == null) {
                selectedNode.setManagedPatientIds(new ArrayList<>());
            }
            selectedNode.getManagedPatientIds().add(patient.getPatientId());
            selectedNode.setCurrentWorkload(selectedNode.getManagedPatientIds().size());

            patient.setAssignedFogNodeId(selectedNode.getNodeId());
            patientRepository.save(patient);
        }

        // Persist updated workloads to MongoDB
        List<FogNode> savedNodes = fogNodeRepository.saveAll(onlineNodes);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("totalPatientsRebalanced", allPatients.size());
        result.put("onlineNodesCount", savedNodes.size());
        result.put("nodeWorkloadDistribution", savedNodes.stream().collect(
                Collectors.toMap(FogNode::getNodeId, n -> n.getCurrentWorkload() + "/" + n.getMaxCapacity())
        ));

        return result;
    }

    /**
     * Synchronize actual database assignments to FogNode managed counts.
     */
    public void syncNodeWorkloads() {
        List<FogNode> nodes = fogNodeRepository.findAll();
        for (FogNode node : nodes) {
            List<Patient> assigned = patientRepository.findByAssignedFogNodeId(node.getNodeId());
            List<String> patientIds = assigned.stream().map(Patient::getPatientId).collect(Collectors.toList());
            node.setManagedPatientIds(patientIds);
            node.setCurrentWorkload(patientIds.size());
            fogNodeRepository.save(node);
        }
    }
}
