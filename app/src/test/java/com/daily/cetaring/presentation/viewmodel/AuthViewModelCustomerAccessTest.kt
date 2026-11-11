package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.CustomerAccessVerifyResponse
import com.daily.cetaring.data.remote.dto.CustomerRegisterRequest
import com.daily.cetaring.data.remote.dto.SendOtpResponse
import com.daily.cetaring.data.remote.dto.UserDTO
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.OtpRateLimitedException
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelCustomerAccessTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<AuthRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    private fun auth(roles: List<String> = listOf("ROLE_CUSTOMER")) = AuthResponse(
        accessToken = "access",
        refreshToken = "refresh",
        tokenType = "Bearer",
        expiresIn = 900,
        user = UserDTO(id = 1, username = "u", phoneNumber = "+919876543210", isActive = true, isVerified = true, roles = roles)
    )

    private fun registrationRequired() = CustomerAccessVerifyResponse(
        status = CustomerAccessVerifyResponse.STATUS_REGISTRATION_REQUIRED,
        auth = null,
        registrationToken = "ticket",
        registrationExpiresInSeconds = 600,
        mobileNumber = "+919876543210"
    )

    @Test
    fun customerAccessPurposeIsAcceptedForSend() = runTest {
        coEvery { repository.sendOtp(any()) } returns SendOtpResponse(success = true, message = "sent")
        val viewModel = AuthViewModel(repository)

        viewModel.sendOtp("+919876543210", AuthViewModel.CUSTOMER_ACCESS_PURPOSE, "CUSTOMER")
        advanceUntilIdle()

        assertTrue(viewModel.otpUiState.value is OtpUiState.Sent)
        coVerify { repository.sendOtp(match { it.purpose == "CUSTOMER_ACCESS" && it.mobileNumber == "9876543210" }) }
    }

    @Test
    fun rateLimitedSendShowsRetryCountdown() = runTest {
        coEvery { repository.sendOtp(any()) } throws OtpRateLimitedException("Please wait before requesting another OTP", 42)
        val viewModel = AuthViewModel(repository)

        viewModel.sendOtp("9876543210", AuthViewModel.CUSTOMER_ACCESS_PURPOSE, "CUSTOMER")
        advanceUntilIdle()

        val state = viewModel.otpUiState.value as OtpUiState.Error
        assertEquals(42, state.retryAfterSeconds)
    }

    @Test
    fun existingCustomerIsSignedInDirectly() = runTest {
        val response = auth()
        coEvery { repository.verifyCustomerAccess(any()) } returns CustomerAccessVerifyResponse(
            CustomerAccessVerifyResponse.STATUS_AUTHENTICATED, response, null, null, "+919876543210"
        )
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("+919876543210", "123456")
        advanceUntilIdle()

        assertEquals(OtpUiState.Success(response), viewModel.otpUiState.value)
    }

    @Test
    fun newNumberMovesToAccountCreationWithoutSession() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } returns registrationRequired()
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        val state = viewModel.otpUiState.value as OtpUiState.RegistrationRequired
        assertEquals("+919876543210", state.verifiedMobileNumber)
        assertEquals("ticket", state.registrationToken)
        coVerify(exactly = 0) { repository.registerCustomer(any()) }
    }

    @Test
    fun incorrectOtpShowsError() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } throws IllegalArgumentException("Invalid OTP")
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "000000")
        advanceUntilIdle()

        assertEquals("OTP expired or invalid. Please request a new OTP.", (viewModel.otpUiState.value as OtpUiState.Error).message)
    }

    @Test
    fun registrationSendsVerifiedNumberTicketAndCleanName() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } returns registrationRequired()
        val request = slot<CustomerRegisterRequest>()
        val response = auth()
        coEvery { repository.registerCustomer(capture(request)) } returns response
        val viewModel = AuthViewModel(repository)
        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        viewModel.completeCustomerRegistration("  Ravi   Kumar ")
        advanceUntilIdle()

        assertEquals(CustomerRegisterRequest("+919876543210", "ticket", "Ravi Kumar"), request.captured)
        assertEquals(OtpUiState.Success(response), viewModel.otpUiState.value)
    }

    @Test
    fun invalidNameIsRejectedLocally() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } returns registrationRequired()
        val viewModel = AuthViewModel(repository)
        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        viewModel.completeCustomerRegistration("R")
        advanceUntilIdle()

        val state = viewModel.otpUiState.value as OtpUiState.RegistrationRequired
        assertEquals("Full name must have at least 2 characters.", state.errorMessage)
        coVerify(exactly = 0) { repository.registerCustomer(any()) }
    }

    @Test
    fun repeatedContinueTapsCreateOnlyOneRequest() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } returns registrationRequired()
        val gate = CompletableDeferred<AuthResponse>()
        coEvery { repository.registerCustomer(any()) } coAnswers { gate.await() }
        val viewModel = AuthViewModel(repository)
        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        viewModel.completeCustomerRegistration("Ravi Kumar")
        viewModel.completeCustomerRegistration("Ravi Kumar")
        advanceUntilIdle()
        assertTrue((viewModel.otpUiState.value as OtpUiState.RegistrationRequired).isSubmitting)
        gate.complete(auth())
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.registerCustomer(any()) }
        assertTrue(viewModel.otpUiState.value is OtpUiState.Success)
    }

    @Test
    fun failedRegistrationKeepsTicketForRetry() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } returns registrationRequired()
        coEvery { repository.registerCustomer(any()) } throws IllegalArgumentException("Server is unavailable")
        val viewModel = AuthViewModel(repository)
        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        viewModel.completeCustomerRegistration("Ravi Kumar")
        advanceUntilIdle()

        val state = viewModel.otpUiState.value as OtpUiState.RegistrationRequired
        assertEquals("ticket", state.registrationToken)
        assertEquals(false, state.isSubmitting)
        assertEquals("Server is unavailable", state.errorMessage)
    }

    @Test
    fun nameValidationMatchesBackendRules() {
        assertNull(AuthViewModel.validateFullName("Ravi Kumar"))
        assertNull(AuthViewModel.validateFullName("రవి కుమార్"))
        assertNull(AuthViewModel.validateFullName("Anne-Marie D'Souza"))
        assertEquals("Please enter your full name.", AuthViewModel.validateFullName("   "))
        assertTrue(AuthViewModel.validateFullName("Ravi123") != null)
        assertTrue(AuthViewModel.validateFullName("x".repeat(81)) != null)
    }
}
