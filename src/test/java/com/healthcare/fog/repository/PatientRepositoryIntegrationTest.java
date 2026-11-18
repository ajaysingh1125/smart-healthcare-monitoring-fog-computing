package com.healthcare.fog.repository;

import com.healthcare.fog.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PatientRepositoryIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Verify Spring Data MongoDB CRUD and query methods")
    void testPatientMongoCrud() {
        String testPatientId = "TEST-PAT-" + System.currentTimeMillis();
        Patient patient = new Patient(
                testPatientId,
                "Test Patient",
                42,
                "MALE",
                "+1-999-" + (System.currentTimeMillis() % 10000000),
                Arrays.asList("Test Condition"),
                LocalDateTime.now(),
                "FOG-TEST-01",
                "Contact Name",
                "+1-999-0000"
        );

        // 1. Create / Save
        Patient saved = patientRepository.save(patient);
        assertThat(saved.getId()).isNotNull();

        // 2. Read by patientId
        Optional<Patient> found = patientRepository.findByPatientId(testPatientId);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Patient");

        // 3. Update
        found.get().setAge(43);
        patientRepository.save(found.get());
        Optional<Patient> updated = patientRepository.findByPatientId(testPatientId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getAge()).isEqualTo(43);

        // 4. Delete
        patientRepository.delete(updated.get());
        Optional<Patient> deleted = patientRepository.findByPatientId(testPatientId);
        assertThat(deleted).isEmpty();
    }
}
