# Smart Healthcare Monitoring Using Fog Computing

> **College Project** — Full-stack IoT Healthcare Monitoring System demonstrating a 3-tier Fog Computing Architecture using Java 17, Spring Boot 3.3.5, and MongoDB.

---

## Architecture Overview

```
IoT Sensors → Fog Node Layer (Edge Inference) → Cloud/MongoDB Storage
```

| Tier | Technology | Role |
|---|---|---|
| Perception (IoT) | Java Simulation Engine | HR, Temp, SpO2, BP generation |
| Fog Layer | Spring Boot Services | Local edge inference, load balancing, caching |
| Cloud/Storage | MongoDB Community Server | Persistent records, alert history |

---

## Technology Stack

| Component | Version |
|---|---|
| Java | 17 (Eclipse Adoptium Temurin 17.0.21) |
| Spring Boot | 3.3.5 |
| Apache Maven | 3.9.15 |
| MongoDB Community | 8.3.4 |
| Spring Data MongoDB | Included |
| Bootstrap | 5.3.2 |
| Chart.js | 4.4.1 |

---

## Project Structure

```
smart-healthcare-fog/
├── src/main/java/com/healthcare/fog/
│   ├── controller/          # REST API Controllers (5)
│   │   ├── HealthCheckController.java
│   │   ├── PatientController.java
│   │   ├── FogNodeController.java
│   │   ├── SimulationController.java
│   │   └── AlertController.java
│   ├── model/               # MongoDB Document Models
│   │   ├── Patient.java
│   │   ├── HealthReading.java
│   │   ├── FogNode.java
│   │   ├── EmergencyAlert.java
│   │   └── SensorDevice.java
│   ├── service/             # Core Business Logic
│   │   ├── FogProcessingService.java      # Edge inference engine
│   │   ├── FogLoadBalancer.java           # Least-loaded scheduling
│   │   ├── FogLocalCache.java             # Ring-buffer edge cache
│   │   ├── FogNodeService.java            # Online/offline + failover
│   │   ├── SensorSimulationService.java   # IoT simulation engine
│   │   ├── AbnormalityDetectionService.java
│   │   ├── EmergencyAlertService.java
│   │   └── PatientService.java
│   ├── repository/          # Spring Data MongoDB Repositories (5)
│   ├── dto/                 # Request/Response DTOs
│   └── exception/           # Global Exception Handler
├── src/main/resources/
│   ├── static/              # Phase 6 Frontend (SPA)
│   │   ├── index.html
│   │   ├── css/custom.css
│   │   └── js/
│   │       ├── api.js       # Centralized REST client
│   │       ├── charts.js    # Chart.js visualizations
│   │       └── app.js       # SPA navigation controller
│   └── application.properties
└── src/test/                # 42 Unit + Integration Tests
```

---

## How to Run

### Prerequisites
1. **Java 17** installed and on PATH  
2. **MongoDB Community Server** running on `localhost:27017`  
3. **Apache Maven** (project uses `C:\Users\ChangeMe\apache-maven-3.9.15\bin\mvn.cmd`)

### Build & Run

```powershell
# 1. Navigate to project
cd C:\Users\ChangeMe\.gemini\antigravity\scratch\smart-healthcare-fog

# 2. Build (skip tests)
C:\Users\ChangeMe\apache-maven-3.9.15\bin\mvn.cmd clean package -DskipTests

# 3. Run the JAR
java -jar target\smart-healthcare-fog-0.0.1-SNAPSHOT.jar
```

### Open the Dashboard

```
http://localhost:8080/
```

---

## REST API Reference

### Health
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/health` | System health + MongoDB ping latency |

### Patient Management
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/patients` | All registered patients |
| GET | `/api/patients/{id}` | Patient by ID |
| POST | `/api/patients` | Register new patient |
| PUT | `/api/patients/{id}` | Update patient |
| DELETE | `/api/patients/{id}` | Delete patient |

