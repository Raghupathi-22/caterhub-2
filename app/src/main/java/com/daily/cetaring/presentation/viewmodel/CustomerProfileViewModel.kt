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
    data class Loaded(
        val user: UserDTO,
        val saved: Boolean = false,
        val isSaving: Boolean = false,
        val saveError: String? = null,
        val isDeleting: Boolean = false,
        val actionError: String? = null
    ) : CustomerProfileUiState()
    data object LoggedOut : CustomerProfileUiState()
    data class AccountDeleted(val message: String) : CustomerProfileUiState()
    data class Error(val message: String) : CustomerProfileUiState()
}

class CustomerProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CustomerProfileUiState>(CustomerProfileUiState.Idle)
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        val current = _uiState.value
        if (current is CustomerProfileUiState.Loaded && (current.isSaving || current.isDeleting)) return
        viewModelScope.launch {
            _uiState.value = CustomerProfileUiState.Loading
            try {
                _uiState.value = CustomerProfileUiState.Loaded(userRepository.getMyProfile())
            } catch (exception: Exception) {
                _uiState.value = CustomerProfileUiState.Error(exception.message ?: "Unable to load profile")
            }
        }
    }

    /**
     * Saves editable customer fields. The OTP-verified mobile number is intentionally never sent,
     * so it can only change through a verified flow.
     */
    fun saveProfile(firstName: String, lastName: String, email: String) {
        val current = _uiState.value as? CustomerProfileUiState.Loaded ?: return
        if (current.isSaving || current.isDeleting) return
        if (CustomerProfileEditRules.validate(firstName, lastName, email).isNotEmpty()) return
        _uiState.value = current.copy(isSaving = true, saveError = null, saved = false)
        viewModelScope.launch {
            try {
                val user = userRepository.updateMyProfile(
                    CustomerProfileEditRules.toRequest(firstName, lastName, email)
                )
                _uiState.value = CustomerProfileUiState.Loaded(user, saved = true)
            } catch (exception: Exception) {
                val latest = _uiState.value as? CustomerProfileUiState.Loaded ?: current
                _uiState.value = latest.copy(
                    isSaving = false,
                    saveError = exception.message ?: "Unable to save profile. Please try again."
                )
            }
        }
    }

    fun consumeSaved() {
        val current = _uiState.value as? CustomerProfileUiState.Loaded ?: return
        if (current.saved) _uiState.value = current.copy(saved = false)
    }

    fun clearErrors() {
        val current = _uiState.value as? CustomerProfileUiState.Loaded ?: return
        _uiState.value = current.copy(saveError = null, actionError = null)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = CustomerProfileUiState.LoggedOut
        }
    }

    fun deleteAccount() {
        val current = _uiState.value as? CustomerProfileUiState.Loaded ?: return
        if (current.isDeleting || current.isSaving) return
        _uiState.value = current.copy(isDeleting = true, actionError = null)
        viewModelScope.launch {
            try {
                userRepository.deleteMyAccount()
                authRepository.logout()
                _uiState.value = CustomerProfileUiState.AccountDeleted("Your CaterHub account has been deleted successfully.")
            } catch (exception: Exception) {
                // Deletion was not confirmed by the server; stay signed in on the profile.
                val latest = _uiState.value as? CustomerProfileUiState.Loaded ?: current
                _uiState.value = latest.copy(
                    isDeleting = false,
                    actionError = exception.message ?: "Unable to delete account. Please try again."
                )
            }
        }
    }
}

/** Client-side checks mirroring the backend rules for PATCH users/me. */
object CustomerProfileEditRules {
    const val MAX_NAME_LENGTH = 100
    const val MAX_EMAIL_LENGTH = 100
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    enum class Field { FIRST_NAME, LAST_NAME, EMAIL }

    fun validate(firstName: String, lastName: String, email: String): Map<Field, String> = buildMap {
        val first = firstName.trim()
        when {
            first.isEmpty() -> put(Field.FIRST_NAME, "Please enter your first name.")
            first.length > MAX_NAME_LENGTH -> put(Field.FIRST_NAME, "First name must be at most $MAX_NAME_LENGTH characters.")
        }
        if (lastName.trim().length > MAX_NAME_LENGTH) {
            put(Field.LAST_NAME, "Last name must be at most $MAX_NAME_LENGTH characters.")
        }
        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isEmpty() -> Unit
            trimmedEmail.length > MAX_EMAIL_LENGTH -> put(Field.EMAIL, "Email must be at most $MAX_EMAIL_LENGTH characters.")
            !EMAIL_REGEX.matches(trimmedEmail) -> put(Field.EMAIL, "Please enter a valid email address.")
        }
    }

    fun toRequest(firstName: String, lastName: String, email: String) = UpdateUserProfileRequest(
        firstName = firstName.trim().replace(Regex("\\s+"), " ").ifBlank { null },
        lastName = lastName.trim().replace(Regex("\\s+"), " ").ifBlank { null },
        email = email.trim().ifBlank { null },
        phoneNumber = null
    )
}
