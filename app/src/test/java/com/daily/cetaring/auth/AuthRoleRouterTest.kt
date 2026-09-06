package com.daily.cetaring.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRoleRouterTest {
    @Test
    fun workerRoleRoutesToWorkerDashboard() {
        assertEquals(AuthDestination.WORKER_DASHBOARD, AuthRoleRouter.destinationForRoles(listOf("ROLE_WORKER")))
    }

    @Test
    fun customerRoleRoutesToCustomerHome() {
        assertEquals(AuthDestination.CUSTOMER_HOME, AuthRoleRouter.destinationForRoles(listOf("ROLE_CUSTOMER")))
    }

    @Test
    fun adminRoleTakesPriority() {
        assertEquals(AuthDestination.ADMIN_HOME, AuthRoleRouter.destinationForRoles(listOf("ROLE_CUSTOMER", "ROLE_ADMIN")))
    }

    @Test
    fun emptyRolesUseFallbackDestination() {
        assertEquals(
            AuthDestination.WORKER_DASHBOARD,
            AuthRoleRouter.destinationForRoles(emptyList(), fallback = AuthDestination.WORKER_DASHBOARD)
        )
        assertEquals(
            AuthDestination.CUSTOMER_HOME,
            AuthRoleRouter.destinationForRoles(null, fallback = AuthDestination.CUSTOMER_HOME)
        )
    }

    @Test
    fun storedRoleStringIsParsed() {
        assertEquals(listOf("ROLE_CUSTOMER", "ROLE_WORKER"), AuthRoleRouter.parseStoredRoles(" ROLE_CUSTOMER, ROLE_WORKER "))
        assertTrue(AuthRoleRouter.parseStoredRoles(null).isEmpty())
    }
}
