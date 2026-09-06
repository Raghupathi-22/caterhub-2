package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CustomerBookingSource
import com.daily.cetaring.data.remote.dto.CustomerBookingUiModel
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.BookingValidator
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed class BookingUiState {
    data object Idle : BookingUiState()
    data object Loading : BookingUiState()
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
    // Kept in the constructor so MainActivity and existing DI wiring do not
    // have to change. Staff requests are intentionally not created here.
    private val workerRepository: com.daily.cetaring.data.repository.WorkerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _draft = MutableStateFlow(BookingDraft())
    val draft: StateFlow<BookingDraft> = _draft.asStateFlow()

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

        if (_uiState.value is BookingUiState.Loading) return

        viewModelScope.launch {
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
                _uiState.value = BookingUiState.Error(
                    exception.message ?: "Something went wrong. Please try again."
                )
            }
        }
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
