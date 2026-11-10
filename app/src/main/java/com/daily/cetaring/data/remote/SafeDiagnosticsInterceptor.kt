package com.daily.cetaring.data.remote

import com.daily.cetaring.diagnostics.ReleaseDiagnostics
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Release-safe HTTP diagnostics: logs only method, path, status, duration and
 * exception class. Never logs query strings, headers (tokens), or bodies
 * (OTPs, phone numbers, addresses). Long digit runs in the path are masked.
 */
class SafeDiagnosticsInterceptor(
    private val log: (String) -> Unit = ReleaseDiagnostics::info,
    private val logError: (String) -> Unit = ReleaseDiagnostics::error
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.method
        val path = safePath(request.url.encodedPath)
        val startNs = System.nanoTime()
        try {
            val response = chain.proceed(request)
            val tookMs = (System.nanoTime() - startNs) / 1_000_000
            val line = "CATERHUB_HTTP method=$method path=$path status=${response.code} durationMs=$tookMs"
            if (response.isSuccessful) log(line) else logError(line)
            return response
        } catch (exception: IOException) {
            val tookMs = (System.nanoTime() - startNs) / 1_000_000
            logError(
                "CATERHUB_HTTP method=$method path=$path status=NA durationMs=$tookMs exception=${exception::class.java.simpleName}"
            )
            throw exception
        }
    }

    companion object {
        private val LONG_DIGITS = Regex("\\d{6,}")

        fun safePath(encodedPath: String): String = encodedPath.replace(LONG_DIGITS, "***")
    }
}
