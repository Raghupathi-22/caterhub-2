package com.daily.cetaring.presentation.components

import com.daily.cetaring.R
import com.daily.cetaring.domain.catalog.ServiceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComingSoonServicesTest {
    private val halls = ComingSoonServices.FunctionAndBanquetHalls

    @Test
    fun functionHallsPromotionUsesApprovedCopy() {
        assertEquals("Function & Banquet Halls", halls.title)
        assertTrue(halls.description.startsWith("Your celebrations deserve a beautiful venue."))
        assertEquals("Venue booking launching soon on CaterHub.", halls.launchMessage)
        assertEquals(listOf("Weddings", "Receptions", "Birthdays", "Special occasions"), halls.occasions)
    }

    @Test
    fun carouselHasThreeDistinctVenueSlidesWithApprovedCopy() {
        assertTrue(halls.slides.size >= 3)
        assertEquals(halls.slides.size, halls.slides.map { it.imageRes }.toSet().size)
        assertEquals(
            listOf(
                "Function & Banquet Halls" to "Beautiful venues for unforgettable weddings.",
                "Celebrate in Style" to "Discover beautiful spaces for your special occasions.",
                "Your Perfect Celebration Venue" to "Wedding receptions, birthdays and special events."
            ),
            halls.slides.map { it.heading to it.caption }
        )
        assertEquals(
            listOf(
                R.drawable.svc_venue_banquet_hall,
                R.drawable.svc_venue_convention_centre,
                R.drawable.svc_venue_reception_stage
            ),
            halls.slides.map { it.imageRes }
        )
        halls.slides.forEach { assertTrue(it.imageDescription.isNotBlank()) }
    }

    @Test
    fun autoSlideIntervalIsWithinFourToFiveSeconds() {
        assertTrue(COMING_SOON_AUTO_SLIDE_MILLIS in 4_000L..5_000L)
    }

    @Test
    fun promotionCarriesNoPricesRatingsOrAvailability() {
        val copy = listOf(halls.title, halls.description, halls.launchMessage, halls.eyebrow) +
            halls.occasions + halls.slides.flatMap { listOf(it.heading, it.caption, it.imageDescription) }
        val forbidden = Regex("₹|rs\\.?\\s*\\d|\\d+\\s*(guests|seats|stars?)|rating|available now|book now", RegexOption.IGNORE_CASE)
        copy.forEach { assertFalse("Unexpected listing data in: $it", forbidden.containsMatchIn(it)) }
    }

    @Test
    fun upcomingVenueServiceIsNotABookableCatalogueCategory() {
        assertFalse(ServiceCatalog.customerCategories.any { it.id == halls.id })
    }
}