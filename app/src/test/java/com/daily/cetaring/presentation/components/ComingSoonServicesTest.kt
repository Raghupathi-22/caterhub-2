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
    fun functionHallsPromotionUsesApprovedCopyAndVenuePhoto() {
        assertEquals("Function & Banquet Halls", halls.title)
        assertTrue(halls.description.startsWith("Your celebrations deserve a beautiful venue."))
        assertTrue(halls.launchMessage.contains("launching soon"))
        assertEquals(R.drawable.svc_venue_banquet_hall, halls.imageRes)
    }

    @Test
    fun promotionCarriesNoPricesRatingsOrAvailability() {
        val copy = listOf(halls.title, halls.description, halls.launchMessage, halls.eyebrow) + halls.occasions
        val forbidden = Regex("₹|rs\\.?\\s*\\d|\\d+\\s*(guests|seats|stars?)|rating|available now|book now", RegexOption.IGNORE_CASE)
        copy.forEach { assertFalse("Unexpected listing data in: $it", forbidden.containsMatchIn(it)) }
    }

    @Test
    fun upcomingVenueServiceIsNotABookableCatalogueCategory() {
        assertFalse(ServiceCatalog.customerCategories.any { it.id == halls.id })
    }
}
