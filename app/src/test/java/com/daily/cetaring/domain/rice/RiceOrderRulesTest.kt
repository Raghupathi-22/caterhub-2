package com.daily.cetaring.domain.rice

import com.daily.cetaring.data.remote.dto.RiceProductResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RiceOrderRulesTest {
    private val product = RiceProductResponse(
        active = true,
        basePackKg = 50,
        basePackPrice = BigDecimal("5299.00"),
        minOrderKg = 50,
        orderStepKg = 1,
        maxOrderKg = 5000,
        sampleEnabled = true,
        sampleWeightGrams = 500,
        samplePrice = BigDecimal.ZERO
    )

    @Test
    fun minimumQuantityPricesAtBasePack() {
        val quote = RiceOrderRules.quote(product, 50)!!
        assertEquals(BigDecimal("5299.00"), quote.subtotal)
        assertEquals(BigDecimal("5299.00"), quote.total)
        assertNull(quote.deliveryCharge)
    }

    @Test
    fun additionalQuantitiesArePricedProportionally() {
        assertEquals(BigDecimal("7948.50"), RiceOrderRules.quote(product, 75)!!.total)
        assertEquals(BigDecimal("10598.00"), RiceOrderRules.quote(product, 100)!!.total)
        assertEquals(BigDecimal("5404.98"), RiceOrderRules.quote(product, 51)!!.total)
    }

    @Test
    fun configuredDeliveryChargeIsAddedToTotal() {
        val quote = RiceOrderRules.quote(product.copy(deliveryCharge = BigDecimal("150")), 50)!!
        assertEquals(BigDecimal("150.00"), quote.deliveryCharge)
        assertEquals(BigDecimal("5449.00"), quote.total)
    }

    @Test
    fun quantityBelowMinimumIsRejected() {
        assertEquals("Minimum order quantity is 50 kg.", RiceOrderRules.validateQuantity(product, 49))
        assertNull(RiceOrderRules.quote(product, 49))
    }

    @Test
    fun quantityAboveMaximumAndOffStepAreRejected() {
        assertTrue(RiceOrderRules.validateQuantity(product, 5001)!!.startsWith("Maximum order quantity"))
        val stepped = product.copy(orderStepKg = 25)
        assertTrue(RiceOrderRules.validateQuantity(stepped, 60) != null)
        assertNull(RiceOrderRules.validateQuantity(stepped, 75))
    }

    @Test
    fun stepperRespectsMinimumAndMaximum() {
        val stepped = product.copy(orderStepKg = 25, maxOrderKg = 100)
        assertEquals(50, RiceOrderRules.decrement(stepped, 50))
        assertEquals(75, RiceOrderRules.increment(stepped, 50))
        assertEquals(100, RiceOrderRules.increment(stepped, 100))
    }

    @Test
    fun inactiveProductIsNotOrderable() {
        assertFalse(RiceOrderRules.isOrderable(product.copy(active = false)))
        assertNull(RiceOrderRules.quote(product.copy(active = false), 50))
    }

    @Test
    fun mobileNumbersAreNormalized() {
        assertEquals("9876543210", RiceOrderRules.normalizeMobile("+91 98765 43210"))
        assertEquals("9876543210", RiceOrderRules.normalizeMobile("09876543210"))
        assertNull(RiceOrderRules.normalizeMobile("12345"))
        assertNull(RiceOrderRules.normalizeMobile("1234567890"))
    }

    @Test
    fun pincodeAndFormatting() {
        assertTrue(RiceOrderRules.isValidPincode("600040"))
        assertFalse(RiceOrderRules.isValidPincode("60004"))
        assertTrue(RiceOrderRules.formatInr(BigDecimal("5299.00")).contains("5,299"))
        assertTrue(RiceOrderRules.formatInr(BigDecimal("7948.50")).contains("7,948.50"))
    }
}
