package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.BookingApiService
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import kotlinx.coroutines.flow.first

class BookingRepository(
    private val bookingApiService: BookingApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun createBooking(request: CreateMyBookingRequest): BookingResponse =
        executeNetworkCall { bookingApiService.createBooking(bearerToken(), request) }

    suspend fun getMyBookings(): List<BookingResponse> =
        executeNetworkCall { bookingApiService.getMyBookings(bearerToken()) }

    suspend fun getBooking(id: Long): BookingResponse =
        executeNetworkCall { bookingApiService.getBooking(bearerToken(), id) }

    suspend fun cancelBooking(id: Long) {
        executeNetworkCall { bookingApiService.cancelBooking(bearerToken(), id) }
    }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) throw IllegalStateException("Your session has expired. Please sign in again.")
        return "Bearer $token"
    }

    private suspend fun <T> executeNetworkCall(block: suspend () -> T): T = try {
        block()
    } catch (exception: Exception) {
        throw mapNetworkException(exception)
    }

    private fun mapNetworkException(exception: Exception): Exception =
        ApiErrorMapper.map(
            exception = exception,
            contextLabel = "booking",
            badRequestFallback = "Please check your booking details and try again.",
            defaultFallback = "Unable to complete booking. Please try again."
        )
}

