package com.healthcare.fog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Phase 7: Fog vs Cloud Performance Comparison Experiment document.
 * Stores controlled experiment results comparing two IoT data processing architectures
 * on an identical synthetic dataset.
 *
 * MEASUREMENT TRANSPARENCY:
 *   - Java processing times → MEASURED with System.nanoTime()
 *   - Network latency values → SIMULATED ESTIMATES (documented in methodologyNote)
 *   - Bandwidth figures     → SIMULATED ESTIMATES based on payload size constants
 */
@Document(collection = "comparison_experiments")
public class ComparisonExperiment {

    @Id
    private String id;

    @Indexed(sparse = true)
    private String experimentId;        // e.g. COMP-0001

    private Instant timestamp;
    private int datasetSize;
    private String status;              // RUNNING | COMPLETED | FAILED
    private String methodologyNote;

    private ModeMetrics cloudOnlyMetrics;
    private ModeMetrics fogBasedMetrics;
    private ComparisonSummary summary;

    // =========================================================
    // Nested: Per-Mode Metrics
    // =========================================================
    public static class ModeMetrics {
        private String mode;                            // CLOUD_ONLY or FOG_BASED
        private int totalReadings;
        private int totalCloudTransmissions;
        private int locallyFilteredReadings;
        private double filterPercentage;

        // --- ACTUAL measured Java nanoTime values ---
        private long totalJavaProcessingNs;
        private double avgJavaProcessingNs;
        private double totalJavaProcessingMs;
        private double avgJavaProcessingMs;

        // --- SIMULATED network/bandwidth estimates (NOT measured) ---
        private long estimatedTotalNetworkLatencyMs;
        private double estimatedAvgNetworkLatencyMsPerReading;
        private long estimatedBandwidthBytes;
        private double estimatedBandwidthKB;

        private int normalCount;
        private int warningCount;
        private int criticalCount;

