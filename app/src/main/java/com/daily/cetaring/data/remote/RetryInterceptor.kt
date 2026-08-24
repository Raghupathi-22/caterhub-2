package com.daily.cetaring.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(
    private val maxRetries: Int = 1
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                val response = chain.proceed(chain.request())
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
}

