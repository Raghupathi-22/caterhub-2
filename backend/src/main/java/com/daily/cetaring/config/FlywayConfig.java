package com.daily.cetaring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Keep production startup deterministic: run Flyway migrations normally.
 * Database repair is intentionally not performed automatically because it is
 * an administrative operation and can hide migration packaging problems.
 */
@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Flyway: running normal migration validation and migrate()");
            flyway.migrate();
        };
    }
}
