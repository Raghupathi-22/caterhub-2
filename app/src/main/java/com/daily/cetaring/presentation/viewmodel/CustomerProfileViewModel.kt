package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.UpdateUserProfileRequest
import com.daily.cetaring.data.remote.dto.UserDTO
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CustomerProfileUiState {
    data object Idle : CustomerProfileUiState()
    data object Loading : CustomerProfileUiState()
    data class Loaded(val user: UserDTO, val saved: Boolean = false) : CustomerProfileUiState()
    data object LoggedOut : CustomerProfileUiState()
    data class Error(val message: String) : CustomerProfileUiState()
}

class CustomerProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CustomerProfileUiState>(CustomerProfileUiState.Idle)
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = CustomerProfileUiState.Loading
            try {
                _uiState.value = CustomerProfileUiState.Loaded(userRepository.getMyProfile())
            } catch (exception: Exception) {
                _uiState.value = CustomerProfileUiState.Error(exception.message ?: "Unable to load profile")
            }
        }
    }

    fun saveProfile(firstName: String, lastName: String, email: String, phoneNumber: String) {
        if (_uiState.value is CustomerProfileUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = CustomerProfileUiState.Loading
            try {
                val user = userRepository.updateMyProfile(
                    UpdateUserProfileRequest(
                        firstName = firstName.trim().ifBlank { null },
                        lastName = lastName.trim().ifBlank { null },
                        email = email.trim().ifBlank { null },
                        phoneNumber = phoneNumber.trim().ifBlank { null }
                    )
                )
                _uiState.value = CustomerProfileUiState.Loaded(user, saved = true)
            } catch (exception: Exception) {
                _uiState.value = CustomerProfileUiState.Error(exception.message ?: "Unable to save profile")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = CustomerProfileUiState.LoggedOut
        }
    }
}

