package com.daily.cetaring.auth

enum class AuthDestination {
    CUSTOMER_HOME,
    WORKER_DASHBOARD,
    ADMIN_HOME
}

object AuthRoleRouter {
    fun destinationForRoles(roles: List<String>): AuthDestination {
        val normalized = roles.map { it.trim().uppercase() }
        return when {
            normalized.any { it.contains("ADMIN") } -> AuthDestination.ADMIN_HOME
            normalized.any { it.contains("WORKER") } -> AuthDestination.WORKER_DASHBOARD
            else -> AuthDestination.CUSTOMER_HOME
        }
    }

    fun parseStoredRoles(raw: String?): List<String> = raw
        .orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

