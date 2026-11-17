package com.healthcare.fog.repository;

import com.healthcare.fog.model.ComparisonExperiment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Phase 7 Fog vs Cloud comparison experiments.
 */
@Repository
public interface ComparisonExperimentRepository extends MongoRepository<ComparisonExperiment, String> {

    Optional<ComparisonExperiment> findByExperimentId(String experimentId);

    List<ComparisonExperiment> findAllByOrderByTimestampDesc();

    Optional<ComparisonExperiment> findFirstByOrderByTimestampDesc();
}
