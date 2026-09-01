package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.BookingApiService
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CustomerBookingSource
import com.daily.cetaring.data.remote.dto.CustomerBookingUiModel
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestBookingResponse
import com.daily.cetaring.domain.catalog.ServiceCatalog
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import retrofit2.HttpException

class BookingRepository(
    private val bookingApiService: BookingApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    private val eventDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun createBooking(request: CreateMyBookingRequest): BookingResponse =
        executeNetworkCall { bookingApiService.createBooking(bearerToken(), request) }

    suspend fun getMyBookings(): List<BookingResponse> =
        executeNetworkCall { bookingApiService.getMyBookings(bearerToken()) }

    suspend fun getUnifiedMyBookings(): List<CustomerBookingUiModel> {
        val token = bearerToken()
        return executeNetworkCall(ApiOperation.LIST) {
            val catering = bookingApiService.getMyBookings(token).map { it.toUiModel() }
            val services = bookingApiService.getMyServiceRequests(token).map { it.toUiModel() }
            (catering + services).sortedByDescending { it.createdAt.orEmpty() }
        }
    }

    suspend fun getBooking(id: Long): BookingResponse =
        executeNetworkCall { bookingApiService.getBooking(bearerToken(), id) }

    suspend fun getUnifiedBooking(id: Long, source: CustomerBookingSource): CustomerBookingUiModel {
        val token = bearerToken()
        return executeNetworkCall(ApiOperation.DETAIL) {
            when (source) {
                CustomerBookingSource.CATERING -> bookingApiService.getBooking(token, id).toUiModel()
                CustomerBookingSource.SERVICE_REQUEST -> bookingApiService.getServiceRequest(token, id).toUiModel()
            }
        }
    }

    suspend fun cancelBooking(id: Long) {
        executeNetworkCall { bookingApiService.cancelBooking(bearerToken(), id) }
    }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) throw IllegalStateException("Your session has expired. Please sign in again.")
        return "Bearer $token"
    }

    private suspend fun <T> executeNetworkCall(
        operation: ApiOperation = ApiOperation.DEFAULT,
        block: suspend () -> T
    ): T = try {
        block()
    } catch (exception: Exception) {
        throw mapNetworkException(exception, operation)
    }

    private fun mapNetworkException(exception: Exception, operation: ApiOperation): Exception =
        when {
            exception is HttpException && exception.code() == 401 ->
                IllegalStateException("Session expired. Please login again.")
            exception is HttpException && exception.code() == 403 ->
                IllegalArgumentException("You don't have permission to view these bookings.")
            exception is HttpException && exception.code() == 404 ->
                if (operation == ApiOperation.DETAIL) {
                    IllegalArgumentException("Booking not found.")
                } else {
                    IllegalArgumentException("Unable to load booking history.")
                }
            exception is HttpException && exception.code() == 409 ->
                IllegalArgumentException("This booking cannot be completed because its status has changed.")
            exception is HttpException && exception.code() in 500..599 ->
                ServerOfflineException("CaterHub is temporarily unavailable. Please try again.")
            else -> ApiErrorMapper.map(
                exception = exception,
                contextLabel = "booking",
                badRequestFallback = "Please check your booking details and try again.",
                defaultFallback = "Unable to complete booking. Please try again."
            )
        }

    private enum class ApiOperation {
        DEFAULT,
        LIST,
        DETAIL
    }

    private fun BookingResponse.toUiModel(): CustomerBookingUiModel {
        val parsedDateTime = runCatching { LocalDateTime.parse(eventDateTime, eventDateTimeFormatter) }.getOrNull()
        val eventDate = parsedDateTime?.toLocalDate()?.toString() ?: eventDateTime.take(10)
        val startTime = parsedDateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm"))
            ?: eventDateTime.substringAfter('T').take(5)
        val services = listOf(mealType).filter { it.isNotBlank() }
        return CustomerBookingUiModel(
            id = id,
            source = CustomerBookingSource.CATERING,
            categoryId = "catering-food",
            categoryName = ServiceCatalog.category("catering-food")?.title ?: "Catering & Food",
            eventType = eventType,
            eventDate = eventDate,
            startTime = startTime,
            endTime = null,
            address = deliveryAddress,
            area = null,
            services = services,
            totalAmount = totalAmount,
            quoteBased = totalAmount == null,
            status = status,
            createdAt = createdAt
        )
    }

    private fun ServiceRequestBookingResponse.toUiModel(): CustomerBookingUiModel {
        val category = ServiceCatalog.categories.firstOrNull { it.serviceType == serviceType }
            ?: ServiceCatalog.category("other-event-services")
        return CustomerBookingUiModel(
            id = id,
            source = CustomerBookingSource.SERVICE_REQUEST,
            categoryId = category?.id ?: "other-event-services",
            categoryName = category?.title ?: "Other Event Services",
            eventType = eventType,
            eventDate = eventDate,
            startTime = startTime,
            endTime = endTime,
            address = location,
            area = area,
            services = selectedServices.filter { it.isNotBlank() },
            totalAmount = totalAmount,
            quoteBased = quoteBased,
            status = status,
            createdAt = createdAt
        )
    }
}
