package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.remote.dto.WorkerStatus
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.domain.catalog.WorkerProfileCatalog
import com.daily.cetaring.domain.worker.WorkerRegistrationDraft
import com.daily.cetaring.domain.worker.WorkerRegistrationRules
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerRegistrationSubmissionTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<WorkerRepository>()

    private val draft = WorkerRegistrationDraft(
        step = WorkerRegistrationRules.LAST_STEP,
        categoryId = "catering-food",
        roleId = "chef",
        experienceYears = 0,
        skills = listOf("Biryani", "Bulk Cooking"),
        areas = listOf("Kukatpally"),
        languages = listOf("Telugu", "Hindi")
    )

    private fun profile() = WorkerProfileResponse(
        id = 1, userId = 2, username = "ravi", fullName = "Ravi", workerType = null,
        status = WorkerStatus.values().first(), experienceYears = 0, skills = "Biryani, Bulk Cooking",
        preferredAreas = "Kukatpally", languages = "Telugu, Hindi", bio = null, rating = 0.0,
        totalRatings = 0, approvedAt = null, rejectionReason = null, createdAt = null, updatedAt = null
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun submitsDraftAsExistingCreateRequest() = runTest(dispatcher) {
        coEvery { repository.createMyProfile(any()) } returns profile()
        val viewModel = WorkerViewModel(repository)

        viewModel.submitProfile(WorkerRegistrationRules.toCreateRequest(draft)!!)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is WorkerUiState.Submitted)
        coVerify(exactly = 1) {
            repository.createMyProfile(match {
                it.experienceYears == 0 && it.skills == "Biryani, Bulk Cooking" &&
                    it.preferredAreas == "Kukatpally" && it.languages == "Telugu, Hindi" && it.bio == null
            })
        }
    }

    @Test
    fun duplicateSubmitWhileInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<WorkerProfileResponse>()
        coEvery { repository.createMyProfile(any()) } coAnswers { gate.await() }
        val viewModel = WorkerViewModel(repository)
        val request = WorkerRegistrationRules.toCreateRequest(draft)!!

        viewModel.submitProfile(request)
        advanceUntilIdle()
        viewModel.submitProfile(request)
        viewModel.submitProfile(request)
        gate.complete(profile())
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.createMyProfile(any()) }
        assertTrue(viewModel.uiState.value is WorkerUiState.Submitted)
    }

    @Test
    fun backendValidationErrorIsSurfacedAndRetryIsAllowed() = runTest(dispatcher) {
        coEvery { repository.createMyProfile(any()) } throws IllegalArgumentException("Worker profile already exists")
        val viewModel = WorkerViewModel(repository)
        val request = WorkerRegistrationRules.toCreateRequest(draft)!!

        viewModel.submitProfile(request)
        advanceUntilIdle()
        assertEquals("Worker profile already exists", (viewModel.uiState.value as WorkerUiState.Error).message)

        coEvery { repository.createMyProfile(any()) } returns profile()
        viewModel.submitProfile(request)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is WorkerUiState.Submitted)
    }

    @Test
    fun profileEditUsesSameListFormatAndPassesEditRules() {
        // Existing free-text values are parsed into chips, edited, and saved in the same format.
        val existing = WorkerProfileCatalog.parseList("Biryani ; bulk cooking, Biryani")
        val edited = WorkerRegistrationRules.toggle(existing, "Live Counters", WorkerProfileCatalog.MAX_SKILLS)
        val skills = WorkerProfileCatalog.joinList(edited)
        assertEquals("Biryani, bulk cooking, Live Counters", skills)

        val areas = WorkerProfileCatalog.joinList(listOf("Miyapur", "Kondapur"))
        val languages = WorkerProfileCatalog.joinList(listOf("Telugu"))
        assertTrue(WorkerProfileEditRules.validate("Ravi Kumar", "", skills, areas, languages, "").isEmpty())
        val request = WorkerProfileEditRules.toRequest("Ravi Kumar", "", skills, areas, languages, "")
        assertEquals("Miyapur, Kondapur", request.preferredAreas)

        val cleared = WorkerProfileCatalog.joinList(WorkerRegistrationRules.toggle(listOf("Telugu"), "Telugu", 10))
        assertTrue(WorkerProfileEditRules.validate("Ravi Kumar", "", skills, areas, cleared, "")
            .containsKey(WorkerProfileEditRules.Field.LANGUAGES))
    }
}
