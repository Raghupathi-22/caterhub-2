package com.daily.cetaring.config

import com.daily.cetaring.BuildConfig

enum class Environment {
    DEVELOPMENT,
    PRODUCTION
}

object AppConfig {
    val environment: Environment = Environment.valueOf(BuildConfig.ENVIRONMENT)

    val apiBaseUrl: String = normalizeBaseUrl(BuildConfig.API_BASE_URL)

    val isCleartextPermitted: Boolean = BuildConfig.ALLOW_CLEARTEXT_LOCAL_TRAFFIC

    private fun normalizeBaseUrl(url: String): String {
        require(url.isNotBlank()) { "API base URL must not be blank" }
        return if (url.endsWith('/')) url else "$url/"
    }
}
