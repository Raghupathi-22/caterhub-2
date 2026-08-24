package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.EventApiService
import com.daily.cetaring.data.remote.dto.CreateEventRequestDto
import com.daily.cetaring.data.remote.dto.EventDashboardDto
import kotlinx.coroutines.flow.first

class EventRepository(
    private val api: EventApiService,
    private val auth: AuthLocalDataSource
) {
    private suspend fun token(): String {
        val value = auth.accessTokenFlow.first()
        if (value.isNullOrBlank()) throw IllegalStateException("Please login again")
        return "Bearer $value"
    }

    suspend fun eventTypes() = api.eventTypes(token())
    suspend fun preview(request: Map<String, Any?>) = api.previewChecklist(token(), request)
    suspend fun create(request: CreateEventRequestDto) = api.createEvent(token(), request)
    suspend fun mine() = api.myEvents(token())
    suspend fun dashboard(id: Long) = api.dashboard(token(), id)
}
