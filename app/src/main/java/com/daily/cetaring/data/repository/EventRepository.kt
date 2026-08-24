package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.EventApiService
import com.daily.cetaring.data.remote.dto.CreateEventRequest
import com.daily.cetaring.data.remote.dto.EventResponse
import com.daily.cetaring.data.remote.dto.EventWorkspaceResponse
import kotlinx.coroutines.flow.first

class EventRepository(
    private val api: EventApiService,
    private val auth: AuthLocalDataSource
) {
    private suspend fun bearer(): String {
        val token = auth.accessTokenFlow.first()
        if (token.isNullOrBlank()) throw IllegalStateException("Please login again")
        return "Bearer $token"
    }

    suspend fun createEvent(request: CreateEventRequest): EventResponse =
        api.createEvent(bearer(), request)

    suspend fun listMyEvents(): List<EventResponse> =
        api.listMyEvents(bearer())

    suspend fun getWorkspace(id: Long): EventWorkspaceResponse =
        api.getWorkspace(bearer(), id)
}
