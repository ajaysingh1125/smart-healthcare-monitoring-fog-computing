package com.healthcare.fog.repository;

import com.healthcare.fog.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {

    Optional<Patient> findByPatientId(String patientId);

    boolean existsByPatientId(String patientId);

    boolean existsByContactNumber(String contactNumber);

    boolean existsByContactNumberAndIdNot(String contactNumber, String id);

    boolean existsByPatientIdAndIdNot(String patientId, String id);

    List<Patient> findByAssignedFogNodeId(String assignedFogNodeId);

    List<Patient> findByActive(Boolean active);
}
