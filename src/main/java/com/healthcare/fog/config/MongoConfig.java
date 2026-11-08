package com.healthcare.fog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public CommandLineRunner mongoStartupVerifier(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                log.info("Checking initial MongoDB connectivity for database: '{}'...", 
                        mongoTemplate.getDb().getName());
                Document pingResult = mongoTemplate.executeCommand(new Document("ping", 1));
                log.info("MongoDB ping successful! Server response: {}", pingResult.toJson());
            } catch (Exception ex) {
                log.error("Failed to connect to MongoDB on startup: {}", ex.getMessage(), ex);
            }
        };
    }
}
