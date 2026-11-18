package com.healthcare.fog.service;

import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.FogNodeRepository;
import com.healthcare.fog.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class FogLoadBalancerTest {

    private FogNodeRepository fogNodeRepository;
    private PatientRepository patientRepository;
    private FogLoadBalancer fogLoadBalancer;

    @BeforeEach
    void setUp() {
        fogNodeRepository = Mockito.mock(FogNodeRepository.class);
        patientRepository = Mockito.mock(PatientRepository.class);
        fogLoadBalancer = new FogLoadBalancer(fogNodeRepository, patientRepository);
    }

    @Test
    @DisplayName("selectBestNode should choose the least-loaded online Fog Node")
    void selectBestNode_shouldChooseLeastLoadedNode() {
        FogNode hospital = new FogNode("FOG-HOSPITAL-01", "Hospital", "Loc", "IP", 8081, "ONLINE", "HOSPITAL", 50);
        hospital.setCurrentWorkload(40); // 80%

        FogNode clinic = new FogNode("FOG-CLINIC-01", "Clinic", "Loc", "IP", 8082, "ONLINE", "CLINIC", 30);
        clinic.setCurrentWorkload(6); // 20%

        when(fogNodeRepository.findByStatus("ONLINE")).thenReturn(Arrays.asList(hospital, clinic));

        Patient generalPatient = new Patient();
        generalPatient.setPatientId("PAT-GENERAL");
        generalPatient.setMedicalHistory(Collections.singletonList("Routine physical"));

        FogNode chosen = fogLoadBalancer.selectBestNode(generalPatient);
        assertThat(chosen.getNodeId()).isEqualTo("FOG-CLINIC-01");
    }

    @Test
    @DisplayName("selectBestNode should route cardiac patient to Hospital Fog Node when available")
    void selectBestNode_shouldRouteCardiacToHospital() {
        FogNode hospital = new FogNode("FOG-HOSPITAL-01", "Hospital", "Loc", "IP", 8081, "ONLINE", "HOSPITAL", 50);
        hospital.setCurrentWorkload(10);

        FogNode clinic = new FogNode("FOG-CLINIC-01", "Clinic", "Loc", "IP", 8082, "ONLINE", "CLINIC", 30);
        clinic.setCurrentWorkload(2);

        when(fogNodeRepository.findByStatus("ONLINE")).thenReturn(Arrays.asList(hospital, clinic));

        Patient cardiacPatient = new Patient();
        cardiacPatient.setPatientId("PAT-CARDIO");
        cardiacPatient.setMedicalHistory(Arrays.asList("Severe Arrhythmia", "Atrial Fibrillation"));

        FogNode chosen = fogLoadBalancer.selectBestNode(cardiacPatient);
        assertThat(chosen.getNodeId()).isEqualTo("FOG-HOSPITAL-01");
    }

    @Test
    @DisplayName("selectBestNode throws exception when no Fog Nodes are online")
    void selectBestNode_shouldThrowWhenNoNodesOnline() {
        when(fogNodeRepository.findByStatus("ONLINE")).thenReturn(Collections.emptyList());

        Patient patient = new Patient();
        patient.setPatientId("PAT-001");

        assertThrows(IllegalStateException.class, () -> fogLoadBalancer.selectBestNode(patient));
    }
}