### Fog Node Cluster
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/fog-nodes` | All 3 fog node statuses |
| GET | `/api/fog-nodes/{nodeId}` | Node detail |
| GET | `/api/fog-nodes/{nodeId}/patients` | Patients managed by node |
| PUT | `/api/fog-nodes/{nodeId}/status?status=OFFLINE` | Take node offline (triggers failover) |
| POST | `/api/fog-nodes/rebalance` | Re-balance patient distribution |
| POST | `/api/fog-nodes/ingest` | Ingest raw sensor telemetry |
| GET | `/api/fog-nodes/{nodeId}/cache?limit=20` | Inspect edge ring-buffer cache |
| GET | `/api/fog-nodes/cache/stats` | Aggregate cache statistics |

### IoT Sensor Simulation
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/simulation/start?intervalMs=3000&mode=MIXED` | Start scheduled simulation |
| POST | `/api/simulation/stop` | Stop simulation |
| GET | `/api/simulation/status` | Engine status |
| POST | `/api/simulation/generate?patientId=PAT-1001&scenario=CRITICAL` | Single on-demand reading |
| POST | `/api/simulation/submit-reading` | Manual telemetry via JSON body |
| GET | `/api/simulation/history?limit=20` | Recent simulation output |

### Emergency Alerts
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/alerts` | All alerts |
| GET | `/api/alerts/active` | Active unresolved alerts |
| GET | `/api/alerts/history` | Resolved historical alerts |
| GET | `/api/alerts/{id}` | Alert detail |
| PUT | `/api/alerts/{id}/acknowledge?clinicianName=Dr.X` | Acknowledge alert |
| PUT | `/api/alerts/{id}/resolve?clinicianName=Dr.X&notes=...` | Resolve alert |
| GET | `/api/alerts/thresholds` | Configured detection thresholds |

### Phase 7: Fog vs Cloud Performance Comparison
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/comparison/run?datasetSize=100` | Execute dual-mode controlled experiment on synthetic dataset |
| GET | `/api/comparison/latest` | Retrieve latest comparison experiment result |
| GET | `/api/comparison/history` | Retrieve full history of comparison experiments from MongoDB |
| GET | `/api/comparison/{experimentId}` | Retrieve specific experiment by ID (e.g., `COMP-0001`) |
| GET | `/api/comparison/methodology` | Retrieve measurement transparency notes, constants, and disclaimer |

---

## Performance Comparison: Mode A vs Mode B

```
Mode A (Cloud-Only):
IoT Sensor ───────────────────────────────────────► Cloud Datacenter (WAN latency per reading)
All readings transmitted across WAN | 0% locally filtered | 100% bandwidth consumed

Mode B (Fog-Based):
IoT Sensor ──► Fog Node (Sub-10ms Local Inference)
                   │
                   ├── Normal Readings ──────────► Locally Filtered & Aggregated (0 WAN latency)
                   └── Warning/Critical Readings ─► Immediate Cloud Forwarding
```

### Measurement Transparency
- **Java Processing Time:** **ACTUALLY MEASURED** with nanosecond precision using `System.nanoTime()` on the JVM.
- **Network Latency:** **SIMULATED ESTIMATE** calculated using a representative WAN round-trip constant (180ms RTT) per cloud transmission.
- **Bandwidth:** **SIMULATED ESTIMATE** calculated assuming 512 bytes per JSON health reading payload.
- **Reproducibility:** Both modes process the **exact same synthetic dataset** generated with a fixed random seed (`Random(42L)`).

---

## Fog Nodes

| Node ID | Type | Capacity | Specialty |
|---|---|---|---|
| `FOG-HOSPITAL-01` | Hospital Core | 50 patients | Cardiac + ICU |
| `FOG-CLINIC-01` | Outpatient Clinic | 30 patients | Routine monitoring |
| `FOG-EMERGENCY-01` | Emergency & Trauma | 20 patients | Critical escalation |

