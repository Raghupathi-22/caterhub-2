package com.daily.cetaring.auth

/**
 * The customer action that was interrupted to sign in. It travels in the
 * navigation argument of the single customer-auth route so it survives
 * process/activity recreation; the action's own form data stays in its owning
 * ViewModel or back-stack entry and is resumed after authentication.
 */
enum class PendingCustomerAction(
    val routeValue: String,
    val title: String,
    val subtitle: String,
    val resumeMessage: String?,
    val nonCustomerMessage: String
) {
    ACCOUNT_ENTRY(
        routeValue = "account",
        title = "Continue with your mobile",
        subtitle = "Sign in or create your CaterHub account with a one-time password.",
        resumeMessage = null,
        nonCustomerMessage = "This number belongs to a partner account. Please use Partner login."
    ),
    CATERING_BOOKING(
        routeValue = "catering_booking",
        title = "Verify to confirm your booking",
        subtitle = "Enter your mobile number. New to CaterHub? You can create your account right after verification.",
        resumeMessage = "Verified successfully. Confirming your booking...",
        nonCustomerMessage = "Please sign in with a customer account to submit this booking."
    ),
    SERVICE_REQUEST(
        routeValue = "service_request",
        title = "Verify to submit your request",
        subtitle = "Enter your mobile number. New to CaterHub? You can create your account right after verification.",
        resumeMessage = "Verified successfully. Submitting your request...",
        nonCustomerMessage = "Please sign in with a customer account to submit this request."
    ),
    RICE_ORDER(
        routeValue = "rice_order",
        title = "Verify to confirm your rice request",
        subtitle = "Enter your mobile number to submit your rice order or free sample request. New customers can create an account right after verification.",
        resumeMessage = "Verified successfully. Submitting your request...",
        nonCustomerMessage = "Please sign in with a customer account to order rice."
    );

    companion object {
        fun fromRouteValue(value: String?): PendingCustomerAction =
            entries.firstOrNull { it.routeValue == value } ?: ACCOUNT_ENTRY
    }
}

sealed interface CustomerAuthOutcome {
    /** Signed in from the landing page: route by role like any other login. */
    data class EnterApp(val destination: AuthDestination) : CustomerAuthOutcome

    /** Return to the interrupted screen and resume the pending submission once. */
    data class ResumePendingAction(val action: PendingCustomerAction, val message: String?) : CustomerAuthOutcome

    /** A non-customer session cannot complete a customer action; drop the pending submission. */
    data class RejectPendingAction(val action: PendingCustomerAction, val message: String) : CustomerAuthOutcome
}

/** Single decision point used by every customer-facing booking/order flow after OTP sign-in or sign-up. */
object CustomerAuthCoordinator {
    const val ROUTE = "customer_auth?action={action}"
    const val ACTION_ARG = "action"

    fun route(action: PendingCustomerAction): String = "customer_auth?action=${action.routeValue}"

    /** Same rule the booking screens use to decide whether a session may submit customer bookings. */
    fun canActAsCustomer(roles: List<String?>?): Boolean =
        AuthRoleRouter.destinationForRoles(roles, fallback = AuthDestination.CUSTOMER_HOME) != AuthDestination.WORKER_DASHBOARD

    fun canSubmitCustomerAction(accessToken: String?, storedRoles: String?): Boolean =
        !accessToken.isNullOrBlank() && canActAsCustomer(AuthRoleRouter.parseStoredRoles(storedRoles))

    fun resolve(action: PendingCustomerAction, roles: List<String?>?): CustomerAuthOutcome {
        if (action == PendingCustomerAction.ACCOUNT_ENTRY) {
            return CustomerAuthOutcome.EnterApp(
                AuthRoleRouter.destinationForRoles(roles, fallback = AuthDestination.CUSTOMER_HOME)
            )
        }
        return if (canActAsCustomer(roles)) {
            CustomerAuthOutcome.ResumePendingAction(action, action.resumeMessage)
        } else {
            CustomerAuthOutcome.RejectPendingAction(action, action.nonCustomerMessage)
        }
    }
}
