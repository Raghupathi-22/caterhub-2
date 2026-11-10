package com.daily.cetaring.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Retries only idempotent requests (GET/HEAD/OPTIONS). Non-idempotent requests
 * such as POST /auth/otp/send must never be replayed: a replay after a slow
 * response re-sends the OTP and trips the backend resend cooldown.
 */
class RetryInterceptor(
    private val maxRetries: Int = 1
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!isIdempotent(request.method)) {
            return chain.proceed(request)
        }

        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                val response = chain.proceed(request)
                if (response.code < 500 || attempt == maxRetries) {
                    return response
                }
                response.close()
            } catch (exception: IOException) {
                lastException = exception
                if (attempt == maxRetries) {
                    throw exception
                }
            }
            attempt++
        }

        throw lastException ?: IOException("Network request failed")
    }

    companion object {
        private val IDEMPOTENT_METHODS = setOf("GET", "HEAD", "OPTIONS")

        fun isIdempotent(method: String): Boolean = method.uppercase() in IDEMPOTENT_METHODS
    }
}

