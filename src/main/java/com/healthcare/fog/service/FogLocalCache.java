package com.healthcare.fog.service;

import com.healthcare.fog.model.HealthReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Thread-safe In-Memory Fog Cache for ultra-fast local retrieval of proximate telemetry
 * without repeated disk/database roundtrips.
 */
@Component
public class FogLocalCache {

    private static final Logger log = LoggerFactory.getLogger(FogLocalCache.class);

    private static final int MAX_NODE_CACHE_SIZE = 100;
    private static final int MAX_PATIENT_CACHE_SIZE = 25;

    // Cache indexed by fogNodeId
    private final Map<String, Deque<HealthReading>> nodeReadingsCache = new ConcurrentHashMap<>();

    // Cache indexed by patientId
    private final Map<String, Deque<HealthReading>> patientReadingsCache = new ConcurrentHashMap<>();

    /**
     * Cache an ingested reading in the fog node's localized memory buffer.
     */
    public void cacheReading(HealthReading reading) {
        if (reading == null) return;

        // 1. Cache by Fog Node
        if (reading.getFogNodeId() != null) {
            Deque<HealthReading> nodeDeque = nodeReadingsCache.computeIfAbsent(
                    reading.getFogNodeId(), k -> new ConcurrentLinkedDeque<>()
            );
            nodeDeque.addFirst(reading);
            while (nodeDeque.size() > MAX_NODE_CACHE_SIZE) {
                nodeDeque.pollLast();
            }
        }

        // 2. Cache by Patient
        if (reading.getPatientId() != null) {
            Deque<HealthReading> patientDeque = patientReadingsCache.computeIfAbsent(
                    reading.getPatientId(), k -> new ConcurrentLinkedDeque<>()
            );
            patientDeque.addFirst(reading);
            while (patientDeque.size() > MAX_PATIENT_CACHE_SIZE) {
                patientDeque.pollLast();
            }
        }

        log.debug("Reading cached in local Fog cache for patient {} at node {}", 
                reading.getPatientId(), reading.getFogNodeId());
    }

    /**
     * Retrieve recent readings for a specific fog node.
     */
    public List<HealthReading> getRecentReadingsForNode(String fogNodeId, int limit) {
        Deque<HealthReading> deque = nodeReadingsCache.get(fogNodeId);
        if (deque == null || deque.isEmpty()) {
            return Collections.emptyList();
        }

        List<HealthReading> results = new ArrayList<>();
        int count = 0;
        for (HealthReading reading : deque) {
            results.add(reading);
            count++;
            if (count >= limit) break;
        }
        return results;
    }

    /**
     * Retrieve recent readings for a specific patient.
     */
    public List<HealthReading> getRecentReadingsForPatient(String patientId, int limit) {
        Deque<HealthReading> deque = patientReadingsCache.get(patientId);
        if (deque == null || deque.isEmpty()) {
            return Collections.emptyList();
        }

        List<HealthReading> results = new ArrayList<>();
        int count = 0;
        for (HealthReading reading : deque) {
            results.add(reading);
            count++;
            if (count >= limit) break;
        }
        return results;
    }

    /**
     * Clear cache for a specific node (e.g., upon shutdown or failover).
     */
    public void evictNodeCache(String fogNodeId) {
        nodeReadingsCache.remove(fogNodeId);
        log.info("Evicted local cache for Fog Node '{}'", fogNodeId);
    }

    /**
     * Get aggregate cache statistics.
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        int totalCachedReadings = 0;
        Map<String, Integer> perNodeCounts = new LinkedHashMap<>();

        for (Map.Entry<String, Deque<HealthReading>> entry : nodeReadingsCache.entrySet()) {
            int size = entry.getValue().size();
            perNodeCounts.put(entry.getKey(), size);
            totalCachedReadings += size;
        }

        stats.put("totalCachedReadings", totalCachedReadings);
        stats.put("cachedFogNodesCount", nodeReadingsCache.size());
        stats.put("cachedPatientsCount", patientReadingsCache.size());
        stats.put("perNodeDistribution", perNodeCounts);
        return stats;
    }

    public void clearAll() {
        nodeReadingsCache.clear();
        patientReadingsCache.clear();
    }
}
