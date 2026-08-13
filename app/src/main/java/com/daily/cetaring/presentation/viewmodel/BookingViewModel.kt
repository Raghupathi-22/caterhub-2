package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.BookingValidator
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import com.daily.cetaring.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed class BookingUiState {
    data object Idle : BookingUiState()
    data object Loading : BookingUiState()
    data class ListLoaded(val bookings: List<BookingResponse>) : BookingUiState()
    data class DetailsLoaded(val booking: BookingResponse) : BookingUiState()
    data class Submitted(val booking: BookingResponse) : BookingUiState()
    data class Cancelled(val bookingId: Long) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _draft = MutableStateFlow(BookingDraft())
    val draft: StateFlow<BookingDraft> = _draft.asStateFlow()

    fun updateDraft(transform: (BookingDraft) -> BookingDraft) {
        _draft.value = transform(_draft.value)
    }

    fun startNewBooking(prefillGuests: Int? = null, eventType: String? = null, foodType: String? = null, serviceType: String? = null) {
        _uiState.value = BookingUiState.Idle
        _draft.value = BookingDraft(
            guestCount = prefillGuests,
            eventType = eventType.orEmpty(),
            foodType = foodType.orEmpty(),
            serviceType = serviceType.orEmpty()
        )
    }

    fun validateStep(step: Int): BookingValidationResult = BookingValidator.validateStep(step, _draft.value)

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
                        eventType = currentDraft.eventType,
                        guestCount = currentDraft.guestCount ?: 0,
                        mealType = currentDraft.mealTypeForBackend(),
                        eventDateTime = currentDraft.eventDateTimeIso(),
                        deliveryAddress = currentDraft.deliveryAddress(),
                        specialInstructions = buildSpecialInstructions(currentDraft),
                        estimatedAmount = estimateAmount(currentDraft)
                    )
                )
                _uiState.value = BookingUiState.Submitted(response)
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(exception.message ?: "Something went wrong. Please try again.")
            }
        }
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                _uiState.value = BookingUiState.ListLoaded(bookingRepository.getMyBookings())
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(exception.message ?: "Unable to load bookings")
            }
        }
    }

    fun loadBooking(id: Long) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                _uiState.value = BookingUiState.DetailsLoaded(bookingRepository.getBooking(id))
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(exception.message ?: "Unable to load booking")
            }
        }
    }

    fun cancelBooking(id: Long) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                bookingRepository.cancelBooking(id)
                _uiState.value = BookingUiState.Cancelled(id)
                loadBooking(id)
            } catch (exception: Exception) {
                _uiState.value = BookingUiState.Error(exception.message ?: "Unable to cancel booking")
            }
        }
    }

    fun resetState() {
        _uiState.value = BookingUiState.Idle
    }

    private fun buildSpecialInstructions(draft: BookingDraft): String? {
        val parts = listOf(
            draft.foodRequirements.takeIf { it.isNotBlank() }?.let { "Food requirements: $it" },
            draft.specialInstructions.takeIf { it.isNotBlank() }?.let { "Special instructions: $it" },
            draft.workerCount?.takeIf { it > 0 }?.let { "Requested worker count: $it" }
        )
        return parts.filterNotNull().joinToString("\n").ifBlank { null }
    }

    private fun estimateAmount(draft: BookingDraft): BigDecimal {
        val guests = draft.guestCount ?: 1
        val perGuest = when (draft.foodType) {
            "Breakfast", "Tiffin", "Snacks", "Beverages" -> 180
            "Full Day" -> 850
            else -> 350
        }
        val serviceFee = when (draft.serviceType) {
            "Chef" -> 2500
            "Serving Staff", "Cleaning Staff", "Kitchen Helper" -> 1200
            "Food Only" -> 0
            else -> 3000
        }
        return BigDecimal((guests * perGuest + serviceFee).coerceAtLeast(1000))
    }
}
