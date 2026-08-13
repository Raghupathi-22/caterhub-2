package com.daily.cetaring.data.remote

import retrofit2.http.GET

data class HealthResponse(
    val status: String,
    val database: String,
    val time: String
) {
    val isUp: Boolean
        get() = status.equals("UP", ignoreCase = true) && database.equals("CONNECTED", ignoreCase = true)
}

interface HealthApiService {
    @GET("health")
    suspend fun health(): HealthResponse
}

