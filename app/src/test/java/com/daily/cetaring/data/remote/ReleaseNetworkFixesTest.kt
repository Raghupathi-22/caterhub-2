package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.SendOtpResponse
import com.daily.cetaring.data.repository.OtpRateLimitedException
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.SocketTimeoutException

class ReleaseNetworkFixesTest {

    private fun request(method: String): Request {
        val builder = Request.Builder().url("https://example.test/api/v1/auth/otp/send?mobile=9876543210")
        return if (method == "GET") builder.get().build()
        else builder.method(method, "{}".toRequestBody("application/json".toMediaType())).build()
    }

    private fun response(request: Request, code: Int): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("x")
        .body("{}".toResponseBody(null))
        .build()

    @Test
    fun postIsNeverRetriedAfterTimeout() {
        val req = request("POST")
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns req
        every { chain.proceed(any()) } throws SocketTimeoutException("timeout")

        try {
            RetryInterceptor(maxRetries = 1).intercept(chain)
            fail("expected timeout")
        } catch (_: SocketTimeoutException) {
        }
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun postIsNeverRetriedAfterServerError() {
        val req = request("POST")
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns req
        every { chain.proceed(any()) } returns response(req, 503)

        assertEquals(503, RetryInterceptor(maxRetries = 1).intercept(chain).code)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun getIsRetriedOnceAfterTimeout() {
        val req = request("GET")
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns req
        var calls = 0
        every { chain.proceed(any()) } answers {
            calls++
            if (calls == 1) throw SocketTimeoutException("timeout") else response(req, 200)
        }

        assertEquals(200, RetryInterceptor(maxRetries = 1).intercept(chain).code)
        assertEquals(2, calls)
    }

    @Test
    fun diagnosticsLogOnlyMethodPathAndStatus() {
        val req = request("POST")
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns req
        every { chain.proceed(any()) } returns response(req, 429)
        val lines = mutableListOf<String>()

        SafeDiagnosticsInterceptor(log = { lines += it }, logError = { lines += it }).intercept(chain)

        assertEquals(1, lines.size)
        val line = lines.single()
        assertTrue(line, line.contains("method=POST path=/api/v1/auth/otp/send status=429"))
        assertFalse("query string must not be logged", line.contains("9876543210"))
        assertFalse(line.contains("{}"))
    }

    @Test
    fun diagnosticsMaskLongDigitRunsInPath() {
        assertEquals("/api/v1/users/***/orders/42", SafeDiagnosticsInterceptor.safePath("/api/v1/users/9876543210/orders/42"))
    }

    @Test
    fun retryAfterPrefersHeaderThenBody() {
        assertEquals(42, OtpRateLimitedException.parseRetryAfterSeconds("42", """{"data":{"retryAfterSeconds":7}}"""))
        assertEquals(7, OtpRateLimitedException.parseRetryAfterSeconds(null, """{"data":{"retryAfterSeconds":7}}"""))
        assertNull(OtpRateLimitedException.parseRetryAfterSeconds(null, """{"message":"Please wait"}"""))
        assertEquals(3600, OtpRateLimitedException.parseRetryAfterSeconds("999999", null))
        assertTrue(OtpRateLimitedException.isCooldownMessage("Please wait before requesting another OTP"))
    }

    @Test
    fun resendCooldownUsesBackendCooldownNotOtpExpiry() {
        val response = Gson().fromJson(
            """{"success":true,"expiresInSeconds":300,"deliveryChannel":"SMS","resendCooldownSeconds":45}""",
            SendOtpResponse::class.java
        )
        assertEquals(45L, response.resendCooldownSeconds)
        assertEquals(45, AuthViewModel.resendCooldownFrom(response))
        assertEquals(
            AuthViewModel.DEFAULT_RESEND_COOLDOWN_SECONDS,
            AuthViewModel.resendCooldownFrom(SendOtpResponse(success = true, expiresInSeconds = 300))
        )
    }
}
