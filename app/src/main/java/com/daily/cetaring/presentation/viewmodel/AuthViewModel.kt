package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.auth.OtpAutoStatus
import com.daily.cetaring.auth.OtpEntryRules
import com.daily.cetaring.auth.OtpEntryState
import com.daily.cetaring.auth.OtpMessageParser
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.CustomerAccessVerifyRequest
import com.daily.cetaring.data.remote.dto.CustomerAccessVerifyResponse
import com.daily.cetaring.data.remote.dto.CustomerRegisterRequest
import com.daily.cetaring.data.remote.dto.SendOtpRequest
import com.daily.cetaring.data.remote.dto.SendOtpResponse
import com.daily.cetaring.data.remote.dto.VerifyOtpRequest
import com.daily.cetaring.data.repository.OtpRateLimitedException
import com.daily.cetaring.diagnostics.ReleaseDiagnostics
import com.daily.cetaring.data.repository.AuthRepository
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val response: AuthResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class OtpUiState {
    data object Idle : OtpUiState()
    data object Sending : OtpUiState()

    data class Sent(
        val resendCooldownSeconds: Int,
        val message: String = "OTP sent by SMS",
        val deliveryChannel: String = "SMS"
    ) : OtpUiState()

    data object Verifying : OtpUiState()

    data class Success(
        val response: AuthResponse
    ) : OtpUiState()

    /**
     * Unified customer flow: the OTP was verified server-side for a number with no
     * account yet. The customer must enter a name before an account is created.
     */
    data class RegistrationRequired(
        val verifiedMobileNumber: String,
        val registrationToken: String,
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null
    ) : OtpUiState()

    data class Error(
        val message: String,
        val retryAfterSeconds: Int? = null
    ) : OtpUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        const val DEFAULT_RESEND_COOLDOWN_SECONDS = 60
        const val OTP_EXPIRED_MESSAGE = "This OTP has expired. Tap Resend OTP to get a new code."
        const val OTP_INCORRECT_MESSAGE = "Incorrect OTP. Please check the code and try again."
        const val CUSTOMER_ACCESS_PURPOSE = "CUSTOMER_ACCESS"
        private const val MAX_RESEND_COOLDOWN_SECONDS = 3600L
        private val FULL_NAME_PATTERN = Regex("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'-]{1,79}$")

        /** Mirrors the backend name rules so most mistakes are caught before a request. */
        fun validateFullName(fullName: String): String? {
            val clean = fullName.trim().replace(Regex("\\s+"), " ")
            return when {
                clean.isEmpty() -> "Please enter your full name."
                clean.length < 2 -> "Full name must have at least 2 characters."
                clean.length > 80 -> "Full name must be 80 characters or fewer."
                !FULL_NAME_PATTERN.matches(clean) -> "Please use letters, spaces, dots, apostrophes or hyphens only."
                else -> null
            }
        }

        /** Countdown mirrors the backend resend cooldown, not the OTP expiry. */
        fun resendCooldownFrom(response: SendOtpResponse): Int =
            response.resendCooldownSeconds
                ?.takeIf { it > 0 }
                ?.coerceAtMost(MAX_RESEND_COOLDOWN_SECONDS)
                ?.toInt()
                ?: DEFAULT_RESEND_COOLDOWN_SECONDS
    }

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    private val _otpUiState =
        MutableStateFlow<OtpUiState>(OtpUiState.Idle)

    val otpUiState: StateFlow<OtpUiState> =
        _otpUiState.asStateFlow()

    private val _otpEntry = MutableStateFlow(OtpEntryState())

    /** The OTP being entered: single source of truth for the six visual boxes. */
    val otpEntry: StateFlow<OtpEntryState> = _otpEntry.asStateFlow()

    /** Starts a new OTP session (before a send or resend) and returns its id for SMS callbacks. */
    fun beginOtpSession(listenForSms: Boolean): Int {
        _otpEntry.value = OtpEntryRules.startSession(_otpEntry.value, listenForSms)
        return _otpEntry.value.sessionId
    }

    fun onOtpInput(raw: String) {
        _otpEntry.value = OtpEntryRules.onManualInput(_otpEntry.value, raw)
    }

    /** Parses an SMS for [sessionId]. The message and code are never logged. */
    fun onOtpSmsReceived(message: String, sessionId: Int) {
        _otpEntry.value = OtpEntryRules.onAutoDetected(_otpEntry.value, OtpMessageParser.extractOtp(message), sessionId)
    }

    fun onOtpAutoDetectionEnded(sessionId: Int, status: OtpAutoStatus) {
        _otpEntry.value = OtpEntryRules.onAutoEnded(_otpEntry.value, sessionId, status)
    }

    fun clearOtpEntry() {
        _otpEntry.value = OtpEntryState(sessionId = _otpEntry.value.sessionId + 1)
    }

    /**
     * Send OTP
     *
     * Valid backend purposes:
     * LOGIN
     * REGISTER_CUSTOMER
     * REGISTER_WORKER
     */
    fun sendOtp(
        mobileNumber: String,
        purpose: String,
        userType: String,
        channel: String? = null
    ) {
        viewModelScope.launch {
            ReleaseDiagnostics.info("CATERHUB_OTP_SEND_STARTED")

            _otpUiState.value = OtpUiState.Sending

            try {
                val cleanMobile = normalizeMobileNumber(mobileNumber)

                if (cleanMobile.length != 10) {
                    throw IllegalArgumentException(
                        "Please enter a valid 10-digit mobile number."
                    )
                }

                val cleanPurpose = purpose.trim().uppercase()

                val validPurposes = setOf(
                    "LOGIN",
                    "REGISTER_CUSTOMER",
                    "REGISTER_WORKER",
                    CUSTOMER_ACCESS_PURPOSE
                )

                if (cleanPurpose !in validPurposes) {
                    throw IllegalArgumentException(
                        "Invalid OTP request. Please try again."
                    )
                }

                val cleanUserType =
                    userType.trim().uppercase()

                if (
                    cleanUserType != "CUSTOMER" &&
                    cleanUserType != "WORKER"
                ) {
                    throw IllegalArgumentException(
                        "Invalid user type. Please try again."
                    )
                }

                /*
                 * Important:
                 *
                 * Customer login  -> LOGIN
                 * Worker login    -> LOGIN
                 *
                 * Customer signup -> REGISTER_CUSTOMER
                 * Worker signup   -> REGISTER_WORKER
                 *
                 * We do NOT change the purpose here based only
                 * on userType because LOGIN is valid for both.
                 */

                val request = SendOtpRequest(
                    cleanMobile,
                    cleanPurpose,
                    cleanUserType,
                    channel?.trim()?.uppercase()
                )

                val sendResponse = authRepository.sendOtp(request)
                val deliveryChannel = sendResponse.deliveryChannel
                    ?.takeIf(String::isNotBlank)
                    ?: "SMS"
                val fallbackMessage = if (deliveryChannel.equals("VOICE", ignoreCase = true)) {
                    "We are calling you with the OTP."
                } else {
                    "OTP sent by SMS"
                }
                val resendCooldown = resendCooldownFrom(sendResponse)

                _otpUiState.value =
                    OtpUiState.Sent(
                        resendCooldownSeconds = resendCooldown,
                        message = sendResponse.message
                            ?.takeIf(String::isNotBlank)
                            ?: fallbackMessage,
                        deliveryChannel = deliveryChannel
                    )
                ReleaseDiagnostics.info("CATERHUB_OTP_SEND_SUCCESS")

            } catch (e: Exception) {
                val statusCode = (e as? HttpException)?.code()?.toString() ?: "NA"
                ReleaseDiagnostics.error(
                    "CATERHUB_OTP_SEND_FAILURE exception=${e::class.java.simpleName} httpStatus=$statusCode message=${e.message.orEmpty()}"
                )

                val rateLimited = e as? OtpRateLimitedException
                _otpUiState.value =
                    if (rateLimited != null) {
                        OtpUiState.Error(
                            message = rateLimited.message ?: "Please wait before requesting another OTP",
                            retryAfterSeconds = rateLimited.retryAfterSeconds
                                ?: DEFAULT_RESEND_COOLDOWN_SECONDS
                        )
                    } else {
                        OtpUiState.Error(getReadableError(e))
                    }
            }
        }
    }

    /**
     * Verify OTP
     */
    fun verifyOtp(
        mobileNumber: String,
        otp: String,
        purpose: String,
        name: String?
    ) {
        if (_otpUiState.value is OtpUiState.Verifying) return
        viewModelScope.launch {

            _otpUiState.value =
                OtpUiState.Verifying

            try {
                val cleanMobile =
                    normalizeMobileNumber(mobileNumber)

                if (cleanMobile.length != 10) {
                    throw IllegalArgumentException(
                        "Please enter a valid 10-digit mobile number."
                    )
                }

                val cleanOtp =
                    otp.filter { it.isDigit() }

                if (cleanOtp.length != 6) {
                    throw IllegalArgumentException(
                        "Please enter the 6-digit OTP."
                    )
                }

                val cleanPurpose =
                    purpose.trim().uppercase()

                val validPurposes = setOf(
                    "LOGIN",
                    "REGISTER_CUSTOMER",
                    "REGISTER_WORKER"
                )

                if (cleanPurpose !in validPurposes) {
                    throw IllegalArgumentException(
                        "Invalid OTP verification request."
                    )
                }

                val cleanName =
                    name?.trim()?.takeIf { it.isNotEmpty() }

                val request = VerifyOtpRequest(
                    cleanMobile,
                    cleanOtp,
                    cleanPurpose,
                    cleanName
                )

                val response =
                    authRepository.verifyOtp(request)

                _otpUiState.value =
                    OtpUiState.Success(response)

            } catch (e: Exception) {

                _otpUiState.value =
                    OtpUiState.Error(
                        getReadableError(e)
                    )
            }
        }
    }

    /**
     * Unified customer verification. Existing customers are signed in; a new
     * number moves to [OtpUiState.RegistrationRequired] without creating an account.
     */
    fun verifyCustomerAccessOtp(mobileNumber: String, otp: String) {
        if (_otpUiState.value is OtpUiState.Verifying) return
        viewModelScope.launch {
            _otpUiState.value = OtpUiState.Verifying
            try {
                val cleanMobile = normalizeMobileNumber(mobileNumber)
                if (cleanMobile.length != 10) {
                    throw IllegalArgumentException("Please enter a valid 10-digit mobile number.")
                }
                val cleanOtp = otp.filter { it.isDigit() }
                if (cleanOtp.length != 6) {
                    throw IllegalArgumentException("Please enter the 6-digit OTP.")
                }

                val response = authRepository.verifyCustomerAccess(
                    CustomerAccessVerifyRequest(cleanMobile, cleanOtp)
                )
                _otpUiState.value = when (response.status) {
                    CustomerAccessVerifyResponse.STATUS_AUTHENTICATED ->
                        OtpUiState.Success(requireNotNull(response.auth))
                    else -> OtpUiState.RegistrationRequired(
                        verifiedMobileNumber = response.mobileNumber
                            ?.takeIf(String::isNotBlank)
                            ?: "+91$cleanMobile",
                        registrationToken = requireNotNull(response.registrationToken)
                    )
                }
            } catch (e: Exception) {
                _otpUiState.value = OtpUiState.Error(getReadableError(e))
            }
        }
    }

    /**
     * Creates the customer account for the number verified in
     * [verifyCustomerAccessOtp]. Repeated taps are ignored while a request is
     * in flight; a retried request is idempotent on the backend.
     */
    fun completeCustomerRegistration(fullName: String) {
        val pending = _otpUiState.value as? OtpUiState.RegistrationRequired ?: return
        if (pending.isSubmitting) return
        val nameError = validateFullName(fullName)
        if (nameError != null) {
            _otpUiState.value = pending.copy(errorMessage = nameError)
            return
        }
        val cleanName = fullName.trim().replace(Regex("\\s+"), " ")
        _otpUiState.value = pending.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = authRepository.registerCustomer(
                    CustomerRegisterRequest(
                        mobileNumber = pending.verifiedMobileNumber,
                        registrationToken = pending.registrationToken,
                        fullName = cleanName
                    )
                )
                _otpUiState.value = OtpUiState.Success(response)
            } catch (e: Exception) {
                _otpUiState.value = pending.copy(isSubmitting = false, errorMessage = getReadableError(e))
            }
        }
    }

    /**
     * Normalize Indian mobile number.
     *
     * Accepts:
     * 9999999999
     * +919999999999
     * 919999999999
     *
     * Sends only:
     * 9999999999
     */
    private fun normalizeMobileNumber(
        mobileNumber: String
    ): String {

        val digits =
            mobileNumber.filter { it.isDigit() }

        return when {
            digits.length == 10 ->
                digits

            digits.length == 12 &&
                    digits.startsWith("91") ->
                digits.substring(2)

            digits.length == 13 &&
                    digits.startsWith("91") ->
                digits.substring(2)

            else ->
                digits
        }
    }

    /**
     * Convert technical exceptions into
     * user-friendly messages.
     */
    private fun getReadableError(
        exception: Exception
    ): String {

        val message =
            exception.message?.trim()

        if (message.isNullOrBlank()) {
            return "Received an unexpected response. Please try again."
        }

        return when {
            message.contains(
                "timeout",
                ignoreCase = true
            ) ->
                "Server is taking too long to respond. Please try again."

            message.contains(
                "unable to resolve",
                ignoreCase = true
            ) ||
                    message.contains(
                        "failed to connect",
                        ignoreCase = true
                    ) ->
                "Unable to connect to the server. Please check your internet connection."

            message.contains(
                "400",
                ignoreCase = true
            ) ->
                "Please check your mobile number and try again."

            message.contains(
                "Authentication request was rejected",
                ignoreCase = true
            ) ->
                message

            message.contains("otp", ignoreCase = true) &&
                (message.contains("expired", ignoreCase = true) || message.contains("no active", ignoreCase = true)) ->
                OTP_EXPIRED_MESSAGE

            message.contains("otp", ignoreCase = true) &&
                (message.contains("invalid", ignoreCase = true) || message.contains("incorrect", ignoreCase = true)) ->
                OTP_INCORRECT_MESSAGE

            message.contains(
                "session has expired",
                ignoreCase = true
            ) ->
                "Your session has expired. Please sign in again."

            message.contains(
                "403",
                ignoreCase = true
            ) ->
                "You are not allowed to perform this action."

            message.contains(
                "404",
                ignoreCase = true
            ) ->
                "The requested service was not found."

            else ->
                message
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value =
                    AuthUiState.Idle

            } catch (e: Exception) {

                _uiState.value =
                    AuthUiState.Error(
                        getReadableError(e)
                    )
            }
        }
    }

    fun resetUiState() {
        _uiState.value =
            AuthUiState.Idle
    }

    fun resetOtpState() {
        _otpUiState.value =
            OtpUiState.Idle
    }
}