package com.healthcare.fog.repository;

import com.healthcare.fog.model.EmergencyAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyAlertRepository extends MongoRepository<EmergencyAlert, String> {

    Optional<EmergencyAlert> findByAlertId(String alertId);

    List<EmergencyAlert> findByPatientId(String patientId);

    List<EmergencyAlert> findByFogNodeId(String fogNodeId);

    List<EmergencyAlert> findByAlertLevel(String alertLevel);

    List<EmergencyAlert> findByStatus(String status);

    List<EmergencyAlert> findByResolvedFalseOrderByTimestampDesc();

    List<EmergencyAlert> findByResolvedTrueOrderByTimestampDesc();

    List<EmergencyAlert> findByPatientIdOrderByTimestampDesc(String patientId);

    List<EmergencyAlert> findByAcknowledgedFalseOrderByTimestampDesc();
}
