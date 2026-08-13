package com.daily.cetaring.data.remote

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class RetryInterceptorTest {

    @Test
    fun retriesOnceWhenServerReturnsFiveHundred() {
        val request = Request.Builder()
            .url("https://example.com/api/v1/health")
            .build()
        var attempts = 0
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                attempts++
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(if (attempts == 1) 500 else 200)
                    .message(if (attempts == 1) "Server Error" else "OK")
                    .body("".toResponseBody())
                    .build()
            }
            override fun connection() = null
            override fun call(): okhttp3.Call = throw UnsupportedOperationException()
            override fun connectTimeoutMillis(): Int = 10_000
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 10_000
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 10_000
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        val response = RetryInterceptor(maxRetries = 1).intercept(chain)

        assertEquals(2, attempts)
        assertEquals(200, response.code)
        response.close()
    }
}
