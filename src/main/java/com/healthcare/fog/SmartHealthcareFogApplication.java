package com.healthcare.fog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
public class SmartHealthcareFogApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartHealthcareFogApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SmartHealthcareFogApplication.class);
        Environment env = app.run(args).getEnvironment();
        logStartupDetails(env);
    }

    private static void logStartupDetails(Environment env) {
        String serverPort = env.getProperty("server.port", "8080");
        String appName = env.getProperty("spring.application.name", "smart-healthcare-fog-node");
        String dbName = env.getProperty("spring.data.mongodb.database", "smart_healthcare");
        String dbHost = env.getProperty("spring.data.mongodb.host", "localhost");
        String dbPort = env.getProperty("spring.data.mongodb.port", "27017");

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Local host address resolution failed: {}", e.getMessage());
        }

        log.info("----------------------------------------------------------");
        log.info("Application '{}' is running!", appName);
        log.info("Local Access URL       : http://localhost:{}/api/health", serverPort);
        log.info("External Access URL    : http://{}:{}/api/health", hostAddress, serverPort);
        log.info("Target MongoDB Database: {} ({}:{})", dbName, dbHost, dbPort);
        log.info("Java Runtime Version   : {}", System.getProperty("java.version"));
        log.info("Architecture           : Fog Computing Gateway / Processing Node");
        log.info("IoT Simulation Engine  : Enabled (Spring Scheduling Active)");
        log.info("----------------------------------------------------------");
    }
}
