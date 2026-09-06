package com.daily.cetaring.auth

enum class AuthDestination {
    CUSTOMER_HOME,
    WORKER_DASHBOARD,
    ADMIN_HOME
}

object AuthRoleRouter {
    fun destinationForRoles(
        roles: List<String>?,
        fallback: AuthDestination = AuthDestination.CUSTOMER_HOME
    ): AuthDestination {
        val normalized = roles.orEmpty()
            .mapNotNull { raw ->
                raw.trim().takeIf { it.isNotBlank() }?.uppercase()
            }
        if (normalized.isEmpty()) return fallback
        return when {
            normalized.any { it.contains("ADMIN") } -> AuthDestination.ADMIN_HOME
            normalized.any { it.contains("WORKER") } -> AuthDestination.WORKER_DASHBOARD
            normalized.any { it.contains("CUSTOMER") } -> AuthDestination.CUSTOMER_HOME
            else -> fallback
        }
    }

    fun parseStoredRoles(raw: String?): List<String> = raw
        .orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
}
