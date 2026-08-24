package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.CreateEventRequestDto
import com.daily.cetaring.data.remote.dto.EventChecklistItemDto
import com.daily.cetaring.data.remote.dto.EventDashboardDto
import com.daily.cetaring.data.remote.dto.EventTypeGroupDto
import com.daily.cetaring.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EventPlannerUiState(
    val loading: Boolean = false,
    val groups: List<EventTypeGroupDto> = emptyList(),
    val selectedType: String? = null,
    val selectedDisplayName: String? = null,
    val checklist: List<EventChecklistItemDto> = emptyList(),
    val selectedServices: Set<String> = emptySet(),
    val error: String? = null,
    val created: EventDashboardDto? = null
)

class EventPlannerViewModel(private val repository: EventRepository) : ViewModel() {
    private val _state = MutableStateFlow(EventPlannerUiState())
    val state: StateFlow<EventPlannerUiState> = _state.asStateFlow()

    fun loadTypes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.eventTypes() }
                .onSuccess { _state.value = _state.value.copy(loading = false, groups = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "Unable to load event types") }
        }
    }

    fun selectType(code: String, displayName: String, guestCount: Int = 100, budget: Double = 0.0) {
        _state.value = _state.value.copy(selectedType = code, selectedDisplayName = displayName, loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                repository.preview(mapOf(
                    "eventType" to code,
                    "guestCount" to guestCount,
                    "budget" to budget
                ))
            }.onSuccess { items ->
                _state.value = _state.value.copy(
                    loading = false,
                    checklist = items,
                    selectedServices = items.filter { it.required }.map { it.serviceKey }.toSet()
                )
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Unable to load event checklist")
            }
        }
    }

    fun toggleService(key: String, selected: Boolean) {
        val current = _state.value.selectedServices.toMutableSet()
        if (selected) current.add(key) else current.remove(key)
        _state.value = _state.value.copy(selectedServices = current)
    }

    fun create(request: CreateEventRequestDto) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, created = null)
            runCatching { repository.create(request) }
                .onSuccess { _state.value = _state.value.copy(loading = false, created = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "Unable to create event") }
        }
    }

    fun clearCreated() {
        _state.value = _state.value.copy(created = null)
    }
}
