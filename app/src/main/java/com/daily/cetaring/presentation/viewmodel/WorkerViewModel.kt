package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.AssignmentResponse
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.WorkerDashboardResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.data.remote.dto.WorkerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkerUiState {
    data object Idle : WorkerUiState()
    data object Loading : WorkerUiState()
    data class ProfileLoaded(val profile: WorkerProfileResponse) : WorkerUiState()
    data class DashboardLoaded(val dashboard: WorkerDashboardResponse) : WorkerUiState()
    data class JobsLoaded(val jobs: List<StaffingJobResponse>) : WorkerUiState()
    data class JobDetailsLoaded(val job: StaffingJobResponse) : WorkerUiState()
    data class MyJobsLoaded(val jobs: List<WorkerJobResponse>) : WorkerUiState()
    data class JobAccepted(val message: String, val job: StaffingJobResponse) : WorkerUiState()
    data class AssignmentAccepted(val assignment: AssignmentResponse) : WorkerUiState()
    data class Submitted(val profile: WorkerProfileResponse) : WorkerUiState()
    data class Error(val message: String) : WorkerUiState()
}

class WorkerViewModel(private val workerRepository: WorkerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkerUiState>(WorkerUiState.Idle)
    val uiState: StateFlow<WorkerUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.ProfileLoaded(workerRepository.getMyProfile())
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to load worker profile")
            }
        }
    }

    fun submitProfile(request: CreateWorkerProfileRequest) {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.Submitted(workerRepository.createMyProfile(request))
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to submit worker profile")
            }
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.DashboardLoaded(workerRepository.getDashboard())
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to load worker dashboard")
            }
        }
    }

    fun loadAvailableJobs(role: WorkerType? = null, area: String? = null, search: String? = null) {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.JobsLoaded(workerRepository.getAvailableJobs(role, area, search))
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to load catering jobs")
            }
        }
    }

    fun loadJob(jobId: Long) {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.JobDetailsLoaded(workerRepository.getJob(jobId))
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to load job details")
            }
        }
    }

    fun acceptJob(jobId: Long) {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                val response = workerRepository.acceptJob(jobId)
                _uiState.value = WorkerUiState.JobAccepted(response.message, response.job)
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to accept job")
            }
        }
    }

    fun loadMyJobs() {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.MyJobsLoaded(workerRepository.getMyJobs())
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to load my jobs")
            }
        }
    }

    fun updateAvailability(available: Boolean) {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                workerRepository.updateAvailability(available)
                loadDashboard()
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to update availability")
            }
        }
    }

    fun acceptAssignment(assignmentId: Long) {
        viewModelScope.launch {
            _uiState.value = WorkerUiState.Loading
            try {
                _uiState.value = WorkerUiState.AssignmentAccepted(workerRepository.acceptAssignment(assignmentId))
                loadDashboard()
            } catch (exception: Exception) {
                _uiState.value = WorkerUiState.Error(exception.message ?: "Unable to accept job")
            }
        }
    }

    fun reset() {
        _uiState.value = WorkerUiState.Idle
    }
}
