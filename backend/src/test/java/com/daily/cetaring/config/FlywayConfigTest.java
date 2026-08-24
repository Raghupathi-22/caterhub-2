package com.daily.cetaring.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class FlywayConfigTest {

    @Test
    void repairsSchemaHistoryBeforeMigrating() {
        Flyway flyway = mock(Flyway.class);
        FlywayMigrationStrategy strategy = new FlywayConfig().flywayMigrationStrategy();

        strategy.migrate(flyway);

        var inOrder = inOrder(flyway);
        inOrder.verify(flyway).repair();
        inOrder.verify(flyway).migrate();
        verifyNoMoreInteractions(flyway);
    }
}