        public String getMode() { return mode; }
        public void setMode(String v) { mode = v; }
        public int getTotalReadings() { return totalReadings; }
        public void setTotalReadings(int v) { totalReadings = v; }
        public int getTotalCloudTransmissions() { return totalCloudTransmissions; }
        public void setTotalCloudTransmissions(int v) { totalCloudTransmissions = v; }
        public int getLocallyFilteredReadings() { return locallyFilteredReadings; }
        public void setLocallyFilteredReadings(int v) { locallyFilteredReadings = v; }
        public double getFilterPercentage() { return filterPercentage; }
        public void setFilterPercentage(double v) { filterPercentage = v; }
        public long getTotalJavaProcessingNs() { return totalJavaProcessingNs; }
        public void setTotalJavaProcessingNs(long v) { totalJavaProcessingNs = v; }
        public double getAvgJavaProcessingNs() { return avgJavaProcessingNs; }
        public void setAvgJavaProcessingNs(double v) { avgJavaProcessingNs = v; }
        public double getTotalJavaProcessingMs() { return totalJavaProcessingMs; }
        public void setTotalJavaProcessingMs(double v) { totalJavaProcessingMs = v; }
        public double getAvgJavaProcessingMs() { return avgJavaProcessingMs; }
        public void setAvgJavaProcessingMs(double v) { avgJavaProcessingMs = v; }
        public long getEstimatedTotalNetworkLatencyMs() { return estimatedTotalNetworkLatencyMs; }
        public void setEstimatedTotalNetworkLatencyMs(long v) { estimatedTotalNetworkLatencyMs = v; }
        public double getEstimatedAvgNetworkLatencyMsPerReading() { return estimatedAvgNetworkLatencyMsPerReading; }
        public void setEstimatedAvgNetworkLatencyMsPerReading(double v) { estimatedAvgNetworkLatencyMsPerReading = v; }
        public long getEstimatedBandwidthBytes() { return estimatedBandwidthBytes; }
        public void setEstimatedBandwidthBytes(long v) { estimatedBandwidthBytes = v; }
        public double getEstimatedBandwidthKB() { return estimatedBandwidthKB; }
        public void setEstimatedBandwidthKB(double v) { estimatedBandwidthKB = v; }
        public int getNormalCount() { return normalCount; }
        public void setNormalCount(int v) { normalCount = v; }
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int v) { warningCount = v; }
        public int getCriticalCount() { return criticalCount; }
        public void setCriticalCount(int v) { criticalCount = v; }
    }

    // =========================================================
    // Nested: Comparison Summary
    // =========================================================
    public static class ComparisonSummary {

        // Java processing times (MEASURED)
        private double cloudJavaProcessingMs;
        private double fogJavaProcessingMs;
        private double javaProcessingDifferenceMs;
        private String fasterProcessingMode;
        private String javaProcessingNote;

        // Network latency (SIMULATED estimates)
        private long cloudEstimatedNetworkLatencyMs;
        private long fogEstimatedNetworkLatencyMs;
        private long networkLatencySavingsMs;
        private double networkLatencySavingsPercent;

        // Bandwidth (SIMULATED estimates)
        private long cloudEstimatedBandwidthBytes;
        private long fogEstimatedBandwidthBytes;
        private long bandwidthSavingsBytes;
        private double bandwidthSavingsPercent;

        // Data routing
        private int totalReadings;
        private int cloudModeTransmissions;
        private int fogModeTransmissions;
        private int locallyFilteredByFog;
        private double fogFilterRatePercent;
        private String comparisonNote;

        public double getCloudJavaProcessingMs() { return cloudJavaProcessingMs; }
        public void setCloudJavaProcessingMs(double v) { cloudJavaProcessingMs = v; }
        public double getFogJavaProcessingMs() { return fogJavaProcessingMs; }
        public void setFogJavaProcessingMs(double v) { fogJavaProcessingMs = v; }
        public double getJavaProcessingDifferenceMs() { return javaProcessingDifferenceMs; }
        public void setJavaProcessingDifferenceMs(double v) { javaProcessingDifferenceMs = v; }
        public String getFasterProcessingMode() { return fasterProcessingMode; }
        public void setFasterProcessingMode(String v) { fasterProcessingMode = v; }
        public String getJavaProcessingNote() { return javaProcessingNote; }
        public void setJavaProcessingNote(String v) { javaProcessingNote = v; }
        public long getCloudEstimatedNetworkLatencyMs() { return cloudEstimatedNetworkLatencyMs; }
        public void setCloudEstimatedNetworkLatencyMs(long v) { cloudEstimatedNetworkLatencyMs = v; }
        public long getFogEstimatedNetworkLatencyMs() { return fogEstimatedNetworkLatencyMs; }
        public void setFogEstimatedNetworkLatencyMs(long v) { fogEstimatedNetworkLatencyMs = v; }
        public long getNetworkLatencySavingsMs() { return networkLatencySavingsMs; }
        public void setNetworkLatencySavingsMs(long v) { networkLatencySavingsMs = v; }
        public double getNetworkLatencySavingsPercent() { return networkLatencySavingsPercent; }
        public void setNetworkLatencySavingsPercent(double v) { networkLatencySavingsPercent = v; }
        public long getCloudEstimatedBandwidthBytes() { return cloudEstimatedBandwidthBytes; }
        public void setCloudEstimatedBandwidthBytes(long v) { cloudEstimatedBandwidthBytes = v; }
        public long getFogEstimatedBandwidthBytes() { return fogEstimatedBandwidthBytes; }
        public void setFogEstimatedBandwidthBytes(long v) { fogEstimatedBandwidthBytes = v; }
        public long getBandwidthSavingsBytes() { return bandwidthSavingsBytes; }
        public void setBandwidthSavingsBytes(long v) { bandwidthSavingsBytes = v; }
        public double getBandwidthSavingsPercent() { return bandwidthSavingsPercent; }
        public void setBandwidthSavingsPercent(double v) { bandwidthSavingsPercent = v; }
        public int getTotalReadings() { return totalReadings; }
        public void setTotalReadings(int v) { totalReadings = v; }
        public int getCloudModeTransmissions() { return cloudModeTransmissions; }
        public void setCloudModeTransmissions(int v) { cloudModeTransmissions = v; }
        public int getFogModeTransmissions() { return fogModeTransmissions; }
        public void setFogModeTransmissions(int v) { fogModeTransmissions = v; }
        public int getLocallyFilteredByFog() { return locallyFilteredByFog; }
        public void setLocallyFilteredByFog(int v) { locallyFilteredByFog = v; }
        public double getFogFilterRatePercent() { return fogFilterRatePercent; }
        public void setFogFilterRatePercent(double v) { fogFilterRatePercent = v; }
        public String getComparisonNote() { return comparisonNote; }
        public void setComparisonNote(String v) { comparisonNote = v; }
    }

    // =========================================================
    // ComparisonExperiment Getters/Setters
    // =========================================================
    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getExperimentId() { return experimentId; }
    public void setExperimentId(String v) { experimentId = v; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant v) { timestamp = v; }
    public int getDatasetSize() { return datasetSize; }
    public void setDatasetSize(int v) { datasetSize = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getMethodologyNote() { return methodologyNote; }
    public void setMethodologyNote(String v) { methodologyNote = v; }
    public ModeMetrics getCloudOnlyMetrics() { return cloudOnlyMetrics; }
    public void setCloudOnlyMetrics(ModeMetrics v) { cloudOnlyMetrics = v; }
    public ModeMetrics getFogBasedMetrics() { return fogBasedMetrics; }
    public void setFogBasedMetrics(ModeMetrics v) { fogBasedMetrics = v; }
    public ComparisonSummary getSummary() { return summary; }
    public void setSummary(ComparisonSummary v) { summary = v; }
}
