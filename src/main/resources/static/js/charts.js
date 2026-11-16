/**
 * Smart Healthcare Monitoring Using Fog Computing
 * Chart.js Visualizations & Real-Time Telemetry Analytics
 */

const HealthCharts = {
    telemetryTrendChart: null,
    severityDistChart: null,
    fogWorkloadChart: null,
    latencyComparisonChart: null,

    // Initialize all analytics and dashboard charts
    init() {
        if (typeof Chart === 'undefined') {
            console.warn('Chart.js library is not yet loaded.');
            return;
        }

        // Set global Chart.js defaults
        Chart.defaults.font.family = "'Segoe UI', -apple-system, BlinkMacSystemFont, sans-serif";
        Chart.defaults.color = '#64748b';
        Chart.defaults.borderColor = '#e2e8f0';

        this.initTelemetryTrendChart();
        this.initSeverityDistChart();
        this.initFogWorkloadChart();
        this.initLatencyComparisonChart();
        this.initComparisonCharts();
    },

    // Phase 7: Experiment Comparison Charts
    expLatencyChart: null,
    expBandwidthChart: null,
    expRoutingChart: null,

    initComparisonCharts() {
        this.initExpLatencyChart();
        this.initExpBandwidthChart();
        this.initExpRoutingChart();
    },

    initExpLatencyChart() {
        const ctx = document.getElementById('expLatencyCanvas');
        if (!ctx) return;
        if (this.expLatencyChart) this.expLatencyChart.destroy();

        this.expLatencyChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Mode A: Cloud-Only', 'Mode B: Fog-Based'],
                datasets: [
                    {
                        label: 'Simulated Network Latency (ms)',
                        backgroundColor: '#ef4444',
                        borderRadius: 6,
                        data: [18000, 7200]
                    },
                    {
                        label: 'Measured Java Processing Time (ms)',
                        backgroundColor: '#0d9488',
                        borderRadius: 6,
                        data: [1.2, 1.8]
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top', labels: { boxWidth: 12 } },
                    tooltip: {
                        backgroundColor: '#0f2b48',
                        callbacks: {
                            label: (ctx) => ` ${ctx.dataset.label}: ${ctx.raw.toLocaleString()} ms`
                        }
                    }
                },
                scales: {
                    x: { grid: { display: false } },
                    y: {
                        type: 'logarithmic',
                        title: { display: true, text: 'Milliseconds (Log Scale)' },
                        grid: { color: '#f1f5f9' }
                    }
                }
            }
        });
    },

    initExpBandwidthChart() {
        const ctx = document.getElementById('expBandwidthCanvas');
        if (!ctx) return;
        if (this.expBandwidthChart) this.expBandwidthChart.destroy();

        this.expBandwidthChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Mode A: Cloud-Only', 'Mode B: Fog-Based'],
                datasets: [
                    {
                        label: 'Estimated Cloud Bandwidth (KB)',
                        backgroundColor: ['#0284c7', '#14b8a6'],
                        borderRadius: 6,
                        data: [50.0, 20.0]
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f2b48',
                        callbacks: {
                            label: (ctx) => ` Bandwidth: ${ctx.raw.toFixed(1)} KB`
                        }
                    }
                },
                scales: {
                    x: { grid: { display: false } },
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'Kilobytes (KB)' },
                        grid: { color: '#f1f5f9' }
                    }
                }
            }
        });
    },

    initExpRoutingChart() {
        const ctx = document.getElementById('expRoutingCanvas');
        if (!ctx) return;
        if (this.expRoutingChart) this.expRoutingChart.destroy();

        this.expRoutingChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Filtered Locally by Fog', 'Cloud Transmissions'],
                datasets: [{
                    data: [60, 40],
                    backgroundColor: ['#10b981', '#ef4444'],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 12, padding: 12 } },
                    tooltip: {
                        backgroundColor: '#0f2b48',
                        callbacks: {
                            label: (ctx) => ` ${ctx.label}: ${ctx.raw} readings`
                        }
                    }
                }
            }
        });
    },

    updateComparisonExperiment(experiment) {
        if (!experiment || !experiment.cloudOnlyMetrics || !experiment.fogBasedMetrics) return;

        const cloud = experiment.cloudOnlyMetrics;
        const fog = experiment.fogBasedMetrics;

        // 1. Latency Chart (Log scale handles ms difference between network and java time)
        if (this.expLatencyChart) {
            this.expLatencyChart.data.datasets[0].data = [
                cloud.estimatedTotalNetworkLatencyMs,
                fog.estimatedTotalNetworkLatencyMs
            ];
            this.expLatencyChart.data.datasets[1].data = [
                Math.max(0.1, cloud.totalJavaProcessingMs),
                Math.max(0.1, fog.totalJavaProcessingMs)
            ];
            this.expLatencyChart.update();
        }

        // 2. Bandwidth Chart
        if (this.expBandwidthChart) {
            this.expBandwidthChart.data.datasets[0].data = [
                cloud.estimatedBandwidthKB,
                fog.estimatedBandwidthKB
            ];
            this.expBandwidthChart.update();
        }

        // 3. Routing Doughnut Chart
        if (this.expRoutingChart) {
            this.expRoutingChart.data.datasets[0].data = [
                fog.locallyFilteredReadings,
                fog.totalCloudTransmissions
            ];
            this.expRoutingChart.update();
        }
    },

    // 1. Live Telemetry Trend Chart (Heart Rate, SpO2, Systolic BP)
    initTelemetryTrendChart() {
        const ctx = document.getElementById('telemetryTrendCanvas');
        if (!ctx) return;

        if (this.telemetryTrendChart) {
            this.telemetryTrendChart.destroy();
        }

        this.telemetryTrendChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Heart Rate (BPM)',
                        borderColor: '#ef4444',
                        backgroundColor: 'rgba(239, 68, 68, 0.08)',
                        borderWidth: 2,
                        tension: 0.35,
                        fill: true,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                        data: []
                    },
                    {
                        label: 'SpO2 (%)',
                        borderColor: '#0d9488',
                        backgroundColor: 'rgba(13, 148, 136, 0.08)',
                        borderWidth: 2,
                        tension: 0.35,
                        fill: true,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                        data: []
                    },
                    {
                        label: 'Systolic BP (mmHg)',
                        borderColor: '#0284c7',
                        backgroundColor: 'rgba(2, 132, 199, 0.08)',
                        borderWidth: 2,
                        tension: 0.35,
                        fill: true,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                        data: []
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            boxWidth: 12,
                            usePointStyle: true
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f2b48',
                        titleFont: { size: 12 },
                        bodyFont: { size: 12 },
                        padding: 10,
                        cornerRadius: 8
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { maxRotation: 45, autoSkip: true, maxTicksLimit: 8 }
                    },
                    y: {
                        beginAtZero: false,
                        suggestedMin: 50,
                        suggestedMax: 160,
                        grid: { color: '#f1f5f9' }
                    }
                }
            }
        });
    },

    // 2. Abnormality Severity Distribution (Normal, Warning, Critical)
    initSeverityDistChart() {
        const ctx = document.getElementById('severityDistCanvas');
        if (!ctx) return;

        if (this.severityDistChart) {
            this.severityDistChart.destroy();
        }

        this.severityDistChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Normal', 'Warning', 'Critical'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
                    borderWidth: 2,
                    borderColor: '#ffffff',
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            boxWidth: 12,
                            usePointStyle: true,
                            padding: 16
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f2b48',
                        callbacks: {
                            label: function(context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const val = context.raw || 0;
                                const pct = total > 0 ? ((val / total) * 100).toFixed(1) : 0;
                                return ` ${context.label}: ${val} (${pct}%)`;
                            }
                        }
                    }
                }
            }
        });
    },

    // 3. Fog Node Workload & Capacity Utilization Bar Chart
    initFogWorkloadChart() {
        const ctx = document.getElementById('fogWorkloadCanvas');
        if (!ctx) return;

        if (this.fogWorkloadChart) {
            this.fogWorkloadChart.destroy();
        }

        this.fogWorkloadChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Hospital Fog', 'Clinic Fog', 'Emergency Fog'],
                datasets: [
                    {
                        label: 'Active Patients',
                        backgroundColor: '#0d9488',
                        borderRadius: 6,
                        data: [0, 0, 0]
                    },
                    {
                        label: 'Total Capacity',
                        backgroundColor: '#e2e8f0',
                        borderRadius: 6,
                        data: [50, 30, 20]
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: { boxWidth: 12 }
                    },
                    tooltip: { backgroundColor: '#0f2b48' }
                },
                scales: {
                    x: { grid: { display: false } },
                    y: {
                        beginAtZero: true,
                        grid: { color: '#f1f5f9' },
                        ticks: { stepSize: 10 }
                    }
                }
            }
        });
    },

    // 4. Edge Latency vs Cloud Round-Trip Latency Comparison
    initLatencyComparisonChart() {
        const ctx = document.getElementById('latencyComparisonCanvas');
        if (!ctx) return;

        if (this.latencyComparisonChart) {
            this.latencyComparisonChart.destroy();
        }

        this.latencyComparisonChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Fog Edge Inference', 'Direct Cloud Round-Trip'],
                datasets: [{
                    label: 'Latency (ms)',
                    data: [3.4, 185.0],
                    backgroundColor: ['#14b8a6', '#94a3b8'],
                    borderRadius: 8,
                    barThickness: 45
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f2b48',
                        callbacks: {
                            label: (ctx) => ` ${ctx.raw.toFixed(1)} ms latency`
                        }
                    }
                },
                scales: {
                    x: {
                        title: { display: true, text: 'Milliseconds (lower is better)' },
                        grid: { color: '#f1f5f9' }
                    },
                    y: { grid: { display: false } }
                }
            }
        });
    },

    // Update Telemetry Trend Chart with recent readings
    updateTelemetryTrend(readings) {
        if (!this.telemetryTrendChart || !readings || readings.length === 0) return;

        // Take up to latest 15 readings, sorted chronologically
        const sorted = [...readings].reverse().slice(-15);
        const labels = sorted.map(r => {
            if (!r.timestamp) return '';
            const d = new Date(r.timestamp);
            return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        });

        const heartRates = sorted.map(r => r.heartRate || null);
        const spo2Vals = sorted.map(r => r.spo2 || null);
        const systolicBps = sorted.map(r => r.systolicBp || null);

        this.telemetryTrendChart.data.labels = labels;
        this.telemetryTrendChart.data.datasets[0].data = heartRates;
        this.telemetryTrendChart.data.datasets[1].data = spo2Vals;
        this.telemetryTrendChart.data.datasets[2].data = systolicBps;
        this.telemetryTrendChart.update('none'); // frictionless smooth update
    },

    // Update Abnormality Distribution Chart
    updateSeverityDistribution(history) {
        if (!this.severityDistChart || !history) return;

        let normal = 0;
        let warning = 0;
        let critical = 0;

        history.forEach(item => {
            const status = (item.healthStatus || '').toUpperCase();
            if (status === 'NORMAL') normal++;
            else if (status === 'WARNING') warning++;
            else if (status === 'CRITICAL') critical++;
        });

        // Provide default visual ratio if history is very small
        if (normal + warning + critical === 0) {
            normal = 1;
        }

        this.severityDistChart.data.datasets[0].data = [normal, warning, critical];
        this.severityDistChart.update();
    },

    // Update Fog Node Workload Bar Chart
    updateFogWorkload(fogNodes) {
        if (!this.fogWorkloadChart || !fogNodes) return;

        const labels = [];
        const workloads = [];
        const capacities = [];

        fogNodes.forEach(node => {
            labels.push(node.nodeName ? node.nodeName.replace('Fog Node', '').trim() : node.nodeId);
            workloads.push(node.currentWorkload || 0);
            capacities.push(node.maxCapacity || 50);
        });

        if (labels.length > 0) {
            this.fogWorkloadChart.data.labels = labels;
            this.fogWorkloadChart.data.datasets[0].data = workloads;
            this.fogWorkloadChart.data.datasets[1].data = capacities;
            this.fogWorkloadChart.update();
        }
    },

    // Update Edge Processing Latency Metric
    updateLatency(avgProcessingMs) {
        if (!this.latencyComparisonChart) return;
        const edgeVal = avgProcessingMs > 0 ? parseFloat(avgProcessingMs.toFixed(2)) : 3.5;
        this.latencyComparisonChart.data.datasets[0].data[0] = edgeVal;
        this.latencyComparisonChart.update('none');
    }
};
