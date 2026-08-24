package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Loaded(
        val firstName: String,
        val email: String,
        val upcomingBooking: BookingResponse?,
        val bookingCount: Int
    ) : HomeUiState()
    data class Error(val firstName: String, val message: String) : HomeUiState()
}

class HomeViewModel(
    private val bookingRepository: BookingRepository,
    private val authLocalDataSource: AuthLocalDataSource
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val fallbackName = authLocalDataSource.firstNameFlow.first()
                ?: authLocalDataSource.usernameFlow.first()
                ?: "there"
            val email = authLocalDataSource.emailFlow.first().orEmpty()
            try {
                val bookings = bookingRepository.getMyBookings()
                val upcoming = bookings.firstOrNull { it.status.uppercase() !in setOf("COMPLETED", "CANCELLED", "DELIVERED") }
                _uiState.value = HomeUiState.Loaded(
                    firstName = fallbackName,
                    email = email,
                    upcomingBooking = upcoming,
                    bookingCount = bookings.size
                )
            } catch (exception: Exception) {
                _uiState.value = HomeUiState.Error(
                    firstName = fallbackName,
                    message = exception.message ?: "Unable to connect. Please check your internet connection and try again."
                )
            }
        }
    }
}

