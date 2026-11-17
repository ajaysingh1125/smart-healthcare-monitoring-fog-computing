package com.healthcare.fog.service;

import com.healthcare.fog.model.ComparisonExperiment;
import com.healthcare.fog.model.ComparisonExperiment.ComparisonSummary;
import com.healthcare.fog.model.ComparisonExperiment.ModeMetrics;
import com.healthcare.fog.repository.ComparisonExperimentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Phase 7: FogCloudComparisonService
 *
 * <p>Runs controlled performance comparison experiments between two IoT data processing architectures:
 * <ul>
 *   <li><b>MODE A – CLOUD_ONLY:</b> Every sensor reading is immediately serialized and forwarded to the
 *       cloud for processing. No local filtering, no edge intelligence.</li>
 *   <li><b>MODE B – FOG_BASED:</b> Readings are processed locally at the fog node first. Normal readings
 *       are filtered and aggregated locally; only WARNING/CRITICAL readings are forwarded to the cloud.</li>
 * </ul>
 *
 * <p>Both modes process <b>the same synthetic dataset</b> generated with a fixed random seed for
 * reproducibility and fairness.
 *
 * <p><b>MEASUREMENT TRANSPARENCY (Academic Integrity):</b>
 * <ul>
 *   <li>Java processing times are <b>ACTUAL measurements</b> using {@code System.nanoTime()}.</li>
 *   <li>Network latency figures are <b>SIMULATED ESTIMATES</b> based on representative IoT cloud
 *       deployment constants (see {@link #CLOUD_RTT_MS}). They do NOT represent measured network calls.</li>
 *   <li>Bandwidth figures are <b>SIMULATED ESTIMATES</b> based on a fixed reading payload size.</li>
 * </ul>
 */
@Service
public class FogCloudComparisonService {

    // =========================================================
    // Public constants (used by ComparisonController for /methodology endpoint)
    // =========================================================

    /** Representative cloud datacenter WAN round-trip latency estimate (ms). NOT measured. */
    public static final long CLOUD_RTT_MS = 180L;

    /**
     * When a fog node forwards a reading to the cloud it traverses the same WAN link.
     * Same constant used for fair comparison – fog forwarding does NOT get a latency bonus.
     */
    public static final long FOG_FORWARD_RTT_MS = 180L;

    /** Estimated JSON payload size per health reading (bytes). */
    public static final int PAYLOAD_BYTES = 512;

    public static final String ACADEMIC_DISCLAIMER =
            "ACADEMIC DISCLAIMER: This experiment is designed for college project demonstration. " +
            "Network latency and bandwidth values are simulated estimates using representative constants " +
            "documented in this response. Only Java processing times are actual measurements. " +
            "Results are reproducible (fixed seed=42). " +
            "This does not constitute a production network benchmark.";

    // Internal node and patient ID pools
    private static final String[] NODE_IDS   = {"FOG-HOSPITAL-01", "FOG-CLINIC-01", "FOG-EMERGENCY-01"};
    private static final String[] PATIENT_IDS = {
        "PAT-1001","PAT-1002","PAT-1003","PAT-1004","PAT-1005",
        "PAT-1006","PAT-1007","PAT-1008","PAT-1009","PAT-1010"
    };

    // Detection thresholds (mirror AbnormalityThresholds defaults)
    private static final double HR_CRIT_LOW  = 50.0,  HR_WARN_LOW  = 60.0;
    private static final double HR_WARN_HIGH = 100.0, HR_CRIT_HIGH = 120.0;
    private static final double SPO2_CRIT    = 90.0,  SPO2_WARN    = 95.0;
    private static final double TEMP_CL      = 35.5,  TEMP_WL      = 36.5;
    private static final double TEMP_WH      = 37.5,  TEMP_CH      = 38.5;
    private static final double SYS_CL       = 85.0,  SYS_WL       = 90.0;
    private static final double SYS_WH       = 120.0, SYS_CH       = 140.0;
    private static final double DIA_CL       = 55.0,  DIA_WL       = 60.0;
    private static final double DIA_WH       = 80.0,  DIA_CH       = 90.0;

    // =========================================================
    private final ComparisonExperimentRepository repository;
    private final AtomicInteger counter = new AtomicInteger(0);

    public FogCloudComparisonService(ComparisonExperimentRepository repository) {
        this.repository = repository;
        this.counter.set((int) repository.count());
    }

    // =========================================================
    // Public API
    // =========================================================

    public ComparisonExperiment runComparison(int datasetSize) {
        String experimentId = String.format("COMP-%04d", counter.incrementAndGet());

        ComparisonExperiment experiment = new ComparisonExperiment();
        experiment.setExperimentId(experimentId);
        experiment.setTimestamp(Instant.now());
        experiment.setDatasetSize(datasetSize);
        experiment.setStatus("RUNNING");
        experiment.setMethodologyNote(buildMethodologyNote());
        experiment = repository.save(experiment);

        try {
            // 1. Generate reproducible dataset — same input for both modes
            List<SyntheticReading> dataset = generateDataset(datasetSize);

            // 2. Run Mode A: Cloud-Only (all readings forwarded, simple processing)
            ModeMetrics cloudMetrics = runCloudOnlyMode(dataset);

            // 3. Run Mode B: Fog-Based (local filtering + selective forwarding)
            ModeMetrics fogMetrics = runFogBasedMode(dataset);

            // 4. Build cross-mode comparison summary
            ComparisonSummary summary = buildSummary(cloudMetrics, fogMetrics, datasetSize);

            experiment.setCloudOnlyMetrics(cloudMetrics);
            experiment.setFogBasedMetrics(fogMetrics);
            experiment.setSummary(summary);
            experiment.setStatus("COMPLETED");

        } catch (Exception e) {
            experiment.setStatus("FAILED");
            experiment.setMethodologyNote("Experiment FAILED: " + e.getMessage());
        }

        return repository.save(experiment);
    }

    public Optional<ComparisonExperiment> getLatestResult() {
        return repository.findFirstByOrderByTimestampDesc();
    }

    public List<ComparisonExperiment> getAllResults() {
        return repository.findAllByOrderByTimestampDesc();
    }

    public Optional<ComparisonExperiment> getByExperimentId(String experimentId) {
        return repository.findByExperimentId(experimentId);
    }

    // =========================================================
    // MODE A: CLOUD-ONLY Processing
    // =========================================================
    /**
     * Simulates cloud-only architecture: every reading is immediately classified and
     * "transmitted" to the cloud. No local caching, no filtering.
     *
     * <p>Java processing per reading:
     * <ol>
     *   <li>Field access (simulates deserialization)</li>
     *   <li>Severity classification (5 vital comparisons)</li>
     *   <li>Build cloud payload map (simulates JSON serialization overhead)</li>
     * </ol>
     * All three steps are within the nanoTime measurement window.
     */
    private ModeMetrics runCloudOnlyMode(List<SyntheticReading> dataset) {
        long totalNs = 0L;
        int normal = 0, warning = 0, critical = 0;

        for (SyntheticReading r : dataset) {
            long t0 = System.nanoTime();

            // Step 1: Access vitals (deserialization simulation)
            double hr   = r.heartRate;
            double temp = r.temperature;
            double spo2 = r.spo2;
            double sys  = r.systolicBp;
            double dia  = r.diastolicBp;

            // Step 2: Classify (same logic as fog; ensures fair comparison of routing, not detection)
            String severity = classifySeverity(hr, temp, spo2, sys, dia);

            // Step 3: Build cloud payload (simulates JSON serialization before transmission)
            Map<String, Object> payload = new LinkedHashMap<>(12);
            payload.put("patientId",   r.patientId);
            payload.put("fogNodeId",   r.fogNodeId);
            payload.put("heartRate",   hr);
            payload.put("temperature", temp);
            payload.put("spo2",        spo2);
            payload.put("systolicBp",  sys);
            payload.put("diastolicBp", dia);
            payload.put("severity",    severity);
            payload.put("timestamp",   r.timestamp.toString());
            payload.put("destination", "CLOUD_DIRECT");

            totalNs += System.nanoTime() - t0;

            switch (severity) {
                case "WARNING"  -> warning++;
                case "CRITICAL" -> critical++;
                default         -> normal++;
            }
        }

        // Cloud-only: ALL readings are transmitted, NONE filtered locally
        int total         = dataset.size();
        int transmitted   = total;
        int filtered      = 0;
        long estNetworkMs = (long) transmitted * CLOUD_RTT_MS;
        long estBwBytes   = (long) transmitted * PAYLOAD_BYTES;

        return buildModeMetrics("CLOUD_ONLY", total, transmitted, filtered,
                totalNs, estNetworkMs, estBwBytes, normal, warning, critical);
    }

    // =========================================================
    // MODE B: FOG-BASED Processing
    // =========================================================
    /**
     * Simulates fog-based architecture: readings are processed locally at the edge node.
     * NORMAL readings are filtered and aggregated locally (not forwarded).
     * WARNING/CRITICAL readings are forwarded to the cloud immediately.
     *
     * <p>Java processing per reading (more work than cloud-only):
     * <ol>
     *   <li>Field access</li>
     *   <li>Severity classification (same 5-vital check)</li>
     *   <li>Local ring-buffer cache write (simulates fog node edge cache)</li>
     *   <li>Routing decision: build forward payload OR update local aggregate</li>
     * </ol>
     * Steps 3 and 4 are ADDITIONAL work beyond cloud-only, so fog Java processing
     * time per reading will be slightly higher. This is accurate and expected.
     */
    private ModeMetrics runFogBasedMode(List<SyntheticReading> dataset) {
        long totalNs = 0L;
        int normal = 0, warning = 0, critical = 0;
        int cloudTransmissions = 0;
        int locallyFiltered    = 0;

        // Bounded ring-buffer per fog node (simulates actual FogLocalCache)
        Map<String, Deque<Double>> nodeBuffers = new HashMap<>();
        for (String nodeId : NODE_IDS) {
            nodeBuffers.put(nodeId, new ArrayDeque<>(50));
        }
        // Local aggregation stats per node (simulates aggregate-before-forward logic)
        Map<String, double[]> nodeAggregates = new HashMap<>();
        for (String nodeId : NODE_IDS) {
            nodeAggregates.put(nodeId, new double[]{0.0, 0}); // [sumHr, count]
        }

        for (SyntheticReading r : dataset) {
            long t0 = System.nanoTime();

            // Step 1: Access vitals
            double hr   = r.heartRate;
            double temp = r.temperature;
            double spo2 = r.spo2;
            double sys  = r.systolicBp;
            double dia  = r.diastolicBp;

            // Step 2: Classify severity (identical logic to cloud-only for fair comparison)
            String severity = classifySeverity(hr, temp, spo2, sys, dia);

            // Step 3: Write to local edge ring-buffer cache (additional fog work)
            Deque<Double> buffer = nodeBuffers.get(r.fogNodeId);
            if (buffer != null) {
                if (buffer.size() >= 50) buffer.pollFirst();   // evict oldest
                buffer.addLast(hr);
            }

            // Step 4: Routing decision (additional fog work)
            if ("CRITICAL".equals(severity) || "WARNING".equals(severity)) {
                // Build forwarding payload for cloud transmission
                Map<String, Object> fwdPayload = new LinkedHashMap<>(10);
                fwdPayload.put("PRIORITY",   severity.equals("CRITICAL") ? "URGENT" : "HIGH");
                fwdPayload.put("patientId",  r.patientId);
                fwdPayload.put("fogNodeId",  r.fogNodeId);
                fwdPayload.put("heartRate",  hr);
                fwdPayload.put("spo2",       spo2);
                fwdPayload.put("severity",   severity);
                fwdPayload.put("timestamp",  r.timestamp.toString());
                cloudTransmissions++;
            } else {
                // NORMAL: aggregate locally, skip cloud transmission
                double[] agg = nodeAggregates.get(r.fogNodeId);
                if (agg != null) {
                    agg[0] += hr;   // accumulate HR sum
                    agg[1] += 1;    // increment count
                }
                locallyFiltered++;
            }

            totalNs += System.nanoTime() - t0;

            switch (severity) {
                case "WARNING"  -> warning++;
                case "CRITICAL" -> critical++;
                default         -> normal++;
            }
        }

        int total         = dataset.size();
        long estNetworkMs = (long) cloudTransmissions * FOG_FORWARD_RTT_MS;
        long estBwBytes   = (long) cloudTransmissions * PAYLOAD_BYTES;

        return buildModeMetrics("FOG_BASED", total, cloudTransmissions, locallyFiltered,
                totalNs, estNetworkMs, estBwBytes, normal, warning, critical);
    }

    // =========================================================
    // Shared severity classification — IDENTICAL for both modes
    // (Same thresholds as AbnormalityDetectionService defaults)
    // =========================================================
    private String classifySeverity(double hr, double temp, double spo2, double sys, double dia) {
        if (hr < HR_CRIT_LOW || hr > HR_CRIT_HIGH) return "CRITICAL";
        if (spo2 < SPO2_CRIT)                       return "CRITICAL";
        if (temp < TEMP_CL || temp > TEMP_CH)        return "CRITICAL";
        if (sys  < SYS_CL  || sys  > SYS_CH)         return "CRITICAL";
        if (dia  < DIA_CL  || dia  > DIA_CH)         return "CRITICAL";

        if (hr   < HR_WARN_LOW  || hr   > HR_WARN_HIGH)  return "WARNING";
        if (spo2 < SPO2_WARN)                             return "WARNING";
        if (temp < TEMP_WL      || temp > TEMP_WH)        return "WARNING";
        if (sys  < SYS_WL       || sys  > SYS_WH)         return "WARNING";
        if (dia  < DIA_WL       || dia  > DIA_WH)         return "WARNING";

        return "NORMAL";
    }

    // =========================================================
    // Dataset Generation — fixed seed ensures reproducibility
    // =========================================================
    private List<SyntheticReading> generateDataset(int size) {
        Random rng = new Random(42L);  // fixed seed — same dataset for every same size
        List<SyntheticReading> dataset = new ArrayList<>(size);

        Instant base = Instant.now();
        for (int i = 0; i < size; i++) {
            String scenario = pickScenario(rng);    // 60% NORMAL, 25% WARNING, 15% CRITICAL
            String patientId = PATIENT_IDS[i % PATIENT_IDS.length];
            String fogNodeId = NODE_IDS[i % NODE_IDS.length];
            Instant ts = base.minusSeconds(size - i);
            dataset.add(generateReading(rng, scenario, patientId, fogNodeId, ts));
        }
        return dataset;
    }

    private String pickScenario(Random rng) {
        int roll = rng.nextInt(100);
        if (roll < 60) return "NORMAL";
        if (roll < 85) return "WARNING";
        return "CRITICAL";
    }

    private SyntheticReading generateReading(Random rng, String scenario,
                                             String patientId, String fogNodeId,
                                             Instant timestamp) {
        double hr, temp, spo2, sys, dia;
        // Start with baseline normal values
        hr   = 68.0 + rng.nextDouble() * 24.0;   // 68-92
        temp = 36.3 + rng.nextDouble() * 0.9;    // 36.3-37.2
        spo2 = 96.0 + rng.nextDouble() * 3.5;    // 96-99.5
        sys  = 100.0 + rng.nextDouble() * 18.0;  // 100-118
        dia  = 62.0 + rng.nextDouble() * 15.0;   // 62-77

        if ("WARNING".equals(scenario)) {
            int v = rng.nextInt(5);
            switch (v) {
                case 0 -> hr   = 101.0 + rng.nextDouble() * 15.0;   // 101-116
                case 1 -> temp = 37.6 + rng.nextDouble() * 0.8;     // 37.6-38.4
                case 2 -> spo2 = 91.0 + rng.nextDouble() * 3.5;     // 91-94.5
                case 3 -> sys  = 121.0 + rng.nextDouble() * 16.0;   // 121-137
                case 4 -> dia  = 81.0 + rng.nextDouble() * 8.0;     // 81-89
            }
        } else if ("CRITICAL".equals(scenario)) {
            int v = rng.nextInt(5);
            switch (v) {
                case 0 -> hr   = rng.nextBoolean()
                                 ? 121.0 + rng.nextDouble() * 50.0   // 121-171 (high critical)
                                 : 25.0  + rng.nextDouble() * 24.0;  // 25-49 (low critical)
                case 1 -> temp = 38.6 + rng.nextDouble() * 2.5;     // 38.6-41.1
                case 2 -> spo2 = 72.0 + rng.nextDouble() * 17.0;    // 72-89
                case 3 -> sys  = rng.nextBoolean()
                                 ? 141.0 + rng.nextDouble() * 60.0   // 141-201
                                 : 40.0  + rng.nextDouble() * 44.0;  // 40-84
                case 4 -> dia  = rng.nextBoolean()
                                 ? 91.0  + rng.nextDouble() * 30.0   // 91-121
                                 : 20.0  + rng.nextDouble() * 34.0;  // 20-54
            }
        }

        return new SyntheticReading(patientId, fogNodeId,
                round1(hr), round1(temp), round1(spo2), round1(sys), round1(dia),
                scenario, timestamp);
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    // =========================================================
    // Builder helpers
    // =========================================================
    private ModeMetrics buildModeMetrics(String mode, int total, int transmitted, int filtered,
                                          long totalNs, long estNetworkMs, long estBwBytes,
                                          int normal, int warning, int critical) {
        ModeMetrics m = new ModeMetrics();
        m.setMode(mode);
        m.setTotalReadings(total);
        m.setTotalCloudTransmissions(transmitted);
        m.setLocallyFilteredReadings(filtered);
        m.setFilterPercentage(total > 0 ? round2(filtered * 100.0 / total) : 0.0);

        m.setTotalJavaProcessingNs(totalNs);
        m.setAvgJavaProcessingNs(total > 0 ? round2((double) totalNs / total) : 0.0);
        m.setTotalJavaProcessingMs(round2(totalNs / 1_000_000.0));
        m.setAvgJavaProcessingMs(total > 0 ? round4(totalNs / 1_000_000.0 / total) : 0.0);

        m.setEstimatedTotalNetworkLatencyMs(estNetworkMs);
        m.setEstimatedAvgNetworkLatencyMsPerReading(total > 0 ? round2((double) estNetworkMs / total) : 0.0);
        m.setEstimatedBandwidthBytes(estBwBytes);
        m.setEstimatedBandwidthKB(round2(estBwBytes / 1024.0));

        m.setNormalCount(normal);
        m.setWarningCount(warning);
        m.setCriticalCount(critical);
        return m;
    }

    private ComparisonSummary buildSummary(ModeMetrics cloud, ModeMetrics fog, int datasetSize) {
        ComparisonSummary s = new ComparisonSummary();

        // Java processing times (measured)
        s.setCloudJavaProcessingMs(cloud.getTotalJavaProcessingMs());
        s.setFogJavaProcessingMs(fog.getTotalJavaProcessingMs());
        double jDiff = fog.getTotalJavaProcessingMs() - cloud.getTotalJavaProcessingMs();
        s.setJavaProcessingDifferenceMs(round2(jDiff));
        s.setFasterProcessingMode(jDiff > 0 ? "CLOUD_ONLY" : "FOG_BASED");
        s.setJavaProcessingNote(
                "Fog-based Java processing time is typically higher per reading because the fog node " +
                "performs additional local work: ring-buffer cache writes and routing decisions. " +
                "Cloud-only only classifies and serializes. The difference is usually < 1ms total " +
                "for 100 readings and is negligible compared to simulated network latency savings."
        );

        // Network latency (simulated estimates)
        s.setCloudEstimatedNetworkLatencyMs(cloud.getEstimatedTotalNetworkLatencyMs());
        s.setFogEstimatedNetworkLatencyMs(fog.getEstimatedTotalNetworkLatencyMs());
        long netSavings = cloud.getEstimatedTotalNetworkLatencyMs() - fog.getEstimatedTotalNetworkLatencyMs();
        s.setNetworkLatencySavingsMs(netSavings);
        s.setNetworkLatencySavingsPercent(
                cloud.getEstimatedTotalNetworkLatencyMs() > 0
                        ? round2(netSavings * 100.0 / cloud.getEstimatedTotalNetworkLatencyMs())
                        : 0.0
        );

        // Bandwidth (simulated estimates)
        s.setCloudEstimatedBandwidthBytes(cloud.getEstimatedBandwidthBytes());
        s.setFogEstimatedBandwidthBytes(fog.getEstimatedBandwidthBytes());
        long bwSavings = cloud.getEstimatedBandwidthBytes() - fog.getEstimatedBandwidthBytes();
        s.setBandwidthSavingsBytes(bwSavings);
        s.setBandwidthSavingsPercent(
                cloud.getEstimatedBandwidthBytes() > 0
                        ? round2(bwSavings * 100.0 / cloud.getEstimatedBandwidthBytes())
                        : 0.0
        );

        // Data routing
        s.setTotalReadings(datasetSize);
        s.setCloudModeTransmissions(cloud.getTotalCloudTransmissions());
        s.setFogModeTransmissions(fog.getTotalCloudTransmissions());
        s.setLocallyFilteredByFog(fog.getLocallyFilteredReadings());
        s.setFogFilterRatePercent(fog.getFilterPercentage());
        s.setComparisonNote(
                "Fog computing advantage is primarily in NETWORK overhead reduction. " +
                "By filtering " + fog.getLocallyFilteredReadings() + " of " + datasetSize +
                " readings locally (" + fog.getFilterPercentage() + "%), estimated cloud " +
                "transmissions drop from " + cloud.getTotalCloudTransmissions() + " to " +
                fog.getTotalCloudTransmissions() + ". " +
                "Network and bandwidth values are simulated estimates using " + CLOUD_RTT_MS +
                "ms round-trip constant. See methodologyNote for full transparency."
        );

        return s;
    }

    private String buildMethodologyNote() {
        return "METHODOLOGY: " +
               "Java processing times are ACTUAL measurements using System.nanoTime() — nanosecond precision. " +
               "Network latency values are SIMULATED ESTIMATES: " +
               "cloud WAN round-trip = " + CLOUD_RTT_MS + "ms (representative value); " +
               "fog-to-cloud forwarding = " + FOG_FORWARD_RTT_MS + "ms (same route, no bonus assumed). " +
               "Bandwidth estimates assume " + PAYLOAD_BYTES + " bytes per JSON reading payload. " +
               "Dataset generated with Random seed=42 for reproducibility. " +
               "Both modes process the same dataset. " + ACADEMIC_DISCLAIMER;
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }

    // =========================================================
    // Internal synthetic reading type (not persisted)
    // =========================================================
    private record SyntheticReading(
            String patientId,
            String fogNodeId,
            double heartRate,
            double temperature,
            double spo2,
            double systolicBp,
            double diastolicBp,
            String scenario,
            Instant timestamp
    ) {}
}
