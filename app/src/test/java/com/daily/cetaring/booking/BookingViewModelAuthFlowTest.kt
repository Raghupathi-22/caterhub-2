package com.daily.cetaring.booking

import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.BookingRepository
import com.daily.cetaring.data.repository.BookingSessionExpiredException
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.presentation.viewmodel.BookingUiState
import com.daily.cetaring.presentation.viewmodel.BookingViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class BookingViewModelAuthFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val bookingRepository = mockk<BookingRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val workerRepository = mockk<WorkerRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitWithoutSessionRequestsOtpAndSkipsApiCall() = runTest {
        coEvery { authRepository.getAccessToken() } returns null
        coEvery { authRepository.getRefreshToken() } returns null
        every { authRepository.rolesFlow } returns flowOf(null)

        val viewModel = BookingViewModel(bookingRepository, authRepository, workerRepository)
        viewModel.updateDraft { validDraft() }

        viewModel.submitBooking()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BookingUiState.AuthRequired)
        state as BookingUiState.AuthRequired
        assertEquals("Login to confirm your booking.", state.message)
        assertTrue(!state.isSessionExpired)
        coVerify(exactly = 0) { bookingRepository.createBooking(any()) }
    }

    @Test
    fun submitWithCustomerSessionSubmitsDirectly() = runTest {
        coEvery { authRepository.getAccessToken() } returns "access-token"
        coEvery { authRepository.getRefreshToken() } returns "refresh-token"
        every { authRepository.rolesFlow } returns flowOf("ROLE_CUSTOMER")
        coEvery { bookingRepository.createBooking(any()) } returns sampleBookingResponse()

        val viewModel = BookingViewModel(bookingRepository, authRepository, workerRepository)
        viewModel.updateDraft { validDraft() }

        viewModel.submitBooking()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is BookingUiState.Submitted)
        coVerify(exactly = 1) { bookingRepository.createBooking(any()) }
    }

    @Test
    fun expiredSessionAfterAuthenticatedSubmitRequestsReLogin() = runTest {
        coEvery { authRepository.getAccessToken() } returns "expired-access-token"
        coEvery { authRepository.getRefreshToken() } returns null
        every { authRepository.rolesFlow } returns flowOf("ROLE_CUSTOMER")
        coEvery { bookingRepository.createBooking(any()) } throws BookingSessionExpiredException("expired")

        val viewModel = BookingViewModel(bookingRepository, authRepository, workerRepository)
        viewModel.updateDraft { validDraft() }

        viewModel.submitBooking()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BookingUiState.AuthRequired)
        state as BookingUiState.AuthRequired
        assertTrue(state.isSessionExpired)
        assertEquals(
            "Your session has expired. Please sign in again to submit your booking.",
            state.message
        )
        coVerify(exactly = 1) { bookingRepository.createBooking(any()) }
    }

    @Test
    fun duplicateSubmitTriggersOnlyOneBookingRequest() = runTest {
        coEvery { authRepository.getAccessToken() } returns "access-token"
        coEvery { authRepository.getRefreshToken() } returns "refresh-token"
        every { authRepository.rolesFlow } returns flowOf("ROLE_CUSTOMER")
        coEvery { bookingRepository.createBooking(any()) } returns sampleBookingResponse()

        val viewModel = BookingViewModel(bookingRepository, authRepository, workerRepository)
        viewModel.updateDraft { validDraft() }

        viewModel.submitBooking()
        viewModel.submitBooking()
        advanceUntilIdle()

        coVerify(exactly = 1) { bookingRepository.createBooking(any()) }
    }

    @Test
    fun otpSuccessResumesPendingSubmissionAutomatically() = runTest {
        var accessToken: String? = null
        var refreshToken: String? = null
        var roles: String? = null

        coEvery { authRepository.getAccessToken() } answers { accessToken }
        coEvery { authRepository.getRefreshToken() } answers { refreshToken }
        every { authRepository.rolesFlow } answers { flowOf(roles) }
        coEvery { bookingRepository.createBooking(any()) } returns sampleBookingResponse()

        val viewModel = BookingViewModel(bookingRepository, authRepository, workerRepository)
        viewModel.updateDraft { validDraft() }

        viewModel.submitBooking()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is BookingUiState.AuthRequired)

        accessToken = "new-access-token"
        refreshToken = "new-refresh-token"
        roles = "ROLE_CUSTOMER"

        viewModel.resumePendingSubmissionAfterAuth()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is BookingUiState.Submitted)
        coVerify(exactly = 1) { bookingRepository.createBooking(any()) }
    }

    private fun validDraft(): BookingDraft = BookingDraft(
        eventType = "Corporate Event",
        guestCount = 150,
        selectedFoodServices = setOf(CateringMealServiceType.LUNCH),
        cateringPlan = BookingOptions.planCustomMenu,
        foodRequirements = "jj",
        eventDate = LocalDate.now().plusDays(1).toString(),
        eventTime = "22:00",
        address = "Plot 10",
        area = "Kondapur",
        city = "Hyderabad"
    )

    private fun sampleBookingResponse(): BookingResponse = BookingResponse(
        id = 101L,
        businessId = 1L,
        userId = 2L,
        bookingReference = "BK-101",
        eventType = "Corporate Event",
        guestCount = 150,
        mealType = "Lunch | Custom Menu",
        eventDateTime = "2026-09-08T22:00:00",
        deliveryAddress = "Plot 10, Kondapur, Hyderabad",
        specialInstructions = "Food requirements: jj",
        totalAmount = BigDecimal("104850"),
        status = "CREATED",
        paymentStatus = "PENDING",
        createdAt = "2026-09-07T13:00:00"
    )
}
