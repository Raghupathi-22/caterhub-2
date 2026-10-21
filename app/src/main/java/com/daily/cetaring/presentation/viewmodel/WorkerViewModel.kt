package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.AssignmentResponse
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.UpdateWorkerProfileRequest
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

    private val _editState = MutableStateFlow(WorkerEditProfileState())
    val editState: StateFlow<WorkerEditProfileState> = _editState.asStateFlow()

    /** Loads the latest saved profile from the backend for the edit form. */
    fun loadProfileForEdit() {
        if (_editState.value.isSaving) return
        _editState.value = WorkerEditProfileState(isLoading = true)
        viewModelScope.launch {
            _editState.value = try {
                WorkerEditProfileState(profile = workerRepository.getMyProfile())
            } catch (exception: Exception) {
                WorkerEditProfileState(loadError = exception.message ?: "Unable to load your profile")
            }
        }
    }

    fun saveProfile(request: UpdateWorkerProfileRequest) {
        val current = _editState.value
        if (current.isSaving || current.profile == null) return
        _editState.value = current.copy(isSaving = true, saveError = null)
        viewModelScope.launch {
            try {
                val updated = workerRepository.updateMyProfile(request)
                _uiState.value = WorkerUiState.ProfileLoaded(updated)
                _profileSavedNotice.value = true
                _editState.value = _editState.value.copy(profile = updated, isSaving = false, saved = true)
            } catch (exception: Exception) {
                // Keep the previously saved profile; the server did not confirm the update.
                _editState.value = _editState.value.copy(
                    isSaving = false,
                    saveError = exception.message ?: "Unable to save your profile. Please try again."
                )
            }
        }
    }

    fun clearEditSaveError() {
        _editState.value = _editState.value.copy(saveError = null)
    }

    private val _profileSavedNotice = MutableStateFlow(false)
    /** True once after a confirmed save so the profile screen can show a success message. */
    val profileSavedNotice: StateFlow<Boolean> = _profileSavedNotice.asStateFlow()

    fun consumeProfileSavedNotice() {
        _profileSavedNotice.value = false
    }

    fun resetEditState() {
        _editState.value = WorkerEditProfileState()
    }
}

data class WorkerEditProfileState(
    val profile: WorkerProfileResponse? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saved: Boolean = false
)

/** Client-side checks mirroring the backend rules for PUT workers/profiles/me. */
object WorkerProfileEditRules {
    const val MIN_AGE = 18
    const val MAX_AGE = 75
    const val MAX_NAME_LENGTH = 100
    const val MAX_TEXT_LENGTH = 1000

    enum class Field { NAME, AGE, SKILLS, AREAS, LANGUAGES, BIO }

    fun validate(
        name: String,
        age: String,
        skills: String,
        areas: String,
        languages: String,
        bio: String
    ): Map<Field, String> = buildMap {
        val trimmedName = name.trim()
        when {
            trimmedName.isEmpty() -> put(Field.NAME, "Please enter your name.")
            trimmedName.length < 2 || trimmedName.length > MAX_NAME_LENGTH ->
                put(Field.NAME, "Name must be between 2 and $MAX_NAME_LENGTH characters.")
        }
        val trimmedAge = age.trim()
        if (trimmedAge.isNotEmpty()) {
            val value = trimmedAge.toIntOrNull()
            if (value == null || value < MIN_AGE || value > MAX_AGE) {
                put(Field.AGE, "Age must be between $MIN_AGE and $MAX_AGE.")
            }
        }
        requiredText(skills, "skills")?.let { put(Field.SKILLS, it) }
        requiredText(areas, "preferred service areas")?.let { put(Field.AREAS, it) }
        requiredText(languages, "languages")?.let { put(Field.LANGUAGES, it) }
        if (bio.trim().length > MAX_TEXT_LENGTH) {
            put(Field.BIO, "Bio must be $MAX_TEXT_LENGTH characters or fewer.")
        }
    }

    fun toRequest(
        name: String,
        age: String,
        skills: String,
        areas: String,
        languages: String,
        bio: String
    ): UpdateWorkerProfileRequest = UpdateWorkerProfileRequest(
        fullName = name.trim().replace(Regex("\\s+"), " "),
        age = age.trim().toIntOrNull(),
        skills = skills.trim(),
        preferredAreas = areas.trim(),
        languages = languages.trim(),
        bio = bio.trim().ifEmpty { null }
    )

    private fun requiredText(value: String, label: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> "Please enter your $label."
            trimmed.length > MAX_TEXT_LENGTH -> "Please keep $label to $MAX_TEXT_LENGTH characters or fewer."
            else -> null
        }
    }
}
