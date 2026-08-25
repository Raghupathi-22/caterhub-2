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
        val apiMessage = apiErrorMessage(exception)
        return when (exception.code()) {
            400, 422 -> IllegalArgumentException(apiMessage ?: badRequestFallback)
            401 -> IllegalStateException("Your session has expired. Please sign in again.")
            403 -> IllegalArgumentException("You don't have permission to perform this action.")
            404 -> IllegalArgumentException("We couldn't find this $contextLabel.")
            409 -> IllegalArgumentException("This $contextLabel has already been updated.")
            in 500..599 -> ServerOfflineException("Something went wrong on our server. Please try again.")
            else -> IllegalArgumentException(apiMessage ?: defaultFallback)
        }
    }

    private fun apiErrorMessage(exception: HttpException): String? {
        val body = exception.response()?.errorBody()?.string() ?: return null
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
            ?: Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
    }
}