**Load Balancing:** Least-loaded scheduling with clinical condition affinity.  
**Failover:** When a node goes OFFLINE, all its patients are automatically reassigned to surviving online nodes.  
**Local Cache:** Bounded ConcurrentLinkedDeque ring-buffer storing the last N readings per node.

---

## Abnormality Detection Thresholds

> **Academic Demonstration Notice:** These thresholds are calibrated for college project simulation purposes only and are NOT clinical diagnostic criteria.

| Vital | Normal | Warning | Critical |
|---|---|---|---|
| Heart Rate | 60–100 BPM | Outside normal | < 40 or > 140 BPM |
| SpO2 | ≥ 95% | < 95% | < 90% |
| Temperature | 36.1–37.2 °C | Outside normal | > 39.5 °C |
| Systolic BP | 90–120 mmHg | 121–180 | > 180 or < 70 mmHg |
| Diastolic BP | 60–80 mmHg | 81–110 | > 110 or < 40 mmHg |

**Safety Rule:** CRITICAL alerts cannot be automatically resolved — manual clinician attribution is mandatory.

---

## Test Suite

```
47 total tests | 0 failures | 0 errors

HealthCheckControllerTest          (2 tests)
PatientControllerTest              (8 tests)
PatientRepositoryIntegrationTest   (1 test)
FogLoadBalancerTest                (3 tests)
FogNodeFailoverTest                (1 test)
FogProcessingServiceTest           (2 tests)
FogNodeControllerTest              (3 tests)
SensorSimulationServiceTest        (3 tests)
SimulationControllerTest           (5 tests)
AbnormalityDetectionServiceTest    (5 tests)
EmergencyAlertServiceTest          (4 tests)
AlertControllerTest                (4 tests)
FogCloudComparisonServiceTest      (2 tests)
ComparisonControllerTest           (3 tests)
SmartHealthcareFogApplicationTests (1 test)
```

Run tests:
```powershell
C:\Users\ChangeMe\apache-maven-3.9.15\bin\mvn.cmd test -f pom.xml
```

---

## Frontend Views

| View | URL Fragment | Description |
|---|---|---|
| Dashboard | `#dashboard` | KPI cards + live edge telemetry activity stream |
| Patient Management | `#patients` | CRUD table with search, add, edit, delete |
| Live Monitoring | `#monitoring` | Vital display cards + scenario triggers + telemetry history |
| Fog Node Cluster | `#fognodes` | Node cards, workload progress bars, online/offline toggle |
| Emergency Alerts | `#alerts` | Alert table with acknowledge and resolve workflows |
| Edge Analytics | `#analytics` | Chart.js: telemetry trends, severity distribution, workload, latency |
| Fog vs Cloud (Phase 7) | `#comparison` | Side-by-side Mode A vs Mode B metrics, log-scale latency chart, bandwidth chart, run controls |

---

## Phases Implemented

| Phase | Description | Status |
|---|---|---|
| Phase 1 | Spring Boot + MongoDB initialization, `/api/health` | ✅ Complete |
| Phase 2 | MongoDB document models, Patient CRUD, 10 seed patients | ✅ Complete |
| Phase 3 | Fog Computing architecture: 3 nodes, load balancer, ring cache, failover | ✅ Complete |
| Phase 4 | IoT Sensor Simulation Engine (5 vitals, scheduling, pipeline) | ✅ Complete |
| Phase 5 | Abnormality Detection + Emergency Alert System | ✅ Complete |
| Phase 6 | Professional Bootstrap 5 + Chart.js frontend SPA | ✅ Complete |
| Phase 7 | Fog vs Cloud Performance Comparison (Mode A vs Mode B, APIs, UI graphs) | ✅ Complete |
| Phase 8 | Complete QA testing, debugging, and end-to-end integration audit | ✅ Complete |
