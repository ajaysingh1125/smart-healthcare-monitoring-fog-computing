package com.healthcare.fog.service;

import com.healthcare.fog.dto.DatabaseHealth;
import com.healthcare.fog.dto.HealthStatusResponse;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final MongoTemplate mongoTemplate;

    @Value("${spring.application.name:smart-healthcare-fog-node}")
    private String applicationName;

    @Value("${spring.data.mongodb.host:localhost}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port:27017}")
    private String mongoPort;

    @Value("${spring.data.mongodb.database:smart_healthcare}")
    private String expectedDbName;

    public HealthCheckService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public HealthStatusResponse performHealthCheck() {
        DatabaseHealth dbHealth = checkMongoHealth();

        boolean isDbHealthy = "UP".equalsIgnoreCase(dbHealth.getStatus());
        String overallStatus = isDbHealthy ? "UP" : "DOWN";

        Map<String, Object> systemDetails = new LinkedHashMap<>();
        systemDetails.put("javaVersion", System.getProperty("java.version"));
        systemDetails.put("javaVendor", System.getProperty("java.vendor"));
        systemDetails.put("osName", System.getProperty("os.name"));
        systemDetails.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemDetails.put("freeMemoryBytes", Runtime.getRuntime().freeMemory());
        systemDetails.put("totalMemoryBytes", Runtime.getRuntime().totalMemory());

        return new HealthStatusResponse(
                overallStatus,
                applicationName,
                "Fog Computing Monitoring Gateway",
                dbHealth,
                systemDetails
        );
    }

    private DatabaseHealth checkMongoHealth() {
        String dbName = expectedDbName;
        String hostEndpoint = mongoHost + ":" + mongoPort;
        long startTime = System.currentTimeMillis();

        try {
            if (mongoTemplate.getDb() != null) {
                dbName = mongoTemplate.getDb().getName();
            }

            Document pingCmd = new Document("ping", 1);
            Document result = mongoTemplate.executeCommand(pingCmd);
            long latency = System.currentTimeMillis() - startTime;

            Number ok = result.get("ok", Number.class);
            if (ok != null && ok.doubleValue() == 1.0) {
                log.debug("MongoDB health ping OK ({} ms)", latency);
                return new DatabaseHealth(
                        "UP",
                        dbName,
                        hostEndpoint,
                        latency,
                        "MongoDB Community Server connection verified and responsive"
                );
            } else {
                log.warn("MongoDB returned unexpected ping response: {}", result);
                return new DatabaseHealth(
                        "DOWN",
                        dbName,
                        hostEndpoint,
                        latency,
                        "Ping completed but returned status: " + result.toJson()
                );
            }
        } catch (Exception ex) {
            long latency = System.currentTimeMillis() - startTime;
            log.error("MongoDB health check ping failed: {}", ex.getMessage());
            return new DatabaseHealth(
                    "DOWN",
                    dbName,
                    hostEndpoint,
                    latency,
                    "MongoDB connectivity failure: " + ex.getMessage()
            );
        }
    }
}
