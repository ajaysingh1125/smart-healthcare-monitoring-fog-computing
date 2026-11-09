package com.healthcare.fog.repository;

import com.healthcare.fog.model.SensorDevice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDeviceRepository extends MongoRepository<SensorDevice, String> {

    Optional<SensorDevice> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);

    List<SensorDevice> findByAssignedPatientId(String assignedPatientId);

    List<SensorDevice> findByPairedFogNodeId(String pairedFogNodeId);

    List<SensorDevice> findByStatus(String status);
}
