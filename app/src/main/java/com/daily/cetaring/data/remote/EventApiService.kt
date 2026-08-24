package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.CreateEventRequest
import com.daily.cetaring.data.remote.dto.EventResponse
import com.daily.cetaring.data.remote.dto.EventWorkspaceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface EventApiService {
    @POST("events")
    suspend fun createEvent(
        @Header("Authorization") authorization: String,
        @Body request: CreateEventRequest
    ): EventResponse

    @GET("events")
    suspend fun listMyEvents(
        @Header("Authorization") authorization: String
    ): List<EventResponse>

    @GET("events/{id}")
    suspend fun getEvent(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): EventResponse

    @GET("events/{id}/workspace")
    suspend fun getWorkspace(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): EventWorkspaceResponse
}
