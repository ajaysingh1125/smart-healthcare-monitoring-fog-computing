package com.healthcare.fog.config;

import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.model.SensorDevice;
import com.healthcare.fog.repository.FogNodeRepository;
import com.healthcare.fog.repository.PatientRepository;
import com.healthcare.fog.repository.SensorDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Order(10)
    public CommandLineRunner seedDatabase(PatientRepository patientRepository,
                                          FogNodeRepository fogNodeRepository,
                                          SensorDeviceRepository sensorDeviceRepository) {
        return args -> {
            // ================================================================
            // 1. Seed the Three Functional Fog Nodes
            // ================================================================
            if (fogNodeRepository.count() == 0) {
                log.info("MongoDB collection 'fog_nodes' is empty. Seeding the 3 Functional Fog Nodes...");

                List<FogNode> fogNodes = Arrays.asList(
                        new FogNode(
                                "FOG-HOSPITAL-01",
                                "Hospital Core Fog Node",
                                "St. Jude Memorial Hospital - Acute Inpatient & ICU",
                                "192.168.10.10",
                                8081,
                                "ONLINE",
                                "HOSPITAL",
                                50
                        ),
                        new FogNode(
                                "FOG-CLINIC-01",
                                "Community Clinic Fog Node",
                                "Metro Health Outpatient Center - Ambulatory Gateway",
                                "192.168.20.10",
                                8082,
                                "ONLINE",
                                "CLINIC",
                                30
                        ),
                        new FogNode(
                                "FOG-EMERGENCY-01",
                                "Emergency & Trauma Fog Node",
                                "Mobile Emergency Response Vehicle & Rapid Triage",
                                "192.168.30.10",
                                8083,
                                "ONLINE",
                                "EMERGENCY",
                                20
                        )
                );

                fogNodeRepository.saveAll(fogNodes);
                log.info("Successfully configured and seeded 3 Fog Nodes into MongoDB.");
            } else {
                log.info("MongoDB collection 'fog_nodes' already contains {} records.", fogNodeRepository.count());
            }

            // ================================================================
            // 2. Seed 10 Fictional Sample Patients
            // ================================================================
            if (patientRepository.count() == 0) {
                log.info("MongoDB collection 'patients' is empty. Seeding 10 fictional sample patients...");

                List<Patient> samplePatients = Arrays.asList(
                        new Patient(
                                "PAT-1001", "Eleanor Vance", 68, "FEMALE", "+1-555-0101",
                                Arrays.asList("Hypertension", "Atrial Fibrillation"),
                                LocalDateTime.now().minusDays(30), "FOG-HOSPITAL-01",
                                "Thomas Vance", "+1-555-0102"
                        ),
                        new Patient(
                                "PAT-1002", "Marcus Chen", 45, "MALE", "+1-555-0201",
                                Arrays.asList("Type 2 Diabetes", "Asthma"),
                                LocalDateTime.now().minusDays(25), "FOG-CLINIC-01",
                                "Mei Chen", "+1-555-0202"
                        ),
                        new Patient(
                                "PAT-1003", "Amina Al-Mansoor", 52, "FEMALE", "+1-555-0301",
                                Arrays.asList("Coronary Artery Disease", "Mild Arrhythmia"),
                                LocalDateTime.now().minusDays(20), "FOG-HOSPITAL-01",
                                "Tariq Al-Mansoor", "+1-555-0302"
                        ),
                        new Patient(
                                "PAT-1004", "James O'Connor", 74, "MALE", "+1-555-0401",
                                Arrays.asList("Chronic Obstructive Pulmonary Disease (COPD)", "Hypertension"),
                                LocalDateTime.now().minusDays(18), "FOG-CLINIC-01",
                                "Kathleen O'Connor", "+1-555-0402"
                        ),
                        new Patient(
                                "PAT-1005", "Sophia Rodriguez", 29, "FEMALE", "+1-555-0501",
                                Arrays.asList("Post-Operative Appendectomy Monitoring", "Mild Tachycardia"),
                                LocalDateTime.now().minusDays(14), "FOG-EMERGENCY-01",
                                "Carlos Rodriguez", "+1-555-0502"
                        ),
                        new Patient(
                                "PAT-1006", "David Kim", 38, "MALE", "+1-555-0601",
                                Arrays.asList("Sleep Apnea", "Pre-hypertension"),
                                LocalDateTime.now().minusDays(10), "FOG-CLINIC-01",
                                "Grace Kim", "+1-555-0602"
                        ),
                        new Patient(
                                "PAT-1007", "Fatima Zahra", 61, "FEMALE", "+1-555-0701",
                                Arrays.asList("Hyperlipidemia", "Chronic Kidney Disease Stage 2"),
                                LocalDateTime.now().minusDays(8), "FOG-CLINIC-01",
                                "Bilal Zahra", "+1-555-0702"
                        ),
                        new Patient(
                                "PAT-1008", "Robert Sterling", 81, "MALE", "+1-555-0801",
                                Arrays.asList("Congestive Heart Failure", "Osteoarthritis"),
                                LocalDateTime.now().minusDays(5), "FOG-HOSPITAL-01",
                                "Evelyn Sterling", "+1-555-0802"
                        ),
                        new Patient(
                                "PAT-1009", "Priya Sharma", 34, "FEMALE", "+1-555-0901",
                                Arrays.asList("Gestational Hypertension Monitoring"),
                                LocalDateTime.now().minusDays(3), "FOG-CLINIC-01",
                                "Arun Sharma", "+1-555-0902"
                        ),
                        new Patient(
                                "PAT-1010", "Lucas Mueller", 57, "MALE", "+1-555-1001",
                                Arrays.asList("Ischemic Stroke Recovery", "Dyslipidemia"),
                                LocalDateTime.now().minusDays(1), "FOG-EMERGENCY-01",
                                "Greta Mueller", "+1-555-1002"
                        )
                );

                patientRepository.saveAll(samplePatients);
                log.info("Successfully seeded 10 sample patients into MongoDB 'smart_healthcare.patients'.");
            } else {
                log.info("MongoDB collection 'patients' already contains {} records.", patientRepository.count());
            }

            // ================================================================
            // 3. Seed IoT Healthcare Sensor Devices (Phase 4)
            // ================================================================
            if (sensorDeviceRepository.count() == 0) {
                log.info("MongoDB collection 'sensor_devices' is empty. Seeding IoT biomedical sensors for patients...");
                List<Patient> patients = patientRepository.findAll();
                List<SensorDevice> sensorDevices = new ArrayList<>();

                for (Patient p : patients) {
                    sensorDevices.add(new SensorDevice(
                            "SENS-BIO-" + p.getPatientId(),
                            "Multi-Vital Biomedical Patch (" + p.getName() + ")",
                            "MULTI_PARAM_TELEMETRY",
                            "MedTech FogSensors Inc.",
                            p.getPatientId(),
                            p.getAssignedFogNodeId() != null ? p.getAssignedFogNodeId() : "FOG-HOSPITAL-01",
                            95
                    ));
                }

                sensorDeviceRepository.saveAll(sensorDevices);
                log.info("Successfully seeded {} IoT Sensor Devices into MongoDB.", sensorDevices.size());
            } else {
                log.info("MongoDB collection 'sensor_devices' already contains {} records.", sensorDeviceRepository.count());
            }

            // ================================================================
            // 4. Synchronize Fog Node Workloads and Managed Patient Lists
            // ================================================================
            List<FogNode> allNodes = fogNodeRepository.findAll();
            for (FogNode node : allNodes) {
                List<Patient> assigned = patientRepository.findByAssignedFogNodeId(node.getNodeId());
                List<String> patientIds = assigned.stream().map(Patient::getPatientId).collect(Collectors.toList());
                node.setManagedPatientIds(patientIds);
                node.setCurrentWorkload(patientIds.size());
                fogNodeRepository.save(node);
                log.info("Synchronized Fog Node '{}' (Type: {}): {} assigned patients (Workload: {}/{})",
                        node.getNodeId(), node.getNodeType(), patientIds.size(),
                        node.getCurrentWorkload(), node.getMaxCapacity());
            }
        };
    }
}
