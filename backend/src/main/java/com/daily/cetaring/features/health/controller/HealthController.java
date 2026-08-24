package com.daily.cetaring.features.health.controller;

import com.daily.cetaring.features.health.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        String databaseStatus = isDatabaseConnected() ? "CONNECTED" : "DISCONNECTED";
        HealthResponse response = HealthResponse.builder()
            .status("CONNECTED".equals(databaseStatus) ? "UP" : "DOWN")
            .database(databaseStatus)
            .time(LocalDateTime.now())
            .build();

        HttpStatus status = "UP".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/live")
    public ResponseEntity<HealthResponse> live() {
        HealthResponse response = HealthResponse.builder()
            .status("UP")
            .database("NOT_CHECKED")
            .time(LocalDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }

    private boolean isDatabaseConnected() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (Exception ex) {
            log.error("Health check database connectivity failed: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
