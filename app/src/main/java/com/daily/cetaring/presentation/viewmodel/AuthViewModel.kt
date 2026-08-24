package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.SendOtpRequest
import com.daily.cetaring.data.remote.dto.VerifyOtpRequest
import com.daily.cetaring.data.repository.AuthRepository
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

    data class Error(
        val message: String
    ) : OtpUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    private val _otpUiState =
        MutableStateFlow<OtpUiState>(OtpUiState.Idle)

    val otpUiState: StateFlow<OtpUiState> =
        _otpUiState.asStateFlow()

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
                    "REGISTER_WORKER"
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

                _otpUiState.value =
                    OtpUiState.Sent(
                        resendCooldownSeconds = 60,
                        message = sendResponse.message.ifBlank {
                            if (sendResponse.deliveryChannel.equals("VOICE", ignoreCase = true)) {
                                "We are calling you with the OTP."
                            } else {
                                "OTP sent by SMS"
                            }
                        },
                        deliveryChannel = sendResponse.deliveryChannel ?: "SMS"
                    )

            } catch (e: Exception) {

                _otpUiState.value =
                    OtpUiState.Error(
                        getReadableError(e)
                    )
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
            return "Unable to complete the request. Please try again."
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