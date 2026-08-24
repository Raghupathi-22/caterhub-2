package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.CreateEventRequest
import com.daily.cetaring.data.remote.dto.EventResponse
import com.daily.cetaring.data.remote.dto.EventWorkspaceResponse
import com.daily.cetaring.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed class EventUiState {
    data object Idle : EventUiState()
    data object Loading : EventUiState()
    data class Created(val event: EventResponse, val workspace: EventWorkspaceResponse) : EventUiState()
    data class WorkspaceLoaded(val workspace: EventWorkspaceResponse) : EventUiState()
    data class Error(val message: String) : EventUiState()
}

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    private val _state = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val state: StateFlow<EventUiState> = _state.asStateFlow()

    fun createEvent(
        name: String,
        eventType: String,
        date: String,
        startTime: String?,
        endTime: String?,
        location: String,
        guests: Int?,
        budget: String
    ) {
        viewModelScope.launch {
            _state.value = EventUiState.Loading
            try {
                val created = repository.createEvent(
                    CreateEventRequest(
                        name = name.trim(),
                        eventType = eventType,
                        eventDate = date,
                        startTime = startTime,
                        endTime = endTime,
                        location = location.trim().ifBlank { null },
                        guestCount = guests,
                        budget = budget.toBigDecimalOrNull()
                    )
                )
                val workspace = repository.getWorkspace(created.id)
                _state.value = EventUiState.Created(created, workspace)
            } catch (e: Exception) {
                _state.value = EventUiState.Error(
                    e.message?.takeIf { it.isNotBlank() } ?: "Unable to create event. Please try again."
                )
            }
        }
    }

    fun loadWorkspace(id: Long) {
        viewModelScope.launch {
            _state.value = EventUiState.Loading
            try {
                _state.value = EventUiState.WorkspaceLoaded(repository.getWorkspace(id))
            } catch (e: Exception) {
                _state.value = EventUiState.Error(
                    e.message?.takeIf { it.isNotBlank() } ?: "Unable to load event."
                )
            }
        }
    }
}
