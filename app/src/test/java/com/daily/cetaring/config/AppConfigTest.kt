package com.daily.cetaring.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {

    @Test
    fun apiBaseUrlAlwaysEndsWithSlash() {
        assertTrue(AppConfig.apiBaseUrl.endsWith("/"))
    }

    @Test
    fun apiBaseUrlDoesNotUseLocalBackendDefaults() {
        assertFalse(AppConfig.apiBaseUrl.contains("localhost:8080"))
        assertFalse(AppConfig.apiBaseUrl.contains("10.0.2.2:8080"))
        assertFalse(AppConfig.apiBaseUrl.contains("192.168."))
    }

    @Test
    fun developmentApiBaseUrlIsCentralizedAndHasValidScheme() {
        if (AppConfig.environment == Environment.DEVELOPMENT) {
            assertTrue(
                AppConfig.apiBaseUrl.startsWith("https://") || AppConfig.apiBaseUrl.startsWith("http://")
            )
            assertEquals(AppConfig.apiBaseUrl.startsWith("http://"), AppConfig.isCleartextPermitted)
        }
    }

    @Test
    fun productionApiBaseUrlUsesRailwayHttpsOnlyWhenProd() {
        if (AppConfig.environment == Environment.PRODUCTION) {
            assertTrue(AppConfig.apiBaseUrl.startsWith("https://"))
            assertTrue(AppConfig.apiBaseUrl.contains(".up.railway.app/"))
            assertFalse(AppConfig.isCleartextPermitted)
        }
    }
}
