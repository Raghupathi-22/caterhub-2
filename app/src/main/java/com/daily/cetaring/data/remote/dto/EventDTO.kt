package com.daily.cetaring.data.remote.dto

data class EventTypeGroupDto(
    val group: String,
    val types: List<EventTypeDto> = emptyList()
)

data class EventTypeDto(
    val code: String,
    val displayName: String,
    val group: String
)

data class EventChecklistItemDto(
    val category: String,
    val serviceKey: String,
    val serviceName: String,
    val unit: String,
    val required: Boolean,
    val quantityRule: String,
    val budgetRule: String
)

data class SelectedRequirementDto(
    val serviceKey: String,
    val selected: Boolean = true,
    val quantity: Int? = null,
    val customerBudget: Double? = null,
    val notes: String? = null
)

data class CustomRequirementDto(
    val category: String,
    val serviceName: String,
    val description: String? = null,
    val quantity: Int? = 1,
    val unit: String? = "ITEM",
    val customerBudget: Double? = null,
    val notes: String? = null
)

data class CreateEventRequestDto(
    val eventType: String,
    val eventName: String? = null,
    val eventDate: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String,
    val city: String? = null,
    val area: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val guestCount: Int,
    val estimatedBudget: Double,
    val venueSetting: String? = null,
    val foodPreference: String? = null,
    val foodStyle: String? = null,
    val specialRequirements: String? = null,
    val notes: String? = null,
    val poojaKind: String? = null,
    val ageGroup: String? = null,
    val selectedServices: List<SelectedRequirementDto> = emptyList(),
    val customRequirements: List<CustomRequirementDto> = emptyList()
)

data class EventDashboardDto(
    val event: EventSummaryDto,
    val requirements: List<RequirementDto> = emptyList(),
    val timeline: List<TimelineDto> = emptyList(),
    val budgetWarning: String? = null
) {
    data class EventSummaryDto(
        val id: Long,
        val eventCode: String,
        val eventType: String,
        val eventName: String,
        val eventDate: String,
        val startTime: String?,
        val endTime: String?,
        val location: String,
        val city: String?,
        val guestCount: Int,
        val status: String,
        val estimatedBudget: Double,
        val totalEstimatedCost: Double,
        val totalBookedAmount: Double,
        val remainingBudget: Double,
        val bookedRequired: Int,
        val requiredCount: Int,
        val selectedCount: Int,
        val overBudget: Boolean
    )

    data class RequirementDto(
        val id: Long,
        val category: String,
        val serviceKey: String,
        val serviceName: String,
        val description: String?,
        val quantity: Int,
        val unit: String,
        val required: Boolean,
        val estimatedBudget: Double,
        val customerBudget: Double,
        val actualBookedAmount: Double,
        val status: String,
        val vendorId: Long?,
        val bookingId: Long?,
        val staffingRequestId: Long?,
        val confirmedWorkers: Int?,
        val remainingWorkers: Int?,
        val notes: String?
    )

    data class TimelineDto(
        val id: Long,
        val title: String,
        val detail: String?,
        val occurredAt: String
    )
}
