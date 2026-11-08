package com.healthcare.fog.controller;

import com.healthcare.fog.dto.HealthStatusResponse;
import com.healthcare.fog.service.HealthCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    /**
     * Health check endpoint verifying overall application and MongoDB connectivity.
     *
     * @return 200 OK if service and database are healthy, 503 SERVICE_UNAVAILABLE otherwise.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthStatusResponse> getHealth() {
        HealthStatusResponse response = healthCheckService.performHealthCheck();
        
        if ("UP".equalsIgnoreCase(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
