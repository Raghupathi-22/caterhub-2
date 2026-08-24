package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.EventDashboardDto
import com.daily.cetaring.data.remote.dto.EventTypeGroupDto
import com.daily.cetaring.data.remote.dto.EventChecklistItemDto
import com.daily.cetaring.data.remote.dto.CreateEventRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Path

interface EventApiService {
    @GET("events/types")
    suspend fun eventTypes(@Header("Authorization") authorization: String): List<EventTypeGroupDto>

    @POST("events/checklist/preview")
    suspend fun previewChecklist(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, Any?>
    ): List<EventChecklistItemDto>

    @POST("events")
    suspend fun createEvent(
        @Header("Authorization") authorization: String,
        @Body request: CreateEventRequestDto
    ): EventDashboardDto

    @GET("events/mine")
    suspend fun myEvents(@Header("Authorization") authorization: String): List<EventDashboardDto.EventSummaryDto>

    @GET("events/{eventId}")
    suspend fun dashboard(
        @Header("Authorization") authorization: String,
        @Path("eventId") eventId: Long
    ): EventDashboardDto
}
