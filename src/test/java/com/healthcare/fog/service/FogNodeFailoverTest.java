package com.healthcare.fog.service;

import com.healthcare.fog.model.FogNode;
import com.healthcare.fog.model.Patient;
import com.healthcare.fog.repository.FogNodeRepository;
import com.healthcare.fog.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FogNodeFailoverTest {

    private FogNodeRepository fogNodeRepository;
    private PatientRepository patientRepository;
    private FogLoadBalancer fogLoadBalancer;
    private FogLocalCache fogLocalCache;
    private PatientService patientService;
    private FogNodeService fogNodeService;

    @BeforeEach
    void setUp() {
        fogNodeRepository = Mockito.mock(FogNodeRepository.class);
        patientRepository = Mockito.mock(PatientRepository.class);
        fogLoadBalancer = Mockito.mock(FogLoadBalancer.class);
        fogLocalCache = Mockito.mock(FogLocalCache.class);
        patientService = Mockito.mock(PatientService.class);

        fogNodeService = new FogNodeService(
                fogNodeRepository, patientRepository, fogLoadBalancer, fogLocalCache, patientService
        );
    }

    @Test
    @DisplayName("updateNodeStatus to OFFLINE should trigger automated patient failover and reassignment")
    void updateNodeStatus_offline_shouldTriggerFailover() {
        FogNode failingNode = new FogNode("FOG-CLINIC-01", "Clinic Node", "Loc", "IP", 8082, "ONLINE", "CLINIC", 30);
        failingNode.setManagedPatientIds(new ArrayList<>(Arrays.asList("PAT-1002", "PAT-1004")));
        failingNode.setCurrentWorkload(2);

        FogNode backupNode = new FogNode("FOG-HOSPITAL-01", "Hospital Core", "Ward B", "IP", 8081, "ONLINE", "HOSPITAL", 50);

        Patient p1 = new Patient();
        p1.setPatientId("PAT-1002");
        p1.setAssignedFogNodeId("FOG-CLINIC-01");

        Patient p2 = new Patient();
        p2.setPatientId("PAT-1004");
        p2.setAssignedFogNodeId("FOG-CLINIC-01");

        when(fogNodeRepository.findByNodeId("FOG-CLINIC-01")).thenReturn(Optional.of(failingNode));
        when(patientRepository.findByAssignedFogNodeId("FOG-CLINIC-01")).thenReturn(Arrays.asList(p1, p2));
        when(fogLoadBalancer.selectBestNode(any(Patient.class))).thenReturn(backupNode);
        when(fogLoadBalancer.assignPatientToNode(any(), any())).thenReturn(backupNode);

        Map<String, Object> result = fogNodeService.updateNodeStatus("FOG-CLINIC-01", "OFFLINE");

        assertThat(result.get("failoverTriggered")).isEqualTo(true);
        assertThat(result.get("affectedPatientsCount")).isEqualTo(2);
        assertThat(failingNode.getStatus()).isEqualTo("OFFLINE");
        assertThat(failingNode.getCurrentWorkload()).isEqualTo(0);

        verify(fogLocalCache).evictNodeCache("FOG-CLINIC-01");
        verify(fogLoadBalancer).assignPatientToNode("PAT-1002", "FOG-HOSPITAL-01");
        verify(fogLoadBalancer).assignPatientToNode("PAT-1004", "FOG-HOSPITAL-01");
    }
}
