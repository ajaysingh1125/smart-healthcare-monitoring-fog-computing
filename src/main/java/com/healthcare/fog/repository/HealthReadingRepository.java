package com.healthcare.fog.repository;

import com.healthcare.fog.model.HealthReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HealthReadingRepository extends MongoRepository<HealthReading, String> {

    List<HealthReading> findByPatientId(String patientId);

    List<HealthReading> findByPatientIdOrderByTimestampDesc(String patientId, Pageable pageable);

    List<HealthReading> findByFogNodeId(String fogNodeId);

    List<HealthReading> findByStatus(String status);

    List<HealthReading> findByPatientIdAndTimestampBetween(String patientId, Instant start, Instant end);
}
