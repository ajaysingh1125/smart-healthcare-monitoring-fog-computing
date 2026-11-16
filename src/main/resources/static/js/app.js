/**
 * Smart Healthcare Monitoring Using Fog Computing
 * Main Single Page Application (SPA) Controller
 */

const App = {
    currentTab: 'dashboard',
    autoRefreshInterval: null,
    autoRefreshActive: true,
    refreshRateMs: 3000,

    // Data Cache
    patients: [],
    fogNodes: [],
    simulationStatus: null,
    alerts: [],
    readingsHistory: [],
    selectedPatientId: null,

    // Modals instances
    modals: {},

    // Application Lifecycle Initialization
    async init() {
        console.log('Initializing Smart Healthcare Fog Monitoring UI...');
        this.initModals();
        this.initNavigation();
        this.initEventHandlers();
        
        // Initialize Chart.js instances
        HealthCharts.init();

        // Initial Data Fetch
        UI.showLoader();
        try {
            await this.refreshAllData();
            // Select first patient for monitoring view if available
            if (this.patients.length > 0 && !this.selectedPatientId) {
                this.selectedPatientId = this.patients[0].patientId;
                this.updateMonitoringPatientSelect();
            }
        } catch (err) {
            console.error('Initialization error:', err);
            UI.showToast('Connection Notice', 'Could not fetch initial data from backend. Check if Spring Boot is running.', 'warning');
        } finally {
            UI.hideLoader();
        }

        // Start Auto Refresh Loop
        this.startAutoRefresh();
    },

    // Initialize Bootstrap Modals
    initModals() {
        const modalIds = [
            'patientModal', 'patientDetailsModal', 'manualReadingModal',
            'acknowledgeAlertModal', 'resolveAlertModal', 'nodePatientsModal',
            'nodeCacheModal', 'thresholdsModal'
        ];
        modalIds.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                this.modals[id] = new bootstrap.Modal(el);
            }
        });
    },

    // Navigation and SPA Tab Switching
    initNavigation() {
        const navLinks = document.querySelectorAll('#sidebar .nav-link, .nav-switch');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const targetTab = link.getAttribute('data-tab');
                if (targetTab) {
                    this.switchTab(targetTab);
                }
            });
        });

        // Sidebar toggle for mobile
        const sidebarToggle = document.getElementById('sidebarToggle');
        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', () => {
                const sidebar = document.getElementById('sidebar');
                if (sidebar) sidebar.classList.toggle('show');
            });
        }
    },

    switchTab(tabId) {
        this.currentTab = tabId;

        // Update active class on sidebar links
        document.querySelectorAll('#sidebar .nav-link').forEach(link => {
            if (link.getAttribute('data-tab') === tabId) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        // Hide all views, display target
        document.querySelectorAll('.view-section').forEach(sec => sec.classList.add('d-none'));
        const activeSection = document.getElementById(`section-${tabId}`);
        if (activeSection) {
            activeSection.classList.remove('d-none');
        }

        // Update page title / breadcrumb
        const titleMap = {
            'dashboard': 'System Overview & Edge Metrics',
            'patients': 'Patient Registry & Management',
            'monitoring': 'Live Edge Biometric Monitoring',
            'fognodes': 'Fog Node Cluster & Load Balancing',
            'alerts': 'Abnormality Detection & Emergency Alerts',
            'analytics': 'Healthcare & Edge Fog Analytics',
            'comparison': 'Fog vs Cloud Performance Comparison (Phase 7)'
        };
        const titleEl = document.getElementById('current-page-title');
        if (titleEl && titleMap[tabId]) {
            titleEl.textContent = titleMap[tabId];
        }

        // Render target view immediately
        this.renderCurrentView();
    },

    // Setup UI event listeners
    initEventHandlers() {
        // Auto refresh toggle button
        const autoRefreshBtn = document.getElementById('btn-toggle-refresh');
        if (autoRefreshBtn) {
            autoRefreshBtn.addEventListener('click', () => {
                this.autoRefreshActive = !this.autoRefreshActive;
                if (this.autoRefreshActive) {
                    this.startAutoRefresh();
                    autoRefreshBtn.innerHTML = '<i class="bi bi-pause-circle me-1"></i> Live: ON';
                    autoRefreshBtn.classList.replace('btn-outline-secondary', 'btn-outline-success');
                    UI.showToast('Auto-Refresh', 'Live 3-second data synchronization resumed.', 'info');
                } else {
                    this.stopAutoRefresh();
                    autoRefreshBtn.innerHTML = '<i class="bi bi-play-circle me-1"></i> Live: PAUSED';
                    autoRefreshBtn.classList.replace('btn-outline-success', 'btn-outline-secondary');
                    UI.showToast('Auto-Refresh', 'Live synchronization paused.', 'secondary');
                }
            });
        }

        // Manual Refresh button
        const manualRefreshBtn = document.getElementById('btn-manual-refresh');
        if (manualRefreshBtn) {
            manualRefreshBtn.addEventListener('click', async () => {
                UI.showLoader();
                await this.refreshAllData();
                UI.hideLoader();
                UI.showToast('Refreshed', 'Healthcare & Fog metrics updated from MongoDB.', 'success');
            });
        }

        // Simulation Start / Stop buttons in Header
        const startSimBtn = document.getElementById('btn-start-simulation');
        if (startSimBtn) {
            startSimBtn.addEventListener('click', () => this.handleStartSimulation());
        }

        const stopSimBtn = document.getElementById('btn-stop-simulation');
        if (stopSimBtn) {
            stopSimBtn.addEventListener('click', () => this.handleStopSimulation());
        }

        // Instant Batch Simulation button
        const instantBatchBtn = document.getElementById('btn-instant-batch');
        if (instantBatchBtn) {
            instantBatchBtn.addEventListener('click', () => this.handleInstantBatch());
        }

        // Patient Search input filter
        const searchInput = document.getElementById('patientSearchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.renderPatientsTable(e.target.value.trim().toLowerCase());
            });
        }

        // Patient Form Submit (Create or Update)
        const patientForm = document.getElementById('patientForm');
        if (patientForm) {
            patientForm.addEventListener('submit', (e) => this.handlePatientFormSubmit(e));
        }

        // Monitoring Patient Select Dropdown
        const monSelect = document.getElementById('monitoringPatientSelect');
        if (monSelect) {
            monSelect.addEventListener('change', (e) => {
                this.selectedPatientId = e.target.value;
                this.renderLiveMonitoring();
            });
        }

        // Scenario Trigger Buttons in Live Monitoring
        document.querySelectorAll('.btn-scenario-trigger').forEach(btn => {
            btn.addEventListener('click', () => {
                const scenario = btn.getAttribute('data-scenario');
                this.triggerScenarioReading(scenario);
            });
        });

        // Manual Reading Form Submit
        const manualReadingForm = document.getElementById('manualReadingForm');
        if (manualReadingForm) {
            manualReadingForm.addEventListener('submit', (e) => this.handleManualReadingSubmit(e));
        }

        // Rebalance Workload Button
        const rebalanceBtn = document.getElementById('btn-rebalance-fog');
        if (rebalanceBtn) {
            rebalanceBtn.addEventListener('click', () => this.handleRebalanceFog());
        }

        // Alert Acknowledge Form Submit
        const ackForm = document.getElementById('acknowledgeAlertForm');
        if (ackForm) {
            ackForm.addEventListener('submit', (e) => this.handleAcknowledgeAlertSubmit(e));
        }

        // Alert Resolve Form Submit
        const resolveForm = document.getElementById('resolveAlertForm');
        if (resolveForm) {
            resolveForm.addEventListener('submit', (e) => this.handleResolveAlertSubmit(e));
        }

        // Alert Filter Buttons (All, Active, History)
        document.querySelectorAll('.btn-alert-filter').forEach(btn => {
            btn.addEventListener('click', (e) => {
                document.querySelectorAll('.btn-alert-filter').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const filter = btn.getAttribute('data-filter');
                this.renderAlertsTable(filter);
            });
        });

        // View Thresholds Button
        const viewThresholdsBtn = document.getElementById('btn-view-thresholds');
        if (viewThresholdsBtn) {
            viewThresholdsBtn.addEventListener('click', () => this.showThresholdsModal());
        }

        // Run Fog vs Cloud Comparison Button (Phase 7)
        const runComparisonBtn = document.getElementById('btn-run-comparison');
        if (runComparisonBtn) {
            runComparisonBtn.addEventListener('click', () => this.handleRunComparison());
        }
    },

    // Auto Refresh Management
    startAutoRefresh() {
        this.stopAutoRefresh();
        this.autoRefreshInterval = setInterval(async () => {
            if (this.autoRefreshActive) {
                await this.refreshAllData(false); // background refresh without loader
            }
        }, this.refreshRateMs);
    },

    stopAutoRefresh() {
        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
            this.autoRefreshInterval = null;
        }
    },

    // Master Data Refresh from Spring Boot REST APIs
    async refreshAllData(showLoader = false) {
        if (showLoader) UI.showLoader();
        try {
            const [healthData, patientsData, fogNodesData, simStatus, activeAlerts, historyData] = await Promise.all([
                API.getHealth().catch(err => ({ status: 'DOWN', database: 'DISCONNECTED' })),
                API.getPatients().catch(() => []),
                API.getFogNodes().catch(() => []),
                API.getSimulationStatus().catch(() => ({ running: false })),
                API.getActiveAlerts().catch(() => []),
                API.getSimulationHistory(25).catch(() => [])
            ]);

            this.patients = Array.isArray(patientsData) ? patientsData : [];
            this.fogNodes = Array.isArray(fogNodesData) ? fogNodesData : [];
            this.simulationStatus = simStatus || { running: false };
            this.alerts = Array.isArray(activeAlerts) ? activeAlerts : [];
            this.readingsHistory = Array.isArray(historyData) ? historyData : [];

            // Update Top Header Indicators
            this.updateHeaderIndicators(healthData);

            // Re-render Views
            this.renderKPIs();
            this.renderCurrentView();

            // Update Global Charts
            HealthCharts.updateTelemetryTrend(this.readingsHistory);
            HealthCharts.updateSeverityDistribution(this.readingsHistory);
            HealthCharts.updateFogWorkload(this.fogNodes);

            // Calculate average edge processing time
            if (this.readingsHistory.length > 0) {
                const totalProcMs = this.readingsHistory.reduce((acc, curr) => acc + (curr.processingTimeMs || 0), 0);
                const avgProcMs = totalProcMs / this.readingsHistory.length;
                HealthCharts.updateLatency(avgProcMs);
            }
        } catch (error) {
            console.error('Data refresh error:', error);
        } finally {
            if (showLoader) UI.hideLoader();
        }
    },

    // Update Header Indicators (MongoDB connection, Simulation Engine Status)
    updateHeaderIndicators(healthData) {
        // Backend / MongoDB badge
        const dbBadge = document.getElementById('header-db-status');
        if (dbBadge) {
            if (healthData && healthData.database === 'CONNECTED') {
                dbBadge.className = 'badge bg-success-subtle text-success border border-success-subtle d-inline-flex align-items-center';
                dbBadge.innerHTML = '<span class="pulse-dot online"></span> MongoDB Connected';
            } else {
                dbBadge.className = 'badge bg-danger-subtle text-danger border border-danger-subtle d-inline-flex align-items-center';
                dbBadge.innerHTML = '<span class="pulse-dot offline"></span> DB Disconnected';
            }
        }

        // Simulation Running Indicator
        const simBadge = document.getElementById('header-sim-status');
        const startSimBtn = document.getElementById('btn-start-simulation');
        const stopSimBtn = document.getElementById('btn-stop-simulation');

        if (simBadge) {
            if (this.simulationStatus && this.simulationStatus.running) {
                simBadge.className = 'badge bg-primary-subtle text-primary border border-primary-subtle d-inline-flex align-items-center';
                simBadge.innerHTML = `<span class="pulse-dot online"></span> IoT Sim Active (${this.simulationStatus.intervalMs || 3000}ms)`;
                if (startSimBtn) startSimBtn.classList.add('d-none');
                if (stopSimBtn) stopSimBtn.classList.remove('d-none');
            } else {
                simBadge.className = 'badge bg-secondary-subtle text-secondary border border-secondary-subtle d-inline-flex align-items-center';
                simBadge.innerHTML = '<i class="bi bi-pause-circle me-1"></i> IoT Sim Idle';
                if (startSimBtn) startSimBtn.classList.remove('d-none');
                if (stopSimBtn) stopSimBtn.classList.add('d-none');
            }
        }
    },

    // Render KPI Top Cards on Dashboard
    renderKPIs() {
        // 1. Total Patients
        const totalPatientsEl = document.getElementById('kpi-total-patients');
        if (totalPatientsEl) totalPatientsEl.textContent = this.patients.length;

        // 2. Active Patients (assigned to online nodes)
        const onlineNodeIds = new Set(this.fogNodes.filter(n => n.status === 'ONLINE').map(n => n.nodeId));
        const activePatientsCount = this.patients.filter(p => onlineNodeIds.has(p.assignedFogNodeId)).length;
        const activePatientsEl = document.getElementById('kpi-active-patients');
        if (activePatientsEl) activePatientsEl.textContent = activePatientsCount;

        // 3. Active Fog Nodes
        const onlineNodesCount = this.fogNodes.filter(n => n.status === 'ONLINE').length;
        const fogNodesEl = document.getElementById('kpi-active-fognodes');
        if (fogNodesEl) fogNodesEl.textContent = `${onlineNodesCount} / ${this.fogNodes.length}`;

        // 4. Processed Readings Count
        const processedEl = document.getElementById('kpi-processed-readings');
        if (processedEl) {
            const totalProcessed = this.fogNodes.reduce((acc, n) => acc + (n.totalProcessedReadings || 0), 0) + this.readingsHistory.length;
            processedEl.textContent = totalProcessed > 0 ? totalProcessed : this.readingsHistory.length;
        }

        // 5. Critical Alerts Count
        const criticalCount = this.alerts.filter(a => a.severity === 'CRITICAL' && a.status === 'ACTIVE').length;
        const criticalEl = document.getElementById('kpi-critical-alerts');
        if (criticalEl) {
            criticalEl.textContent = criticalCount;
            if (criticalCount > 0) {
                criticalEl.classList.add('text-danger');
            } else {
                criticalEl.classList.remove('text-danger');
            }
        }

        // 6. Average Edge Processing Time
        const avgLatencyEl = document.getElementById('kpi-avg-latency');
        if (avgLatencyEl) {
            if (this.readingsHistory.length > 0) {
                const totalProcMs = this.readingsHistory.reduce((acc, curr) => acc + (curr.processingTimeMs || 0), 0);
                const avg = (totalProcMs / this.readingsHistory.length).toFixed(2);
                avgLatencyEl.textContent = `${avg} ms`;
            } else {
                avgLatencyEl.textContent = '3.2 ms';
            }
        }
    },

    // Render the currently active view
    renderCurrentView() {
        switch (this.currentTab) {
            case 'dashboard':
                this.renderDashboardView();
                break;
            case 'patients':
                this.renderPatientsTable();
                break;
            case 'monitoring':
                this.renderLiveMonitoring();
                break;
            case 'fognodes':
                this.renderFogNodesView();
                break;
            case 'alerts':
                this.renderAlertsTable();
                break;
            case 'analytics':
                this.renderAnalyticsView();
                break;
            case 'comparison':
                this.renderComparisonView();
                break;
        }
    },

    // =========================================================================
    // 1. DASHBOARD VIEW
    // =========================================================================
    renderDashboardView() {
        // Render Recent Activity Stream Table
        const tbody = document.getElementById('dashboardActivityBody');
        if (!tbody) return;

        if (this.readingsHistory.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">
                <i class="bi bi-activity fs-3 d-block mb-1"></i>No telemetry streamed yet. Click "Start IoT Sim" or "Generate Batch" to initiate edge telemetry.
            </td></tr>`;
            return;
        }

        tbody.innerHTML = this.readingsHistory.slice(0, 8).map(item => {
            const statusBadge = item.healthStatus === 'CRITICAL' ? '<span class="badge badge-fog badge-critical">CRITICAL</span>' :
                                item.healthStatus === 'WARNING' ? '<span class="badge badge-fog badge-warning-custom">WARNING</span>' :
                                '<span class="badge badge-fog badge-normal">NORMAL</span>';

            const routingBadge = item.cloudForwarded ? 
                '<span class="badge bg-danger-subtle text-danger border border-danger-subtle"><i class="bi bi-cloud-arrow-up me-1"></i>Cloud Forwarded</span>' :
                '<span class="badge bg-teal-subtle text-teal border border-teal-subtle"><i class="bi bi-shield-check me-1"></i>Routine Filtered</span>';

            const timeStr = item.timestamp ? new Date(item.timestamp).toLocaleTimeString() : '--';
            const latencyStr = (item.processingTimeMs !== undefined && item.processingTimeMs !== null) ? `${item.processingTimeMs.toFixed(2)} ms` : '< 5 ms';

            return `
                <tr>
                    <td><strong>${item.patientId || 'N/A'}</strong></td>
                    <td><span class="badge bg-light text-dark border">${item.processedByFogNodeId || 'FOG-EDGE'}</span></td>
                    <td>${item.heartRate || '--'} bpm | ${item.spo2 || '--'}% | ${item.systolicBp || '--'}/${item.diastolicBp || '--'}</td>
                    <td>${statusBadge}</td>
                    <td>${routingBadge}</td>
                    <td><small class="text-muted"><i class="bi bi-stopwatch me-1"></i>${latencyStr}</small></td>
                </tr>
            `;
        }).join('');
    },

    // =========================================================================
    // 2. PATIENT MANAGEMENT VIEW
    // =========================================================================
    renderPatientsTable(filterQuery = '') {
        const tbody = document.getElementById('patientsTableBody');
        if (!tbody) return;

        let filtered = this.patients;
        if (filterQuery) {
            filtered = this.patients.filter(p => 
                (p.patientId && p.patientId.toLowerCase().includes(filterQuery)) ||
                (p.name && p.name.toLowerCase().includes(filterQuery)) ||
                (p.medicalHistory && p.medicalHistory.toLowerCase().includes(filterQuery))
            );
        }

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">No matching patient records found in MongoDB.</td></tr>`;
            return;
        }

        tbody.innerHTML = filtered.map(patient => {
            const regDate = patient.registrationDate ? new Date(patient.registrationDate).toLocaleDateString() : 'N/A';
            return `
                <tr>
                    <td><span class="badge bg-primary-subtle text-primary border border-primary-subtle">${patient.patientId}</span></td>
                    <td class="fw-semibold text-dark">${patient.name}</td>
                    <td>${patient.age} / ${patient.gender}</td>
                    <td>${patient.contactNumber || 'N/A'}</td>
                    <td><span class="badge bg-light text-secondary border text-truncate" style="max-width: 140px;" title="${patient.medicalHistory || ''}">${patient.medicalHistory || 'None'}</span></td>
                    <td><span class="badge bg-info-subtle text-info border border-info-subtle">${patient.assignedFogNodeId || 'Unassigned'}</span></td>
                    <td><small class="text-muted">${regDate}</small></td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-info" title="View Details" onclick="App.viewPatientDetails('${patient.patientId}')">
                                <i class="bi bi-eye"></i>
                            </button>
                            <button class="btn btn-outline-primary" title="Edit Patient" onclick="App.openEditPatientModal('${patient.patientId}')">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-outline-danger" title="Delete Patient" onclick="App.deletePatient('${patient.patientId}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    openAddPatientModal() {
        document.getElementById('patientModalTitle').textContent = 'Register New Patient';
        document.getElementById('patientForm').reset();
        document.getElementById('patientInternalId').value = '';
        
        // Auto-generate next patientId
        const nextNum = 1000 + this.patients.length + 1;
        document.getElementById('patientIdInput').value = `PAT-${nextNum}`;
        document.getElementById('patientIdInput').readOnly = false;

        // Populate Fog Node Select
        this.populateFogNodeSelect('patientFogNodeSelect');

        this.modals['patientModal'].show();
    },

    openEditPatientModal(patientId) {
        const patient = this.patients.find(p => p.patientId === patientId);
        if (!patient) return;

        document.getElementById('patientModalTitle').textContent = `Edit Patient (${patient.patientId})`;
        document.getElementById('patientInternalId').value = patient.id || '';
        document.getElementById('patientIdInput').value = patient.patientId;
        document.getElementById('patientIdInput').readOnly = true;
        document.getElementById('patientNameInput').value = patient.name;
        document.getElementById('patientAgeInput').value = patient.age;
        document.getElementById('patientGenderSelect').value = patient.gender;
        document.getElementById('patientContactInput').value = patient.contactNumber || '';
        const historyDisplay = Array.isArray(patient.medicalHistory) ? patient.medicalHistory.join(', ') : (patient.medicalHistory || '');
        document.getElementById('patientMedicalHistoryInput').value = historyDisplay;

        this.populateFogNodeSelect('patientFogNodeSelect', patient.assignedFogNodeId);
        this.modals['patientModal'].show();
    },

    populateFogNodeSelect(selectId, selectedVal = '') {
        const sel = document.getElementById(selectId);
        if (!sel) return;
        sel.innerHTML = '<option value="">Auto-Assign via Fog Load Balancer</option>' +
            this.fogNodes.map(node => `
                <option value="${node.nodeId}" ${node.nodeId === selectedVal ? 'selected' : ''}>
                    ${node.nodeName} (${node.nodeId}) - Workload: ${node.currentWorkload}/${node.maxCapacity}
                </option>
            `).join('');
    },

    async handlePatientFormSubmit(e) {
        e.preventDefault();
        const internalId = document.getElementById('patientInternalId').value;
        const patientId = document.getElementById('patientIdInput').value.trim();
        const name = document.getElementById('patientNameInput').value.trim();
        const age = parseInt(document.getElementById('patientAgeInput').value, 10);
        const gender = document.getElementById('patientGenderSelect').value;
        const contactNumber = document.getElementById('patientContactInput').value.trim();
        const medicalHistoryRaw = document.getElementById('patientMedicalHistoryInput').value.trim();
        const medicalHistory = medicalHistoryRaw ? medicalHistoryRaw.split(',').map(s => s.trim()).filter(Boolean) : [];
        const assignedFogNodeId = document.getElementById('patientFogNodeSelect').value;

        const payload = { patientId, name, age, gender, contactNumber, medicalHistory, assignedFogNodeId };

        UI.showLoader();
        try {
            if (internalId) {
                // Update
                await API.updatePatient(patientId, payload);
                UI.showToast('Patient Updated', `Successfully updated records for ${name} (${patientId}).`, 'success');
            } else {
                // Create
                await API.createPatient(payload);
                UI.showToast('Patient Registered', `Patient ${name} registered in MongoDB and assigned to Fog Node.`, 'success');
            }
            this.modals['patientModal'].hide();
            await this.refreshAllData(false);
            this.updateMonitoringPatientSelect();
        } catch (err) {
            UI.showToast('Validation Error', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async deletePatient(patientId) {
        if (!confirm(`Are you sure you want to permanently delete patient ${patientId} from MongoDB?`)) {
            return;
        }

        UI.showLoader();
        try {
            await API.deletePatient(patientId);
            UI.showToast('Patient Deleted', `Patient ${patientId} has been removed.`, 'info');
            await this.refreshAllData(false);
            this.updateMonitoringPatientSelect();
        } catch (err) {
            UI.showToast('Deletion Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async viewPatientDetails(patientId) {
        const patient = this.patients.find(p => p.patientId === patientId);
        if (!patient) return;

        document.getElementById('detailsPatientId').textContent = patient.patientId;
        document.getElementById('detailsPatientName').textContent = patient.name;
        document.getElementById('detailsPatientAge').textContent = patient.age;
        document.getElementById('detailsPatientGender').textContent = patient.gender;
        document.getElementById('detailsPatientContact').textContent = patient.contactNumber || 'N/A';
        document.getElementById('detailsPatientHistory').textContent = patient.medicalHistory || 'None';
        document.getElementById('detailsPatientFogNode').textContent = patient.assignedFogNodeId || 'Unassigned';
        document.getElementById('detailsPatientRegDate').textContent = patient.registrationDate ? new Date(patient.registrationDate).toLocaleString() : 'N/A';

        // Load recent patient readings from cache or history
        const patientReadings = this.readingsHistory.filter(r => r.patientId === patientId);
        const tbody = document.getElementById('detailsPatientReadingsBody');
        if (tbody) {
            if (patientReadings.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No telemetry records cached for this patient yet.</td></tr>`;
            } else {
                tbody.innerHTML = patientReadings.map(r => `
                    <tr>
                        <td><small>${new Date(r.timestamp).toLocaleTimeString()}</small></td>
                        <td>${r.heartRate || '--'} bpm</td>
                        <td>${r.temperature || '--'} °C</td>
                        <td>${r.spo2 || '--'}%</td>
                        <td><span class="badge ${r.healthStatus === 'CRITICAL' ? 'badge-critical' : r.healthStatus === 'WARNING' ? 'badge-warning-custom' : 'badge-normal'}">${r.healthStatus}</span></td>
                    </tr>
                `).join('');
            }
        }

        this.modals['patientDetailsModal'].show();
    },

    // =========================================================================
    // 3. LIVE MONITORING VIEW
    // =========================================================================
    updateMonitoringPatientSelect() {
        const select = document.getElementById('monitoringPatientSelect');
        if (!select) return;

        select.innerHTML = this.patients.map(p => `
            <option value="${p.patientId}" ${p.patientId === this.selectedPatientId ? 'selected' : ''}>
                ${p.patientId} - ${p.name} (${p.medicalHistory || 'Routine'})
            </option>
        `).join('');

        if (!this.selectedPatientId && this.patients.length > 0) {
            this.selectedPatientId = this.patients[0].patientId;
        }
    },

    renderLiveMonitoring() {
        if (!this.selectedPatientId && this.patients.length > 0) {
            this.selectedPatientId = this.patients[0].patientId;
        }

        const patient = this.patients.find(p => p.patientId === this.selectedPatientId);
        if (!patient) return;

        // Find latest reading for selected patient from history or default to baseline
        const patientReadings = this.readingsHistory.filter(r => r.patientId === this.selectedPatientId);
        const latest = patientReadings.length > 0 ? patientReadings[0] : null;

        // Display vitals
        const hrEl = document.getElementById('liveVitalHeartRate');
        const tempEl = document.getElementById('liveVitalTemperature');
        const spo2El = document.getElementById('liveVitalSpO2');
        const bpEl = document.getElementById('liveVitalBloodPressure');
        const statusBadgeEl = document.getElementById('livePatientHealthStatus');

        if (latest) {
            if (hrEl) hrEl.textContent = latest.heartRate ? Math.round(latest.heartRate) : '--';
            if (tempEl) tempEl.textContent = latest.temperature ? latest.temperature.toFixed(1) : '--';
            if (spo2El) spo2El.textContent = latest.spo2 ? Math.round(latest.spo2) : '--';
            if (bpEl) bpEl.textContent = (latest.systolicBp && latest.diastolicBp) ? `${Math.round(latest.systolicBp)} / ${Math.round(latest.diastolicBp)}` : '-- / --';

            if (statusBadgeEl) {
                statusBadgeEl.textContent = latest.healthStatus || 'NORMAL';
                statusBadgeEl.className = latest.healthStatus === 'CRITICAL' ? 'badge badge-fog badge-critical fs-6' :
                                         latest.healthStatus === 'WARNING' ? 'badge badge-fog badge-warning-custom fs-6' :
                                         'badge badge-fog badge-normal fs-6';
            }
        } else {
            if (hrEl) hrEl.textContent = '72';
            if (tempEl) tempEl.textContent = '36.8';
            if (spo2El) spo2El.textContent = '98';
            if (bpEl) bpEl.textContent = '120 / 80';
            if (statusBadgeEl) {
                statusBadgeEl.textContent = 'AWAITING TELEMETRY';
                statusBadgeEl.className = 'badge bg-secondary-subtle text-secondary fs-6';
            }
        }

        // Assigned Fog Node Info Card
        const node = this.fogNodes.find(n => n.nodeId === patient.assignedFogNodeId);
        const nodeCardTitle = document.getElementById('monAssignedNodeName');
        const nodeCardStatus = document.getElementById('monAssignedNodeStatus');
        const nodeCardLatency = document.getElementById('monAssignedNodeLatency');

        if (nodeCardTitle) nodeCardTitle.textContent = node ? `${node.nodeName} (${node.nodeId})` : (patient.assignedFogNodeId || 'Dynamic Load Balancing');
        if (nodeCardStatus) {
            const isOnline = node ? node.status === 'ONLINE' : true;
            nodeCardStatus.innerHTML = isOnline ? 
                '<span class="badge bg-success-subtle text-success"><span class="pulse-dot online"></span> ONLINE</span>' :
                '<span class="badge bg-danger-subtle text-danger"><span class="pulse-dot offline"></span> OFFLINE</span>';
        }
        if (nodeCardLatency) {
            nodeCardLatency.textContent = latest ? `${(latest.processingTimeMs || 3.4).toFixed(2)} ms edge latency` : '< 5 ms edge inference';
        }

        // Render Telemetry Stream table for this patient
        const tableBody = document.getElementById('monitoringHistoryTableBody');
        if (tableBody) {
            if (patientReadings.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-3">No incoming readings recorded for ${patient.name}. Trigger a scenario button above.</td></tr>`;
            } else {
                tableBody.innerHTML = patientReadings.map(r => `
                    <tr>
                        <td><small>${new Date(r.timestamp).toLocaleTimeString()}</small></td>
                        <td><strong>${Math.round(r.heartRate)}</strong> bpm</td>
                        <td>${r.temperature ? r.temperature.toFixed(1) : '--'} °C</td>
                        <td>${Math.round(r.spo2)}%</td>
                        <td>${Math.round(r.systolicBp)} / ${Math.round(r.diastolicBp)} mmHg</td>
                        <td><span class="badge ${r.healthStatus === 'CRITICAL' ? 'badge-critical' : r.healthStatus === 'WARNING' ? 'badge-warning-custom' : 'badge-normal'}">${r.healthStatus}</span></td>
                    </tr>
                `).join('');
            }
        }
    },

    async triggerScenarioReading(scenario) {
        if (!this.selectedPatientId) {
            UI.showToast('Selection Required', 'Please select a patient to simulate.', 'warning');
            return;
        }

        UI.showLoader();
        try {
            const result = await API.generateSingleReading(this.selectedPatientId, scenario);
            UI.showToast(`Scenario [${scenario}] Processed`, 
                `Processed at Fog Node ${result.processedByFogNodeId} in ${result.processingTimeMs ? result.processingTimeMs.toFixed(2) : 3.2}ms. Decision: ${result.cloudForwarded ? 'Cloud Forwarded' : 'Filtered Locally'}.`, 
                result.healthStatus === 'CRITICAL' ? 'danger' : result.healthStatus === 'WARNING' ? 'warning' : 'success'
            );
            await this.refreshAllData(false);
            this.renderLiveMonitoring();
        } catch (err) {
            UI.showToast('Simulation Error', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    openManualReadingModal() {
        if (!this.selectedPatientId) {
            UI.showToast('Selection Required', 'Please select a patient first.', 'warning');
            return;
        }
        document.getElementById('manualPatientId').value = this.selectedPatientId;
        this.modals['manualReadingModal'].show();
    },

    async handleManualReadingSubmit(e) {
        e.preventDefault();
        const patientId = document.getElementById('manualPatientId').value;
        const heartRate = parseFloat(document.getElementById('manualHeartRate').value);
        const temperature = parseFloat(document.getElementById('manualTemperature').value);
        const spo2 = parseFloat(document.getElementById('manualSpO2').value);
        const systolicBp = parseFloat(document.getElementById('manualSystolicBp').value);
        const diastolicBp = parseFloat(document.getElementById('manualDiastolicBp').value);

        const payload = {
            patientId,
            sensorDeviceId: `SENS-MANUAL-${patientId}`,
            deviceId: `SENS-MANUAL-${patientId}`,
            heartRate,
            bloodOxygenSpO2: spo2,
            spo2,
            bodyTemperature: temperature,
            temperature,
            systolicBloodPressure: systolicBp,
            systolicBp,
            diastolicBloodPressure: diastolicBp,
            diastolicBp
        };

        UI.showLoader();
        try {
            const result = await API.submitManualReading(payload);
            this.modals['manualReadingModal'].hide();
            UI.showToast('Telemetry Ingested', 
                `Edge evaluated status: ${result.healthStatus} (${result.processingTimeMs ? result.processingTimeMs.toFixed(2) : 3.1} ms)`, 
                result.healthStatus === 'CRITICAL' ? 'danger' : 'success'
            );
            await this.refreshAllData(false);
            this.renderLiveMonitoring();
        } catch (err) {
            UI.showToast('Submission Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    // =========================================================================
    // 4. FOG NODE MANAGEMENT VIEW
    // =========================================================================
    renderFogNodesView() {
        const container = document.getElementById('fogNodesCardsContainer');
        if (!container) return;

        if (this.fogNodes.length === 0) {
            container.innerHTML = `<div class="col-12 text-center text-muted py-4">No Fog Nodes initialized.</div>`;
            return;
        }

        container.innerHTML = this.fogNodes.map(node => {
            const isOnline = node.status === 'ONLINE';
            const workloadPct = Math.min(100, Math.round(((node.currentWorkload || 0) / (node.maxCapacity || 50)) * 100));
            const progressColor = workloadPct > 80 ? 'bg-danger' : workloadPct > 50 ? 'bg-warning' : 'bg-teal';

            return `
                <div class="col-lg-4 col-md-6 mb-4">
                    <div class="card med-card h-100 ${!isOnline ? 'border-danger' : ''}">
                        <div class="card-body d-flex flex-column">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h5 class="card-title fw-bold text-navy mb-1">${node.nodeName}</h5>
                                    <span class="badge bg-light text-secondary border font-monospace">${node.nodeId}</span>
                                    <div class="small text-muted mt-1"><i class="bi bi-geo-alt me-1"></i>${node.location || 'Local Hospital Perimeter'}</div>
                                </div>
                                <div class="text-end">
                                    <span class="badge ${isOnline ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'} p-2">
                                        <span class="pulse-dot ${isOnline ? 'online' : 'offline'}"></span>
                                        ${node.status}
                                    </span>
                                </div>
                            </div>

                            <div class="mb-3">
                                <div class="d-flex justify-content-between text-sm mb-1">
                                    <span class="text-muted small">Workload Capacity:</span>
                                    <span class="fw-bold small text-navy">${node.currentWorkload || 0} / ${node.maxCapacity || 50} Patients (${workloadPct}%)</span>
                                </div>
                                <div class="fog-progress">
                                    <div class="progress-bar ${progressColor}" role="progressbar" style="width: ${workloadPct}%"></div>
                                </div>
                            </div>

                            <div class="row g-2 mb-3 bg-light rounded p-2 text-center">
                                <div class="col-6 border-end">
                                    <div class="text-muted small">Total Processed</div>
                                    <div class="fw-bold text-navy">${node.totalProcessedReadings || 0}</div>
                                </div>
                                <div class="col-6">
                                    <div class="text-muted small">Edge Latency</div>
                                    <div class="fw-bold text-teal">${(node.averageProcessingTimeMs || 3.2).toFixed(1)} ms</div>
                                </div>
                            </div>

                            <div class="mt-auto d-grid gap-2">
                                <div class="btn-group">
                                    <button class="btn btn-sm btn-outline-primary" onclick="App.viewNodePatients('${node.nodeId}')">
                                        <i class="bi bi-people me-1"></i>Patients (${node.currentWorkload || 0})
                                    </button>
                                    <button class="btn btn-sm btn-outline-secondary" onclick="App.inspectNodeCache('${node.nodeId}')">
                                        <i class="bi bi-cpu me-1"></i>Cache
                                    </button>
                                </div>
                                <button class="btn btn-sm ${isOnline ? 'btn-outline-danger' : 'btn-outline-success'}" 
                                        onclick="App.toggleNodeStatus('${node.nodeId}', '${isOnline ? 'OFFLINE' : 'ONLINE'}')">
                                    <i class="bi ${isOnline ? 'bi-power' : 'bi-arrow-clockwise'} me-1"></i>
                                    ${isOnline ? 'Simulate Failure (Failover)' : 'Bring Node Online'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    async toggleNodeStatus(nodeId, newStatus) {
        UI.showLoader();
        try {
            const res = await API.updateFogNodeStatus(nodeId, newStatus);
            if (newStatus === 'OFFLINE') {
                UI.showToast('Fog Failover Triggered', 
                    `Node ${nodeId} is now OFFLINE. ${res.reassignedCount || 0} patients automatically reassigned to surviving online nodes.`, 
                    'warning'
                );
            } else {
                UI.showToast('Fog Node Online', `Node ${nodeId} is now active and ready for edge load balancing.`, 'success');
            }
            await this.refreshAllData(false);
            this.renderFogNodesView();
        } catch (err) {
            UI.showToast('Node Action Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async handleRebalanceFog() {
        UI.showLoader();
        try {
            const res = await API.rebalanceFogWorkload();
            UI.showToast('Fog Cluster Rebalanced', `Re-evaluated patient allocations across all ONLINE Fog Nodes.`, 'success');
            await this.refreshAllData(false);
            this.renderFogNodesView();
        } catch (err) {
            UI.showToast('Rebalance Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async viewNodePatients(nodeId) {
        UI.showLoader();
        try {
            const patients = await API.getFogNodePatients(nodeId);
            document.getElementById('nodePatientsModalTitle').textContent = `Patients Managed by ${nodeId}`;
            const tbody = document.getElementById('nodePatientsTableBody');
            if (tbody) {
                if (!patients || patients.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">No patients currently assigned to this node.</td></tr>`;
                } else {
                    tbody.innerHTML = patients.map(p => `
                        <tr>
                            <td><span class="badge bg-light text-dark border">${p.patientId}</span></td>
                            <td>${p.name}</td>
                            <td>${p.age} / ${p.gender}</td>
                            <td><span class="badge bg-info-subtle text-info">${p.medicalHistory || 'Routine'}</span></td>
                        </tr>
                    `).join('');
                }
            }
            this.modals['nodePatientsModal'].show();
        } catch (err) {
            UI.showToast('Failed to load node patients', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async inspectNodeCache(nodeId) {
        UI.showLoader();
        try {
            const cacheData = await API.getFogNodeCache(nodeId, 15);
            document.getElementById('nodeCacheModalTitle').textContent = `Edge Local Ring-Buffer Cache: ${nodeId}`;
            const tbody = document.getElementById('nodeCacheTableBody');
            if (tbody) {
                const readings = cacheData.readings || [];
                if (readings.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No readings currently cached in node memory.</td></tr>`;
                } else {
                    tbody.innerHTML = readings.map(r => `
                        <tr>
                            <td><small>${new Date(r.timestamp).toLocaleTimeString()}</small></td>
                            <td><strong>${r.patientId}</strong></td>
                            <td>${Math.round(r.heartRate || 0)} bpm | ${Math.round(r.spo2 || 0)}%</td>
                            <td><span class="badge ${r.healthStatus === 'CRITICAL' ? 'badge-critical' : r.healthStatus === 'WARNING' ? 'badge-warning-custom' : 'badge-normal'}">${r.healthStatus}</span></td>
                            <td><small class="text-muted">${(r.processingTimeMs || 3.1).toFixed(2)} ms</small></td>
                        </tr>
                    `).join('');
                }
            }
            this.modals['nodeCacheModal'].show();
        } catch (err) {
            UI.showToast('Failed to inspect cache', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    // =========================================================================
    // 5. EMERGENCY ALERTS VIEW
    // =========================================================================
    async renderAlertsTable(filter = 'active') {
        const tbody = document.getElementById('alertsTableBody');
        if (!tbody) return;

        let alertsList = [];
        try {
            if (filter === 'all') {
                alertsList = await API.getAllAlerts();
            } else if (filter === 'history') {
                alertsList = await API.getHistoricalAlerts();
            } else {
                alertsList = await API.getActiveAlerts();
            }
        } catch (err) {
            alertsList = this.alerts;
        }

        if (alertsList.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">
                <i class="bi bi-shield-check fs-2 text-success d-block mb-1"></i>No ${filter} alerts recorded in MongoDB. All biometric parameters are within thresholds.
            </td></tr>`;
            return;
        }

        tbody.innerHTML = alertsList.map(alert => {
            const isCritical = alert.severity === 'CRITICAL';
            const severityBadge = isCritical ? 
                '<span class="badge badge-fog badge-critical"><i class="bi bi-exclamation-triangle-fill me-1"></i>CRITICAL</span>' :
                '<span class="badge badge-fog badge-warning-custom">WARNING</span>';

            const statusBadge = alert.status === 'ACTIVE' ? '<span class="badge bg-danger-subtle text-danger border border-danger-subtle">ACTIVE</span>' :
                                alert.status === 'ACKNOWLEDGED' ? '<span class="badge bg-warning-subtle text-warning border border-warning-subtle">ACKNOWLEDGED</span>' :
                                '<span class="badge bg-success-subtle text-success border border-success-subtle">RESOLVED</span>';

            const timeStr = alert.timestamp ? new Date(alert.timestamp).toLocaleString() : 'N/A';

            let actionButtons = '';
            if (alert.status === 'ACTIVE') {
                actionButtons = `
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-warning" title="Acknowledge Alert" onclick="App.openAcknowledgeAlertModal('${alert.alertId}')">
                            <i class="bi bi-check2"></i> Ack
                        </button>
                        <button class="btn btn-outline-success" title="Resolve Alert" onclick="App.openResolveAlertModal('${alert.alertId}', ${isCritical})">
                            <i class="bi bi-check-circle"></i> Resolve
                        </button>
                    </div>
                `;
            } else if (alert.status === 'ACKNOWLEDGED') {
                actionButtons = `
                    <button class="btn btn-sm btn-outline-success" title="Resolve Alert" onclick="App.openResolveAlertModal('${alert.alertId}', ${isCritical})">
                        <i class="bi bi-check-circle me-1"></i> Resolve
                    </button>
                `;
            } else {
                actionButtons = `<small class="text-muted"><i class="bi bi-person-check me-1"></i>${alert.resolvedBy || 'Clinician'}</small>`;
            }

            return `
                <tr>
                    <td><span class="badge bg-light text-dark border font-monospace">${alert.alertId || 'N/A'}</span></td>
                    <td>
                        <strong>${alert.patientName || alert.patientId}</strong>
                        <div class="small text-muted">${alert.patientId}</div>
                    </td>
                    <td><span class="fw-bold text-navy">${alert.abnormalParameter || 'Biometrics'}</span></td>
                    <td>
                        <span class="fw-semibold text-danger">${alert.measuredValue || '--'}</span>
                        <div class="small text-muted">Threshold: ${alert.thresholdExceeded || 'Limit'}</div>
                    </td>
                    <td>${severityBadge}</td>
                    <td><span class="badge bg-light text-secondary border">${alert.detectedByFogNodeId || 'FOG-EDGE'}</span></td>
                    <td>${statusBadge}<div class="small text-muted mt-1">${timeStr}</div></td>
                    <td>${actionButtons}</td>
                </tr>
            `;
        }).join('');
    },

    openAcknowledgeAlertModal(alertId) {
        document.getElementById('ackAlertId').value = alertId;
        document.getElementById('ackClinicianName').value = 'Dr. Sarah Jenkins, MD';
        document.getElementById('ackNotes').value = 'Clinician triage initiated. Telemetry verified at fog node.';
        this.modals['acknowledgeAlertModal'].show();
    },

    async handleAcknowledgeAlertSubmit(e) {
        e.preventDefault();
        const alertId = document.getElementById('ackAlertId').value;
        const clinicianName = document.getElementById('ackClinicianName').value.trim();
        const notes = document.getElementById('ackNotes').value.trim();

        UI.showLoader();
        try {
            await API.acknowledgeAlert(alertId, clinicianName, notes);
            this.modals['acknowledgeAlertModal'].hide();
            UI.showToast('Alert Acknowledged', `Alert ${alertId} acknowledged by ${clinicianName}.`, 'warning');
            await this.refreshAllData(false);
            this.renderAlertsTable();
        } catch (err) {
            UI.showToast('Action Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    openResolveAlertModal(alertId, isCritical = false) {
        document.getElementById('resolveAlertId').value = alertId;
        document.getElementById('resolveClinicianName').value = 'Dr. Sarah Jenkins, MD';
        document.getElementById('resolveNotes').value = isCritical ? 
            'Patient administered oxygen & stabilized. Normal telemetry restored at edge node.' :
            'Patient rested; vitals normalized.';

        const safetyNotice = document.getElementById('resolveCriticalWarning');
        if (safetyNotice) {
            if (isCritical) {
                safetyNotice.classList.remove('d-none');
            } else {
                safetyNotice.classList.add('d-none');
            }
        }

        this.modals['resolveAlertModal'].show();
    },

    async handleResolveAlertSubmit(e) {
        e.preventDefault();
        const alertId = document.getElementById('resolveAlertId').value;
        const clinicianName = document.getElementById('resolveClinicianName').value.trim();
        const notes = document.getElementById('resolveNotes').value.trim();

        UI.showLoader();
        try {
            await API.resolveAlert(alertId, clinicianName, notes, true);
            this.modals['resolveAlertModal'].hide();
            UI.showToast('Alert Resolved', `Alert ${alertId} resolved in MongoDB by ${clinicianName}.`, 'success');
            await this.refreshAllData(false);
            this.renderAlertsTable();
        } catch (err) {
            UI.showToast('Resolution Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async showThresholdsModal() {
        UI.showLoader();
        try {
            const data = await API.getAlertThresholds();
            const container = document.getElementById('thresholdsModalContent');
            if (container) {
                const t = data.thresholds || {};
                container.innerHTML = `
                    <div class="alert alert-info border-info mb-3">
                        <i class="bi bi-info-circle-fill me-2"></i><strong>Academic Disclaimer:</strong> ${data.academicNotice}
                    </div>
                    <table class="table table-sm table-bordered">
                        <thead class="table-light">
                            <tr><th>Biometric Parameter</th><th>Normal Range</th><th>Warning Limits</th><th>Critical Limits</th></tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Heart Rate (BPM)</td>
                                <td>${t.heartRateNormalMin || 60} - ${t.heartRateNormalMax || 100}</td>
                                <td>&lt; ${t.heartRateNormalMin || 60} or &gt; ${t.heartRateNormalMax || 100}</td>
                                <td>&lt; ${t.heartRateCriticalMin || 40} or &gt; ${t.heartRateCriticalMax || 140}</td>
                            </tr>
                            <tr>
                                <td>Blood Oxygen (SpO2 %)</td>
                                <td>&ge; ${t.spo2NormalMin || 95}%</td>
                                <td>&lt; ${t.spo2NormalMin || 95}%</td>
                                <td>&lt; ${t.spo2CriticalMin || 90}%</td>
                            </tr>
                            <tr>
                                <td>Body Temperature (°C)</td>
                                <td>${t.tempNormalMin || 36.1} - ${t.tempNormalMax || 37.2} °C</td>
                                <td>&lt; ${t.tempNormalMin || 36.1} or &gt; ${t.tempNormalMax || 37.2} °C</td>
                                <td>&gt; ${t.tempCriticalMax || 39.5} °C</td>
                            </tr>
                            <tr>
                                <td>Systolic Blood Pressure (mmHg)</td>
                                <td>${t.systolicNormalMin || 90} - ${t.systolicNormalMax || 120}</td>
                                <td>121 - ${t.systolicCriticalMax || 180}</td>
                                <td>&gt; ${t.systolicCriticalMax || 180} or &lt; ${t.systolicCriticalMin || 70}</td>
                            </tr>
                            <tr>
                                <td>Diastolic Blood Pressure (mmHg)</td>
                                <td>${t.diastolicNormalMin || 60} - ${t.diastolicNormalMax || 80}</td>
                                <td>81 - ${t.diastolicCriticalMax || 110}</td>
                                <td>&gt; ${t.diastolicCriticalMax || 110} or &lt; ${t.diastolicCriticalMin || 40}</td>
                            </tr>
                        </tbody>
                    </table>
                `;
            }
            this.modals['thresholdsModal'].show();
        } catch (err) {
            UI.showToast('Thresholds Error', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    // =========================================================================
    // 6. ANALYTICS VIEW
    // =========================================================================
    renderAnalyticsView() {
        // Force chart resize/repaint when switching to analytics tab
        if (HealthCharts.telemetryTrendChart) HealthCharts.telemetryTrendChart.resize();
        if (HealthCharts.severityDistChart) HealthCharts.severityDistChart.resize();
        if (HealthCharts.fogWorkloadChart) HealthCharts.fogWorkloadChart.resize();
        if (HealthCharts.latencyComparisonChart) HealthCharts.latencyComparisonChart.resize();

        // Calculate analytics summary stats
        const total = this.readingsHistory.length;
        const filteredAtEdge = this.readingsHistory.filter(r => !r.cloudForwarded).length;
        const filterRatio = total > 0 ? ((filteredAtEdge / total) * 100).toFixed(1) : '75.0';

        const reductionEl = document.getElementById('analyticsBandwidthReduction');
        if (reductionEl) reductionEl.textContent = `${filterRatio}%`;

        const alertsGeneratedEl = document.getElementById('analyticsAlertsCount');
        if (alertsGeneratedEl) alertsGeneratedEl.textContent = this.alerts.length;
    },

    // =========================================================================
    // 7. FOG VS CLOUD COMPARISON VIEW (PHASE 7)
    // =========================================================================
    latestExperiment: null,
    comparisonHistory: [],

    async renderComparisonView() {
        if (!this.latestExperiment) {
            UI.showLoader();
            try {
                const latest = await API.getLatestComparison();
                if (latest && latest.experimentId) {
                    this.latestExperiment = latest;
                }
                const history = await API.getComparisonHistory();
                this.comparisonHistory = Array.isArray(history) ? history : [];
            } catch (err) {
                console.error('Error fetching comparison data:', err);
            } finally {
                UI.hideLoader();
            }
        }

        this.displayComparisonResults(this.latestExperiment);
        this.renderComparisonHistoryTable();
    },

    displayComparisonResults(exp) {
        if (!exp || !exp.cloudOnlyMetrics || !exp.fogBasedMetrics) {
            document.getElementById('comparisonResultsContainer')?.classList.add('d-none');
            document.getElementById('comparisonEmptyState')?.classList.remove('d-none');
            return;
        }

        document.getElementById('comparisonResultsContainer')?.classList.remove('d-none');
        document.getElementById('comparisonEmptyState')?.classList.add('d-none');

        const cloud = exp.cloudOnlyMetrics;
        const fog = exp.fogBasedMetrics;
        const summary = exp.summary || {};

        // Header info
        const expIdEl = document.getElementById('compExpId');
        if (expIdEl) expIdEl.textContent = `${exp.experimentId} (${exp.datasetSize} Readings)`;
        const expTimeEl = document.getElementById('compExpTimestamp');
        if (expTimeEl) expTimeEl.textContent = new Date(exp.timestamp).toLocaleString();

        // 1. Mode A: Cloud-Only Card
        document.getElementById('cloudTotalReadings').textContent = cloud.totalReadings;
        document.getElementById('cloudTransmissions').textContent = `${cloud.totalCloudTransmissions} (100%)`;
        document.getElementById('cloudFiltered').textContent = `${cloud.locallyFilteredReadings} (0%)`;
        document.getElementById('cloudJavaTime').textContent = `${cloud.totalJavaProcessingMs.toFixed(2)} ms (${cloud.avgJavaProcessingNs.toFixed(0)} ns/rd)`;
        document.getElementById('cloudNetworkLatency').textContent = `${cloud.estimatedTotalNetworkLatencyMs.toLocaleString()} ms (${cloud.estimatedAvgNetworkLatencyMsPerReading.toFixed(0)} ms/rd)`;
        document.getElementById('cloudBandwidth').textContent = `${cloud.estimatedBandwidthKB.toFixed(1)} KB (${cloud.estimatedBandwidthBytes.toLocaleString()} B)`;

        // 2. Mode B: Fog-Based Card
        document.getElementById('fogTotalReadings').textContent = fog.totalReadings;
        document.getElementById('fogTransmissions').textContent = `${fog.totalCloudTransmissions} (${(100 - fog.filterPercentage).toFixed(1)}%)`;
        document.getElementById('fogFiltered').textContent = `${fog.locallyFilteredReadings} (${fog.filterPercentage.toFixed(1)}%)`;
        document.getElementById('fogJavaTime').textContent = `${fog.totalJavaProcessingMs.toFixed(2)} ms (${fog.avgJavaProcessingNs.toFixed(0)} ns/rd)`;
        document.getElementById('fogNetworkLatency').textContent = `${fog.estimatedTotalNetworkLatencyMs.toLocaleString()} ms (${fog.estimatedAvgNetworkLatencyMsPerReading.toFixed(0)} ms/rd)`;
        document.getElementById('fogBandwidth').textContent = `${fog.estimatedBandwidthKB.toFixed(1)} KB (${fog.estimatedBandwidthBytes.toLocaleString()} B)`;

        // 3. Comparison Summary KPI Badges
        document.getElementById('compSavingsLatency').textContent = `${summary.networkLatencySavingsPercent.toFixed(1)}%`;
        document.getElementById('compSavingsLatencySub').textContent = `Saved ${summary.networkLatencySavingsMs.toLocaleString()} ms simulated network overhead`;
        document.getElementById('compSavingsBandwidth').textContent = `${summary.bandwidthSavingsPercent.toFixed(1)}%`;
        document.getElementById('compSavingsBandwidthSub').textContent = `Saved ${(summary.bandwidthSavingsBytes / 1024).toFixed(1)} KB payload transmission`;
        document.getElementById('compFilteredRate').textContent = `${summary.fogFilterRatePercent.toFixed(1)}%`;
        document.getElementById('compFilteredRateSub').textContent = `${summary.locallyFilteredByFog} of ${summary.totalReadings} routine readings filtered at edge`;
        document.getElementById('compJavaOverhead').textContent = `${summary.javaProcessingDifferenceMs > 0 ? '+' : ''}${summary.javaProcessingDifferenceMs.toFixed(2)} ms`;
        document.getElementById('compJavaOverheadSub').textContent = `Local edge cache & routing work (${summary.fasterProcessingMode === 'CLOUD_ONLY' ? 'Fog overhead' : 'Edge faster'})`;

        // Update Charts
        HealthCharts.updateComparisonExperiment(exp);
    },

    renderComparisonHistoryTable() {
        const tbody = document.getElementById('comparisonHistoryTableBody');
        if (!tbody) return;

        if (!this.comparisonHistory || this.comparisonHistory.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-3">No comparison experiments run yet. Click "Run Experiment" above.</td></tr>`;
            return;
        }

        tbody.innerHTML = this.comparisonHistory.map(exp => {
            const sum = exp.summary || {};
            const timeStr = exp.timestamp ? new Date(exp.timestamp).toLocaleTimeString() : 'N/A';
            return `
                <tr>
                    <td><span class="badge bg-light text-dark border font-monospace">${exp.experimentId}</span></td>
                    <td><small>${timeStr}</small></td>
                    <td>${exp.datasetSize} readings</td>
                    <td><span class="badge bg-success-subtle text-success">${sum.fogFilterRatePercent ? sum.fogFilterRatePercent.toFixed(1) : 0}% filtered</span></td>
                    <td><span class="fw-bold text-teal">${sum.networkLatencySavingsPercent ? sum.networkLatencySavingsPercent.toFixed(1) : 0}%</span></td>
                    <td><span class="fw-bold text-primary">${sum.bandwidthSavingsPercent ? sum.bandwidthSavingsPercent.toFixed(1) : 0}%</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-info" onclick="App.viewComparisonExperiment('${exp.experimentId}')">
                            <i class="bi bi-eye me-1"></i> View
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    },

    async handleRunComparison() {
        const select = document.getElementById('comparisonDatasetSelect');
        const datasetSize = select ? parseInt(select.value, 10) : 100;

        UI.showLoader();
        try {
            const exp = await API.runComparison(datasetSize);
            this.latestExperiment = exp;
            const history = await API.getComparisonHistory();
            this.comparisonHistory = Array.isArray(history) ? history : [];
            this.displayComparisonResults(exp);
            this.renderComparisonHistoryTable();
            UI.showToast('Comparison Experiment Completed', 
                `Experiment ${exp.experimentId} executed with ${datasetSize} readings. Fog filtered ${exp.summary.fogFilterRatePercent}% routine data locally.`, 
                'success'
            );
        } catch (err) {
            UI.showToast('Experiment Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async viewComparisonExperiment(experimentId) {
        UI.showLoader();
        try {
            const exp = await API.getComparisonById(experimentId);
            if (exp) {
                this.latestExperiment = exp;
                this.displayComparisonResults(exp);
                window.scrollTo({ top: 0, behavior: 'smooth' });
                UI.showToast('Experiment Loaded', `Viewing results for ${exp.experimentId}`, 'info');
            }
        } catch (err) {
            UI.showToast('Error', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    // =========================================================================
    // Simulation Engine Actions
    // =========================================================================
    async handleStartSimulation() {
        UI.showLoader();
        try {
            await API.startSimulation(3000, 'MIXED');
            UI.showToast('IoT Simulation Started', 'Healthcare IoT Sensors transmitting at 3000ms intervals.', 'success');
            await this.refreshAllData(false);
        } catch (err) {
            UI.showToast('Failed to start simulation', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async handleStopSimulation() {
        UI.showLoader();
        try {
            await API.stopSimulation();
            UI.showToast('IoT Simulation Stopped', 'Scheduled sensor telemetry paused.', 'info');
            await this.refreshAllData(false);
        } catch (err) {
            UI.showToast('Failed to stop simulation', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    },

    async handleInstantBatch() {
        UI.showLoader();
        try {
            // Trigger 1 reading per patient in mixed scenarios
            const samplePatients = this.patients.slice(0, 5);
            for (const p of samplePatients) {
                await API.generateSingleReading(p.patientId, 'MIXED');
            }
            UI.showToast('Batch Telemetry Streamed', `Dispatched instant telemetry batch across 5 registered patients.`, 'success');
            await this.refreshAllData(false);
        } catch (err) {
            UI.showToast('Batch Simulation Failed', err.message, 'danger');
        } finally {
            UI.hideLoader();
        }
    }
};

// Start application when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
