package com.daily.cetaring.booking

import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.presentation.components.EventTypeGridRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTypeSelectionTest {

    @Test
    fun `selector offers every supported event type in order`() {
        val expected = listOf(
            "Birthday", "Wedding", "Engagement", "Reception", "Anniversary", "Baby Shower",
            "Naming Ceremony", "Housewarming", "Pooja / Religious Event", "Corporate Event",
            "School / College Event", "Festival", "Party", "Other"
        )
        assertEquals(expected, EventTypeCatalog.eventTypes.map { it.displayName })
    }

    @Test
    fun `selection stores the unchanged backend value`() {
        val pooja = EventTypeCatalog.eventTypes.first { it.id == "pooja" }
        assertEquals("Religious Ceremony", pooja.backendValue)
        assertEquals("Pooja / Religious Event", EventTypeCatalog.displayNameForBackendValue(pooja.backendValue))
        val ids = EventTypeCatalog.eventTypes.map { it.backendValue }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `grid uses two columns on phones`() {
        assertEquals(2, EventTypeGridRules.columnsFor(320f, 1f))
        assertEquals(2, EventTypeGridRules.columnsFor(412f, 1f))
    }

    @Test
    fun `grid drops to one column on very narrow or heavily scaled layouts`() {
        assertEquals(1, EventTypeGridRules.columnsFor(240f, 1f))
        assertEquals(1, EventTypeGridRules.columnsFor(320f, 1.5f))
    }

    @Test
    fun `grid uses three columns on wide screens`() {
        assertEquals(3, EventTypeGridRules.columnsFor(600f, 1f))
        assertTrue(EventTypeGridRules.columnsFor(600f, 2f) < 3)
    }
}
