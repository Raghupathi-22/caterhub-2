package com.daily.cetaring.presentation.viewmodel

import android.util.Log
import com.daily.cetaring.auth.OtpAutoStatus
import com.daily.cetaring.auth.OtpSource
import com.daily.cetaring.data.remote.dto.CustomerAccessVerifyResponse
import com.daily.cetaring.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.net.UnknownHostException
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
class AuthViewModelOtpEntryTest {
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

    @Test
    fun smsPopulatesEntryForCurrentSession() {
        val viewModel = AuthViewModel(repository)
        val session = viewModel.beginOtpSession(listenForSms = true)
        assertTrue(viewModel.otpEntry.value.isListening)
        assertTrue(viewModel.otpEntry.value.inputEnabled)

        viewModel.onOtpSmsReceived("Your CaterHub OTP is 482913. Do not share it.", session)

        assertEquals("482913", viewModel.otpEntry.value.code)
        assertEquals(OtpSource.AUTO, viewModel.otpEntry.value.source)
    }

    @Test
    fun manualEntryWinsOverLateSms() {
        val viewModel = AuthViewModel(repository)
        val session = viewModel.beginOtpSession(listenForSms = true)
        viewModel.onOtpInput("1234")

        viewModel.onOtpSmsReceived("Your CaterHub OTP is 482913.", session)

        assertEquals("1234", viewModel.otpEntry.value.code)
        assertEquals(OtpAutoStatus.IGNORED_MANUAL, viewModel.otpEntry.value.autoStatus)
    }

    @Test
    fun resendIgnoresSmsFromOldSession() {
        val viewModel = AuthViewModel(repository)
        val old = viewModel.beginOtpSession(listenForSms = true)
        viewModel.beginOtpSession(listenForSms = true)

        viewModel.onOtpSmsReceived("Your CaterHub OTP is 482913.", old)

        assertEquals("", viewModel.otpEntry.value.code)
    }

    @Test
    fun retrieverTimeoutKeepsManualEntryAvailable() {
        val viewModel = AuthViewModel(repository)
        val session = viewModel.beginOtpSession(listenForSms = true)

        viewModel.onOtpAutoDetectionEnded(session, OtpAutoStatus.TIMED_OUT)
        viewModel.onOtpInput("905217")

        assertEquals(OtpAutoStatus.TIMED_OUT, viewModel.otpEntry.value.autoStatus)
        assertTrue(viewModel.otpEntry.value.inputEnabled)
        assertEquals("905217", viewModel.otpEntry.value.code)
    }

    @Test
    fun expiredOtpShowsExpiredMessage() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } throws IllegalArgumentException("OTP has expired")
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        assertEquals(AuthViewModel.OTP_EXPIRED_MESSAGE, (viewModel.otpUiState.value as OtpUiState.Error).message)
    }

    @Test
    fun noActiveOtpIsReportedAsExpired() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } throws IllegalArgumentException("No active OTP found or OTP expired")
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        assertEquals(AuthViewModel.OTP_EXPIRED_MESSAGE, (viewModel.otpUiState.value as OtpUiState.Error).message)
    }

    @Test
    fun networkFailureShowsConnectionMessage() = runTest {
        coEvery { repository.verifyCustomerAccess(any()) } throws UnknownHostException("Unable to resolve host")
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        val message = (viewModel.otpUiState.value as OtpUiState.Error).message
        assertTrue(message.contains("internet", ignoreCase = true))
    }

    @Test
    fun duplicateVerifyTapsSendOneRequest() = runTest {
        val gate = CompletableDeferred<CustomerAccessVerifyResponse>()
        coEvery { repository.verifyCustomerAccess(any()) } coAnswers { gate.await() }
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()
        viewModel.verifyCustomerAccessOtp("9876543210", "123456")
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.verifyCustomerAccess(any()) }
        gate.cancel()
    }

    @Test
    fun shortOtpIsRejectedLocally() = runTest {
        val viewModel = AuthViewModel(repository)

        viewModel.verifyCustomerAccessOtp("9876543210", "12345")
        advanceUntilIdle()

        assertTrue(viewModel.otpUiState.value is OtpUiState.Error)
        coVerify(exactly = 0) { repository.verifyCustomerAccess(any()) }
    }
}
