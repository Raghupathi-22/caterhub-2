package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.BookingApiService
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketTimeoutException

class BookingRepository(
    private val bookingApiService: BookingApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun createBooking(request: CreateMyBookingRequest): BookingResponse {
        ensureBackendAvailable()
        return executeNetworkCall { bookingApiService.createBooking(bearerToken(), request) }
    }

    suspend fun getMyBookings(): List<BookingResponse> {
        ensureBackendAvailable()
        return executeNetworkCall { bookingApiService.getMyBookings(bearerToken()) }
    }

    suspend fun getBooking(id: Long): BookingResponse {
        ensureBackendAvailable()
        return executeNetworkCall { bookingApiService.getBooking(bearerToken(), id) }
    }

    suspend fun cancelBooking(id: Long) {
        ensureBackendAvailable()
        executeNetworkCall { bookingApiService.cancelBooking(bearerToken(), id) }
    }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) throw IllegalStateException("Please login again")
        return "Bearer $token"
    }

    private suspend fun ensureBackendAvailable() {
        try {
            val health = executeNetworkCall { healthApiService.health() }
            if (!health.isUp) throw ServerOfflineException()
        } catch (exception: ServerOfflineException) {
            throw exception
        } catch (exception: Exception) {
            throw mapNetworkException(exception)
        }
    }

    private suspend fun <T> executeNetworkCall(block: suspend () -> T): T = try {
        block()
    } catch (exception: Exception) {
        throw mapNetworkException(exception)
    }

    private fun mapNetworkException(exception: Exception): Exception = when (exception) {
        is ServerOfflineException -> exception
        is SocketTimeoutException -> ServerOfflineException("Unable to connect. Please check your internet connection and try again.")
        is EOFException, is ProtocolException -> IllegalStateException("Something went wrong. Please try again.")
        is IOException -> ServerOfflineException("Unable to connect. Please check your internet connection and try again.")
        is HttpException -> when (exception.code()) {
            400 -> IllegalArgumentException("Please check your booking details and try again.")
            401 -> IllegalStateException("Please login again")
            403 -> IllegalArgumentException("You do not have permission to access this booking.")
            404 -> IllegalArgumentException("Booking was not found.")
            409 -> IllegalArgumentException("This request already exists.")
            in 500..599 -> ServerOfflineException("Something went wrong. Please try again.")
            else -> IllegalArgumentException("Unable to complete booking. Please try again.")
        }
        else -> exception
    }
}
