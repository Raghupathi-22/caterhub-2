package com.daily.cetaring.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerAuthCoordinatorTest {

    private val bookingActions = listOf(
        PendingCustomerAction.CATERING_BOOKING,
        PendingCustomerAction.SERVICE_REQUEST,
        PendingCustomerAction.RICE_ORDER
    )

    @Test
    fun everyCustomerActionResumesForCustomerAccounts() {
        bookingActions.forEach { action ->
            val outcome = CustomerAuthCoordinator.resolve(action, listOf("ROLE_CUSTOMER"))
            assertEquals(CustomerAuthOutcome.ResumePendingAction(action, action.resumeMessage), outcome)
        }
    }

    @Test
    fun workerSessionCannotCompleteCustomerActions() {
        bookingActions.forEach { action ->
            val outcome = CustomerAuthCoordinator.resolve(action, listOf("ROLE_WORKER"))
            assertTrue(outcome is CustomerAuthOutcome.RejectPendingAction)
            assertEquals(action.nonCustomerMessage, (outcome as CustomerAuthOutcome.RejectPendingAction).message)
        }
    }

    @Test
    fun newlyRegisteredAccountWithoutRolesInResponseIsTreatedAsCustomer() {
        val outcome = CustomerAuthCoordinator.resolve(PendingCustomerAction.RICE_ORDER, emptyList())
        assertTrue(outcome is CustomerAuthOutcome.ResumePendingAction)
    }

    @Test
    fun accountEntryRoutesByRole() {
        assertEquals(
            CustomerAuthOutcome.EnterApp(AuthDestination.CUSTOMER_HOME),
            CustomerAuthCoordinator.resolve(PendingCustomerAction.ACCOUNT_ENTRY, listOf("ROLE_CUSTOMER"))
        )
        assertEquals(
            CustomerAuthOutcome.EnterApp(AuthDestination.ADMIN_HOME),
            CustomerAuthCoordinator.resolve(PendingCustomerAction.ACCOUNT_ENTRY, listOf("ROLE_ADMIN"))
        )
    }

    @Test
    fun routeRoundTripsActionAndUnknownValuesFallBackToAccountEntry() {
        PendingCustomerAction.entries.forEach { action ->
            val value = CustomerAuthCoordinator.route(action).substringAfter("action=")
            assertEquals(action, PendingCustomerAction.fromRouteValue(value))
        }
        assertEquals(PendingCustomerAction.ACCOUNT_ENTRY, PendingCustomerAction.fromRouteValue("unknown"))
        assertEquals(PendingCustomerAction.ACCOUNT_ENTRY, PendingCustomerAction.fromRouteValue(null))
    }

    @Test
    fun submitRequiresTokenAndCustomerCapableRole() {
        assertFalse(CustomerAuthCoordinator.canSubmitCustomerAction(null, "ROLE_CUSTOMER"))
        assertFalse(CustomerAuthCoordinator.canSubmitCustomerAction("", "ROLE_CUSTOMER"))
        assertFalse(CustomerAuthCoordinator.canSubmitCustomerAction("token", "ROLE_WORKER"))
        assertTrue(CustomerAuthCoordinator.canSubmitCustomerAction("token", "ROLE_CUSTOMER"))
    }
}
