package com.daily.cetaring.features.health.controller;

import com.daily.cetaring.features.health.dto.HealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthControllerTest {

    @Test
    void healthReturnsUpWhenDatabaseResponds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        HealthController controller = new HealthController(jdbcTemplate);

        ResponseEntity<HealthResponse> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().getStatus());
        assertEquals("CONNECTED", response.getBody().getDatabase());
        assertNotNull(response.getBody().getTime());
    }

    @Test
    void healthReturnsDownWhenDatabaseFails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(new IllegalStateException("db down"));
        HealthController controller = new HealthController(jdbcTemplate);

        ResponseEntity<HealthResponse> response = controller.health();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DOWN", response.getBody().getStatus());
        assertEquals("DISCONNECTED", response.getBody().getDatabase());
    }

    @Test
    void liveReturnsUpWithoutCheckingDatabase() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        HealthController controller = new HealthController(jdbcTemplate);

        ResponseEntity<HealthResponse> response = controller.live();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().getStatus());
        assertEquals("NOT_CHECKED", response.getBody().getDatabase());
        assertNotNull(response.getBody().getTime());
    }
}
