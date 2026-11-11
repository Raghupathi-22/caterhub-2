package com.daily.cetaring.data.repository

import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketTimeoutException

object ApiErrorMapper {
    fun map(
        exception: Exception,
        contextLabel: String,
        badRequestFallback: String,
        defaultFallback: String
    ): Exception {
        return when (exception) {
            is ServerOfflineException -> exception
            is SocketTimeoutException -> ServerOfflineException("Connection timed out. Please try again.")
            is EOFException, is ProtocolException ->
                IllegalStateException("Received an invalid response from the server. Please try again.")
            is IOException ->
                ServerOfflineException("Please check your internet connection.")
            is HttpException -> mapHttpException(exception, contextLabel, badRequestFallback, defaultFallback)
            else -> exception
        }
    }

    private fun mapHttpException(
        exception: HttpException,
        contextLabel: String,
        badRequestFallback: String,
        defaultFallback: String
    ): Exception {
        val body = runCatching { exception.response()?.errorBody()?.string() }.getOrNull()
        val apiMessage = apiErrorMessage(body)
        return when (exception.code()) {
            400, 422 -> IllegalArgumentException(apiMessage ?: badRequestFallback)
            401 -> IllegalStateException("Your session has expired. Please sign in again.")
            403 -> IllegalArgumentException("You don't have permission to perform this action.")
            404 -> IllegalArgumentException("We couldn't find this $contextLabel.")
            409 -> IllegalArgumentException(apiMessage ?: "This $contextLabel has already been updated.")
            in 500..599 -> ServerOfflineException(serverErrorMessage(body))
            else -> IllegalArgumentException(apiMessage ?: defaultFallback)
        }
    }

    /** Never shows raw server text for 5xx; only the opaque reference id used to find the server log. */
    internal fun serverErrorMessage(body: String?): String {
        val errorId = body?.let {
            Regex("\"errorId\"\\s*:\\s*\"([A-Za-z0-9-]{1,40})\"").find(it)?.groupValues?.getOrNull(1)
        }
        return if (errorId != null) {
            "Something went wrong on our server. Please try again. (Ref: $errorId)"
        } else {
            "Something went wrong on our server. Please try again."
        }
    }

    internal fun apiErrorMessage(body: String?): String? {
        if (body == null) return null
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
            ?: Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
            ?: Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
    }
}

