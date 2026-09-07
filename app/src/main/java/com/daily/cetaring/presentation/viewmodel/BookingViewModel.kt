package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.auth.AuthRoleRouter
import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CustomerBookingSource
import com.daily.cetaring.data.remote.dto.CustomerBookingUiModel
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.BookingValidator
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.BookingAuthenticationRequiredException
import com.daily.cetaring.data.repository.BookingRepository
import com.daily.cetaring.data.repository.BookingSessionExpiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed class BookingUiState {
    data object Idle : BookingUiState()
    data object Loading : BookingUiState()
    data class AuthRequired(
        val message: String,
        val isSessionExpired: Boolean = false
    ) : BookingUiState()
    data class ListLoaded(val bookings: List<CustomerBookingUiModel>) : BookingUiState()
    data class DetailsLoaded(val booking: CustomerBookingUiModel) : BookingUiState()

    data class Submitted(
        val booking: BookingResponse,
        val staffingJobs: List<StaffingJobResponse> = emptyList(),
        val staffingError: String? = null
    ) : BookingUiState()

    data class Cancelled(val bookingId: Long) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    // Kept in the constructor so MainActivity and existing DI wiring do not
    // have to change. Staff requests are intentionally not created here.
    private val workerRepository: com.daily.cetaring.data.repository.WorkerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _draft = MutableStateFlow(BookingDraft())
    val draft: StateFlow<BookingDraft> = _draft.asStateFlow()
    private var pendingSubmissionAfterAuth: Boolean = false
    private var submissionInProgress: Boolean = false

    fun updateDraft(transform: (BookingDraft) -> BookingDraft) {
        _draft.value = transform(_draft.value)
    }

    fun startNewBooking(
        prefillGuests: Int? = null,
        eventType: String? = null,
        foodService: String? = null
    ) {
        _uiState.value = BookingUiState.Idle
        val selectedPlan = BookingOptions.normalizePlanValue(foodService)
        val selectedFoodType = BookingOptions.planByBackendValue(selectedPlan)?.foodType
            ?: BookingOptions.foodTypeNonVegetarian
        _draft.value = BookingDraft(
            guestCount = prefillGuests,
            eventType = eventType,
            guestCountSelection = if (prefillGuests != null && prefillGuests !in BookingOptions.guestQuickOptions) {
                BookingOptions.guestSelectionCustom
            } else {
                BookingOptions.guestSelectionPreset
            },
            customGuestCountInput = if (prefillGuests != null && prefillGuests !in BookingOptions.guestQuickOptions) {
                prefillGuests.toString().take(BookingOptions.maxGuestInputLength)
            } else {
                ""
            },
            cateringFoodType = selectedFoodType,
            cateringPlan = selectedPlan
        )
    }

    fun validateStep(step: Int): BookingValidationResult =
        BookingValidator.validateStep(step, _draft.value)

    fun submitBooking() {
        val currentDraft = _draft.value
        val validation = BookingValidator.validateForSubmit(currentDraft)

        if (validation is BookingValidationResult.Invalid) {
            _uiState.value = BookingUiState.Error(validation.message)
            return
        }

        if (_uiState.value is BookingUiState.Loading || submissionInProgress) return
        submissionInProgress = true

        viewModelScope.launch {
            when (ensureCustomerSessionForBookingSubmission()) {
                CustomerBookingAuthState.READY -> submitBookingInternal(currentDraft)
                CustomerBookingAuthState.LOGIN_REQUIRED -> {
                    promptCustomerLogin(isSessionExpired = false)
                    submissionInProgress = false
                }
                CustomerBookingAuthState.SESSION_EXPIRED -> {
                    promptCustomerLogin(isSessionExpired = true)
                    submissionInProgress = false
                }
            }
        }
    }

    fun resumePendingSubmissionAfterAuth() {
        if (!pendingSubmissionAfterAuth || _uiState.value is BookingUiState.Loading || submissionInProgress) return
        pendingSubmissionAfterAuth = false
        submissionInProgress = true
        viewModelScope.launch {
            submitBookingInternal(_draft.value)
        }
    }

    fun clearPendingSubmissionAfterAuth() {
        pendingSubmissionAfterAuth = false
        if (_uiState.value is BookingUiState.AuthRequired) {
            _uiState.value = BookingUiState.Idle
        }
    }

    fun markAuthPromptHandled() {
        if (_uiState.value is BookingUiState.AuthRequired) {
            _uiState.value = BookingUiState.Idle
        }
    }

    fun showError(message: String) {
        _uiState.value = BookingUiState.Error(message)
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                _uiState.value = BookingUiState.ListLoaded(
                    bookingRepository.getUnifiedMyBookings()
                )
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(
                    exception.message ?: "Unable to load bookings"
                )
            }
        }
    }

    fun loadBooking(id: Long, source: CustomerBookingSource) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                _uiState.value = BookingUiState.DetailsLoaded(
                    bookingRepository.getUnifiedBooking(id, source)
                )
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(
                    exception.message ?: "Unable to load booking"
                )
            }
        }
    }

    fun cancelBooking(id: Long) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                bookingRepository.cancelBooking(id)
                _uiState.value = BookingUiState.Cancelled(id)
                loadBooking(id, CustomerBookingSource.CATERING)
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(
                    exception.message ?: "Unable to cancel booking"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = BookingUiState.Idle
    }

    private suspend fun submitBookingInternal(currentDraft: BookingDraft) {
        _uiState.value = BookingUiState.Loading

        try {
            val response = bookingRepository.createBooking(
                CreateMyBookingRequest(
                    eventType = currentDraft.eventType.orEmpty(),
                    guestCount = currentDraft.guestCount ?: 0,
                    mealType = currentDraft.mealTypeForBackend(),
                    eventDateTime = currentDraft.eventDateTimeIso(),
                    deliveryAddress = currentDraft.deliveryAddress(),
                    specialInstructions = buildSpecialInstructions(currentDraft),
                    estimatedAmount = estimateAmount(currentDraft)
                )
            )

            // Important: customer catering booking does NOT create staff jobs.
            _uiState.value = BookingUiState.Submitted(response)
        } catch (exception: Exception) {
            when (exception) {
                is BookingAuthenticationRequiredException -> promptCustomerLogin(isSessionExpired = false)
                is BookingSessionExpiredException -> promptCustomerLogin(isSessionExpired = true)
                else -> {
                    val message = exception.message ?: "Something went wrong. Please try again."
                    _uiState.value = BookingUiState.Error(message)
                }
            }
        } finally {
            submissionInProgress = false
        }
    }

    private suspend fun ensureCustomerSessionForBookingSubmission(): CustomerBookingAuthState {
        val accessToken = authRepository.getAccessToken()?.takeIf { it.isNotBlank() }
        val refreshToken = authRepository.getRefreshToken()?.takeIf { it.isNotBlank() }
        val storedRoles = AuthRoleRouter.parseStoredRoles(authRepository.rolesFlow.first())
        val hasCustomerRole = storedRoles.any { it.contains("CUSTOMER", ignoreCase = true) }
        val hasKnownNonCustomerRole = storedRoles.isNotEmpty() && !hasCustomerRole

        if (accessToken != null && hasCustomerRole) {
            return CustomerBookingAuthState.READY
        }
        if (accessToken != null && storedRoles.isEmpty()) {
            return CustomerBookingAuthState.READY
        }
        if (accessToken == null && refreshToken == null) {
            return CustomerBookingAuthState.LOGIN_REQUIRED
        }
        if (accessToken != null && hasKnownNonCustomerRole) {
            return CustomerBookingAuthState.LOGIN_REQUIRED
        }
        if (refreshToken != null) {
            return try {
                val response = authRepository.refreshToken(refreshToken)
                val refreshedHasCustomerRole = response.user.roles
                    .filterNotNull()
                    .any { it.contains("CUSTOMER", ignoreCase = true) }
                if (refreshedHasCustomerRole) {
                    CustomerBookingAuthState.READY
                } else {
                    CustomerBookingAuthState.LOGIN_REQUIRED
                }
            } catch (_: Exception) {
                if (accessToken != null) {
                    CustomerBookingAuthState.SESSION_EXPIRED
                } else {
                    CustomerBookingAuthState.LOGIN_REQUIRED
                }
            }
        }

        return CustomerBookingAuthState.LOGIN_REQUIRED
    }

    private fun promptCustomerLogin(isSessionExpired: Boolean) {
        pendingSubmissionAfterAuth = true
        _uiState.value = BookingUiState.AuthRequired(
            message = if (isSessionExpired) {
                "Your session has expired. Please sign in again to submit your booking."
            } else {
                "Login to confirm your booking."
            },
            isSessionExpired = isSessionExpired
        )
    }

    private enum class CustomerBookingAuthState {
        READY,
        LOGIN_REQUIRED,
        SESSION_EXPIRED
    }

    private fun buildSpecialInstructions(draft: BookingDraft): String? {
        return listOf(
            "Food services: ${draft.selectedFoodServicesLabel()}",
            "Catering plan: ${draft.cateringPlan}",
            draft.foodRequirements
                .takeIf { it.isNotBlank() }
                ?.let { "Food requirements: $it" },
            draft.specialInstructions
                .takeIf { it.isNotBlank() }
                ?.let { "Special instructions: $it" }
        )
            .filterNotNull()
            .joinToString("\n")
            .ifBlank { null }
    }

    private fun estimateAmount(draft: BookingDraft): BigDecimal {
        val guests = draft.guestCount ?: 1
        val perGuest = BookingOptions.planByBackendValue(draft.cateringPlan)?.pricePerPerson
            ?: BookingOptions.planByBackendValue(BookingOptions.normalizePlanValue(draft.cateringPlan))?.pricePerPerson
            ?: 699

        return BigDecimal(guests * perGuest)
    }
}
