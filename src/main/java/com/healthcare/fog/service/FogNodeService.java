package com.healthcare.fog.service;

import com.healthcare.fog.dto.FogNodeResponseDto;
import com.healthcare.fog.dto.PatientResponseDto;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FogNodeService {

    private static final Logger log = LoggerFactory.getLogger(FogNodeService.class);

    private final FogNodeRepository fogNodeRepository;
    private final PatientRepository patientRepository;
    private final FogLoadBalancer fogLoadBalancer;
    private final FogLocalCache fogLocalCache;
    private final PatientService patientService;

    public FogNodeService(FogNodeRepository fogNodeRepository,
                          PatientRepository patientRepository,
                          FogLoadBalancer fogLoadBalancer,
                          FogLocalCache fogLocalCache,
                          PatientService patientService) {
        this.fogNodeRepository = fogNodeRepository;
        this.patientRepository = patientRepository;
        this.fogLoadBalancer = fogLoadBalancer;
        this.fogLocalCache = fogLocalCache;
        this.patientService = patientService;
    }

    /**
     * Retrieve all Fog Nodes with live statistics.
     */
    public List<FogNodeResponseDto> getAllNodes() {
        return fogNodeRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single Fog Node by nodeId or database ID.
     */
    public FogNodeResponseDto getNodeById(String identifier) {
        FogNode node = findNodeByIdentifier(identifier);
        return mapToResponseDto(node);
    }

    /**
     * Retrieve all patients managed by a specific Fog Node.
     */
    public List<PatientResponseDto> getPatientsForNode(String identifier) {
        FogNode node = findNodeByIdentifier(identifier);
        return patientRepository.findByAssignedFogNodeId(node.getNodeId()).stream()
                .map(patientService::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Update Fog Node status (ONLINE, OFFLINE, MAINTENANCE, DEGRADED).
     * If transitioning to OFFLINE, triggers immediate automatic failover
     * and reassigns all dependent patients to surviving online nodes.
     */
    public Map<String, Object> updateNodeStatus(String nodeId, String newStatus) {
        FogNode node = findNodeByIdentifier(nodeId);
        String oldStatus = node.getStatus();
        String normalizedStatus = newStatus.trim().toUpperCase();

        node.setStatus(normalizedStatus);
        node.setLastHeartbeat(Instant.now());
        fogNodeRepository.save(node);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("nodeId", node.getNodeId());
        result.put("previousStatus", oldStatus);
        result.put("newStatus", normalizedStatus);

        if ("OFFLINE".equalsIgnoreCase(normalizedStatus)) {
            log.warn("FOG NODE FAILURE: Node '{}' marked OFFLINE. Executing automated patient failover...", node.getNodeId());
            
            // Invalidate local in-memory cache for the failed node
            fogLocalCache.evictNodeCache(node.getNodeId());

            // Locate all patients assigned to this failed node
            List<Patient> affectedPatients = patientRepository.findByAssignedFogNodeId(node.getNodeId());
            List<Map<String, String>> reassignmentLog = new ArrayList<>();

            for (Patient patient : affectedPatients) {
                // FogLoadBalancer automatically ignores OFFLINE nodes
                FogNode targetNode = fogLoadBalancer.selectBestNode(patient);
                fogLoadBalancer.assignPatientToNode(patient.getPatientId(), targetNode.getNodeId());

                Map<String, String> logEntry = new LinkedHashMap<>();
                logEntry.put("patientId", patient.getPatientId());
                logEntry.put("patientName", patient.getName());
                logEntry.put("reassignedTo", targetNode.getNodeId());
                logEntry.put("targetNodeType", targetNode.getNodeType());
                reassignmentLog.add(logEntry);
            }

            // Reset failed node workload
            node.setManagedPatientIds(new ArrayList<>());
            node.setCurrentWorkload(0);
            node.setActiveSensorConnections(0L);
            fogNodeRepository.save(node);

            result.put("failoverTriggered", true);
            result.put("affectedPatientsCount", affectedPatients.size());
            result.put("reassignments", reassignmentLog);
            log.info("Automated failover complete for node '{}'. {} patients successfully reassigned.",
                    node.getNodeId(), affectedPatients.size());
        } else {
            result.put("failoverTriggered", false);
            result.put("message", "Fog Node status updated successfully");
        }

        return result;
    }

    private FogNode findNodeByIdentifier(String identifier) {
        return fogNodeRepository.findByNodeId(identifier)
                .or(() -> fogNodeRepository.findById(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Fog Node not found with identifier: " + identifier));
    }

    public FogNodeResponseDto mapToResponseDto(FogNode node) {
        FogNodeResponseDto dto = new FogNodeResponseDto();
        dto.setId(node.getId());
        dto.setNodeId(node.getNodeId());
        dto.setName(node.getName());
        dto.setLocation(node.getLocation());
        dto.setIpAddress(node.getIpAddress());
        dto.setPort(node.getPort());
        dto.setStatus(node.getStatus());
        dto.setNodeType(node.getNodeType());
        dto.setMaxCapacity(node.getMaxCapacity());
        dto.setCurrentWorkload(node.getCurrentWorkload() != null ? node.getCurrentWorkload() : 0);

        int maxCap = (node.getMaxCapacity() != null && node.getMaxCapacity() > 0) ? node.getMaxCapacity() : 50;
        int load = (node.getCurrentWorkload() != null) ? node.getCurrentWorkload() : 0;
        dto.setWorkloadPercentage(Math.round(((double) load / maxCap * 100.0) * 10.0) / 10.0);

        dto.setCpuUsagePercent(node.getCpuUsagePercent());
        dto.setMemoryUsagePercent(node.getMemoryUsagePercent());
        dto.setActiveSensorConnections(node.getActiveSensorConnections());
        dto.setLastHeartbeat(node.getLastHeartbeat());

        dto.setProcessedReadingsCount(node.getProcessedReadingsCount() != null ? node.getProcessedReadingsCount() : 0L);
        dto.setFilteredReadingsCount(node.getFilteredReadingsCount() != null ? node.getFilteredReadingsCount() : 0L);
        dto.setCriticalAlertsCount(node.getCriticalAlertsCount() != null ? node.getCriticalAlertsCount() : 0L);
        dto.setAverageProcessingTimeMs(node.getAverageProcessingTimeMs() != null ? 
                Math.round(node.getAverageProcessingTimeMs() * 1000.0) / 1000.0 : 0.0);
        dto.setTotalProcessingTimeMs(node.getTotalProcessingTimeMs() != null ?
                Math.round(node.getTotalProcessingTimeMs() * 100.0) / 100.0 : 0.0);

        dto.setManagedPatientIds(node.getManagedPatientIds() != null ? node.getManagedPatientIds() : new ArrayList<>());
        return dto;
    }
}
