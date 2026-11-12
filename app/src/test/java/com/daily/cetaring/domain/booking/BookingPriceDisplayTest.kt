package com.daily.cetaring.domain.booking

import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BookingPriceDisplayTest {

    private fun rupees(amount: Int) = "₹$amount"

    @Test
    fun fixedPriceServicesShowTotal() {
        assertEquals("₹2500", BookingPriceDisplay.serviceTotal(2500, hasQuote = false, rupees = ::rupees))
        assertEquals("Estimated total", BookingPriceDisplay.serviceTotalLabel(hasQuote = false, fixedTotal = 2500))
    }

    @Test
    fun quoteOnlyServicesNeverShowZero() {
        val total = BookingPriceDisplay.serviceTotal(0, hasQuote = true, rupees = ::rupees)
        assertEquals(BookingPriceDisplay.PRICE_ON_QUOTE, total)
        assertFalse(total.contains("₹0"))
    }

    @Test
    fun mixedServicesShowPublishedPartPlusQuote() {
        assertEquals("₹1200 + quote", BookingPriceDisplay.serviceTotal(1200, hasQuote = true, rupees = ::rupees))
        assertEquals("Total (published prices)", BookingPriceDisplay.serviceTotalLabel(hasQuote = true, fixedTotal = 1200))
    }

    @Test
    fun cateringWithPublishedPackageShowsEstimate() {
        val plan = BookingOptions.cateringPlanOptions.first { it.pricePerPerson != null }
        val draft = BookingDraft(cateringPlan = plan.backendValue, guestCount = 100)
        assertEquals("₹${plan.pricePerPerson!!.toLong() * 100}", BookingPriceDisplay.cateringTotal(draft) { "₹$it" })
    }

    @Test
    fun customMenuIsPriceOnQuote() {
        val draft = BookingDraft(cateringPlan = BookingOptions.planCustomMenu, guestCount = 100, foodRequirements = "Biryani")
        assertEquals(BookingPriceDisplay.PRICE_ON_QUOTE, BookingPriceDisplay.cateringTotal(draft) { "₹$it" })
    }

    @Test
    fun missingGuestsNeverFabricatesTotal() {
        val plan = BookingOptions.cateringPlanOptions.first { it.pricePerPerson != null }
        val draft = BookingDraft(cateringPlan = plan.backendValue, guestCount = null)
        assertEquals(BookingPriceDisplay.PRICE_ON_QUOTE, BookingPriceDisplay.cateringTotal(draft) { "₹$it" })
    }
}
