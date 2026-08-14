package com.daily.cetaring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway migration strategy that runs {@code repair()} before {@code migrate()}.
 *
 * <p>Purpose: if a previous deployment left a FAILED migration in
 * {@code flyway_schema_history} (e.g. V12 which failed due to a schema
 * mismatch with V1's {@code otps} table), {@code repair()} removes that
 * entry so the corrected migration SQL can be applied cleanly.
 *
 * <p>Safety: {@code repair()} is a no-op when there are no failed or
 * checksum-mismatched entries; it never removes successfully applied migrations.
 */
@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Flyway: running repair() to clear any failed migration records before migrate()");
            flyway.repair();
            flyway.migrate();
        };
    }
}
