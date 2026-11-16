/**
 * Smart Healthcare Monitoring Using Fog Computing
 * Centralized REST API Client
 */

const API = {
    baseUrl: '/api',

    // Generic Fetch Wrapper with JSON handling and detailed error extraction
    async request(endpoint, options = {}) {
        const config = {
            headers: {
                'Accept': 'application/json',
                ...options.headers
            },
            ...options
        };

        if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
            config.headers['Content-Type'] = 'application/json';
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, config);
            const contentType = response.headers.get('content-type');
            let data = null;
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
                if (data && typeof data === 'object') {
                    errorMessage = data.message || data.error || JSON.stringify(data);
                } else if (typeof data === 'string' && data.length > 0) {
                    errorMessage = data;
                }
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, error);
            throw error;
        }
    },

    // 1. Health Endpoint
    async getHealth() {
        return this.request('/health');
    },

    // 2. Patient Management Endpoints
    async getPatients() {
        return this.request('/patients');
    },

    async getPatientById(id) {
        return this.request(`/patients/${encodeURIComponent(id)}`);
    },

    async createPatient(patientData) {
        return this.request('/patients', {
            method: 'POST',
            body: patientData
        });
    },

    async updatePatient(id, patientData) {
        return this.request(`/patients/${encodeURIComponent(id)}`, {
            method: 'PUT',
            body: patientData
        });
    },

    async deletePatient(id) {
        return this.request(`/patients/${encodeURIComponent(id)}`, {
            method: 'DELETE'
        });
    },

    // 3. Fog Node Management Endpoints
    async getFogNodes() {
        return this.request('/fog-nodes');
    },

    async getFogNodeById(nodeId) {
        return this.request(`/fog-nodes/${encodeURIComponent(nodeId)}`);
    },

    async getFogNodePatients(nodeId) {
        return this.request(`/fog-nodes/${encodeURIComponent(nodeId)}/patients`);
    },

    async updateFogNodeStatus(nodeId, status) {
        return this.request(`/fog-nodes/${encodeURIComponent(nodeId)}/status?status=${encodeURIComponent(status)}`, {
            method: 'PUT'
        });
    },

    async rebalanceFogWorkload() {
        return this.request('/fog-nodes/rebalance', {
            method: 'POST'
        });
    },

    async assignPatientToNode(patientId, nodeId) {
        return this.request(`/fog-nodes/assign?patientId=${encodeURIComponent(patientId)}&nodeId=${encodeURIComponent(nodeId)}`, {
            method: 'POST'
        });
    },

    async getFogNodeCache(nodeId, limit = 20) {
        return this.request(`/fog-nodes/${encodeURIComponent(nodeId)}/cache?limit=${limit}`);
    },

    async getFogCacheStats() {
        return this.request('/fog-nodes/cache/stats');
    },

    // 4. Simulation Engine Endpoints
    async getSimulationStatus() {
        return this.request('/simulation/status');
    },

    async startSimulation(intervalMs = 3000, mode = 'MIXED') {
        return this.request(`/simulation/start?intervalMs=${intervalMs}&mode=${encodeURIComponent(mode)}`, {
            method: 'POST'
        });
    },

    async stopSimulation() {
        return this.request('/simulation/stop', {
            method: 'POST'
        });
    },

    async generateSingleReading(patientId, scenario = 'MIXED') {
        return this.request(`/simulation/generate?patientId=${encodeURIComponent(patientId)}&scenario=${encodeURIComponent(scenario)}`, {
            method: 'POST'
        });
    },

    async submitManualReading(readingData) {
        return this.request('/simulation/submit-reading', {
            method: 'POST',
            body: readingData
        });
    },

    async getSimulationHistory(limit = 20) {
        return this.request(`/simulation/history?limit=${limit}`);
    },

    // 5. Emergency Alert Endpoints
    async getAllAlerts() {
        return this.request('/alerts');
    },

    async getActiveAlerts() {
        return this.request('/alerts/active');
    },

    async getHistoricalAlerts() {
        return this.request('/alerts/history');
    },

    async getAlertById(id) {
        return this.request(`/alerts/${encodeURIComponent(id)}`);
    },

    async acknowledgeAlert(id, clinicianName, notes = '') {
        const query = `clinicianName=${encodeURIComponent(clinicianName)}&notes=${encodeURIComponent(notes)}`;
        return this.request(`/alerts/${encodeURIComponent(id)}/acknowledge?${query}`, {
            method: 'PUT'
        });
    },

    async resolveAlert(id, clinicianName, notes = '', manual = true) {
        const query = `clinicianName=${encodeURIComponent(clinicianName)}&notes=${encodeURIComponent(notes)}&manual=${manual}`;
        return this.request(`/alerts/${encodeURIComponent(id)}/resolve?${query}`, {
            method: 'PUT'
        });
    },

    async getAlertThresholds() {
        return this.request('/alerts/thresholds');
    },

    // 6. Fog vs Cloud Performance Comparison Endpoints (Phase 7)
    async runComparison(datasetSize = 100) {
        return this.request(`/comparison/run?datasetSize=${encodeURIComponent(datasetSize)}`, {
            method: 'POST'
        });
    },

    async getLatestComparison() {
        return this.request('/comparison/latest');
    },

    async getComparisonHistory() {
        return this.request('/comparison/history');
    },

    async getComparisonById(experimentId) {
        return this.request(`/comparison/${encodeURIComponent(experimentId)}`);
    },

    async getComparisonMethodology() {
        return this.request('/comparison/methodology');
    }
};

// UI Notification and Feedback Utilities
const UI = {
    showToast(title, message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const iconClass = type === 'success' ? 'bi-check-circle-fill text-success' :
                          type === 'danger' ? 'bi-exclamation-triangle-fill text-danger' :
                          type === 'warning' ? 'bi-exclamation-circle-fill text-warning' :
                          'bi-info-circle-fill text-info';

        const toastEl = document.createElement('div');
        toastEl.className = `toast align-items-center border-0 shadow-sm`;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');
        toastEl.innerHTML = `
            <div class="toast-header">
                <i class="bi ${iconClass} me-2"></i>
                <strong class="me-auto">${title}</strong>
                <small class="text-muted">Just now</small>
                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        `;

        container.appendChild(toastEl);
        const bsToast = new bootstrap.Toast(toastEl, { delay: 4000 });
        bsToast.show();

        toastEl.addEventListener('hidden.bs.toast', () => {
            toastEl.remove();
        });
    },

    showLoader() {
        const loader = document.getElementById('loading-overlay');
        if (loader) {
            loader.classList.remove('d-none');
        }
    },

    hideLoader() {
        const loader = document.getElementById('loading-overlay');
        if (loader) {
            loader.classList.add('d-none');
        }
    }
};
