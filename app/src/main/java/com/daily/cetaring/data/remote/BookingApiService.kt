package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CreateMyBookingRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestBookingResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BookingApiService {
    @POST("bookings/me")
    suspend fun createBooking(
        @Header("Authorization") authorization: String,
        @Body request: CreateMyBookingRequest
    ): BookingResponse

    @GET("bookings/me")
    suspend fun getMyBookings(
        @Header("Authorization") authorization: String
    ): List<BookingResponse>

    @GET("service-requests/me")
    suspend fun getMyServiceRequests(
        @Header("Authorization") authorization: String
    ): List<ServiceRequestBookingResponse>

    @GET("bookings/{id}/me")
    suspend fun getBooking(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): BookingResponse

    @GET("service-requests/{id}/me")
    suspend fun getServiceRequest(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): ServiceRequestBookingResponse

    @DELETE("bookings/{id}/me")
    suspend fun cancelBooking(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    )
}
