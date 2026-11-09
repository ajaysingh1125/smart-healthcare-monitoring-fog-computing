package com.healthcare.fog.repository;

import com.healthcare.fog.model.FogNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FogNodeRepository extends MongoRepository<FogNode, String> {

    Optional<FogNode> findByNodeId(String nodeId);

    boolean existsByNodeId(String nodeId);

    List<FogNode> findByStatus(String status);
}
