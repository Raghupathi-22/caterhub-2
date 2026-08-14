package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.SendOtpRequest
import com.daily.cetaring.data.remote.dto.VerifyOtpRequest
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
    data class Sent(val resendCooldownSeconds: Int) : OtpUiState()
    data object Verifying : OtpUiState()
    data class Success(val response: AuthResponse) : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val _otpUiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val otpUiState: StateFlow<OtpUiState> = _otpUiState.asStateFlow()

    fun sendOtp(mobileNumber: String, purpose: String) {
        viewModelScope.launch {
            _otpUiState.value = OtpUiState.Sending
            try {
                val response = authRepository.sendOtp(SendOtpRequest(mobileNumber, purpose))
                _otpUiState.value = OtpUiState.Sent(resendCooldownSeconds = 60)
            } catch (e: Exception) {
                _otpUiState.value = OtpUiState.Error(e.message ?: "Unable to send OTP")
            }
        }
    }

    fun verifyOtp(mobileNumber: String, otp: String, purpose: String, name: String?) {
        viewModelScope.launch {
            _otpUiState.value = OtpUiState.Verifying
            try {
                val response = authRepository.verifyOtp(VerifyOtpRequest(mobileNumber, otp, purpose, name))
                _otpUiState.value = OtpUiState.Success(response)
            } catch (e: Exception) {
                _otpUiState.value = OtpUiState.Error(e.message ?: "OTP verification failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun resetOtpState() {
        _otpUiState.value = OtpUiState.Idle
    }
}
