package com.daily.cetaring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Production DBs may still have a failed V12 row or checksums from the
 * rewritten OTP / V13-V14 split. repair() only fixes history (failed rows
 * and checksums); it does not change schema. Duplicate version files are
 * rejected at Docker build time, so repair will not hide packaging errors.
 */
@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Flyway: repairing schema history, then migrate()");
            flyway.repair();
            flyway.migrate();
        };
    }
}
