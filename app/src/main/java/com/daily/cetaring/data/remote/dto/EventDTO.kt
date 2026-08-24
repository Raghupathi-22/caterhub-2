package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateEventRequest(
    val name: String,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("eventDate") val eventDate: String,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    val location: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val guestCount: Int?,
    val budget: BigDecimal?
)

data class EventResponse(
    val id: Long,
    val eventCode: String,
    val name: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String?,
    val endTime: String?,
    val location: String?,
    val guestCount: Int?,
    val budget: BigDecimal?
)

data class EventRequirementDto(
    val id: Long,
    val category: String,
    val plannedAmount: BigDecimal?,
    val bookedAmount: BigDecimal?,
    val requiredFlag: Boolean
)

data class EventWorkspaceResponse(
    val id: Long,
    val eventCode: String,
    val name: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String?,
    val endTime: String?,
    val location: String?,
    val guestCount: Int?,
    val budget: BigDecimal?,
    val totalPlanned: BigDecimal,
    val totalBooked: BigDecimal,
    val checklist: List<EventRequirementDto>
)
