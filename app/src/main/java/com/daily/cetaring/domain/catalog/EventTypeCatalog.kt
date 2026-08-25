package com.daily.cetaring.domain.catalog

data class EventTypeDefinition(
    val id: String,
    val displayName: String,
    val backendValue: String
)

object EventTypeCatalog {
    val eventTypes = listOf(
        EventTypeDefinition("birthday", "Birthday", "Birthday"),
        EventTypeDefinition("wedding", "Wedding", "Wedding"),
        EventTypeDefinition("engagement", "Engagement", "Engagement"),
        EventTypeDefinition("reception", "Reception", "Reception"),
        EventTypeDefinition("anniversary", "Anniversary", "Anniversary"),
        EventTypeDefinition("baby_shower", "Baby Shower", "Baby Shower"),
        EventTypeDefinition("naming_ceremony", "Naming Ceremony", "Naming Ceremony"),
        EventTypeDefinition("housewarming", "Housewarming", "Housewarming"),
        EventTypeDefinition("pooja", "Pooja / Religious Event", "Religious Ceremony"),
        EventTypeDefinition("corporate", "Corporate Event", "Corporate Event"),
        EventTypeDefinition("school_college", "School / College Event", "School / College Event"),
        EventTypeDefinition("festival", "Festival", "Festival"),
        EventTypeDefinition("party", "Party", "Party"),
        EventTypeDefinition("other", "Other", "Other")
    )

    fun displayNameForBackendValue(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return eventTypes.firstOrNull { it.backendValue == value }?.displayName ?: value
    }
}
