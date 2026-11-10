package com.daily.cetaring.domain.rice

import com.daily.cetaring.data.remote.dto.RiceProductResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

data class RiceQuote(
    val quantityKg: Int,
    val subtotal: BigDecimal,
    /** Null when the delivery charge is confirmed by the CaterHub team after ordering. */
    val deliveryCharge: BigDecimal?,
    val total: BigDecimal
)

/**
 * Client-side mirror of the backend RicePricing rules. The backend remains the source of truth;
 * the app uses these rules for instant feedback and sends the displayed total for verification.
 */
object RiceOrderRules {
    val deliverySlots = listOf("MORNING" to "Morning", "AFTERNOON" to "Afternoon", "EVENING" to "Evening", "ANYTIME" to "Any time")

    fun isOrderable(product: RiceProductResponse?): Boolean =
        product != null && product.active == true &&
            (product.basePackKg ?: 0) > 0 && product.basePackPrice != null && (product.minOrderKg ?: 0) > 0

    fun stepKg(product: RiceProductResponse): Int = (product.orderStepKg ?: 1).coerceAtLeast(1)

    fun validateQuantity(product: RiceProductResponse, quantityKg: Int?): String? {
        val min = product.minOrderKg ?: return "Rice ordering is not available right now."
        val max = product.maxOrderKg
        val step = stepKg(product)
        return when {
            quantityKg == null -> "Enter the quantity in kg."
            quantityKg < min -> "Minimum order quantity is $min kg."
            max != null && quantityKg > max -> "Maximum order quantity is $max kg. Please contact us for larger orders."
            (quantityKg - min) % step != 0 -> "Quantity must increase in steps of $step kg above $min kg."
            else -> null
        }
    }

    fun quote(product: RiceProductResponse, quantityKg: Int): RiceQuote? {
        if (!isOrderable(product) || validateQuantity(product, quantityKg) != null) return null
        val packKg = BigDecimal(product.basePackKg!!)
        val subtotal = product.basePackPrice!!
            .multiply(BigDecimal(quantityKg))
            .divide(packKg, 2, RoundingMode.HALF_UP)
        val delivery = product.deliveryCharge?.setScale(2, RoundingMode.HALF_UP)
        return RiceQuote(quantityKg, subtotal, delivery, delivery?.let { subtotal.add(it) } ?: subtotal)
    }

    fun increment(product: RiceProductResponse, quantityKg: Int): Int {
        val next = quantityKg + stepKg(product)
        return product.maxOrderKg?.let { next.coerceAtMost(it) } ?: next
    }

    fun decrement(product: RiceProductResponse, quantityKg: Int): Int =
        (quantityKg - stepKg(product)).coerceAtLeast(product.minOrderKg ?: 1)

    /** Accepts 10-digit Indian mobile numbers with an optional +91 / 91 / 0 prefix. */
    fun normalizeMobile(input: String): String? {
        val digits = input.filter { it.isDigit() }
        val national = when {
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
            else -> digits
        }
        return national.takeIf { it.length == 10 && it.first() in '6'..'9' }
    }

    fun isValidPincode(input: String): Boolean = input.length == 6 && input.all { it.isDigit() }

    fun formatInr(amount: BigDecimal?): String {
        if (amount == null) return "—"
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val digits = if (amount.stripTrailingZeros().scale() <= 0) 0 else 2
        format.maximumFractionDigits = digits
        format.minimumFractionDigits = digits
        return format.format(amount)
    }

    fun formatSampleWeight(grams: Int?): String = when {
        grams == null -> "Sample"
        grams >= 1000 && grams % 1000 == 0 -> "${grams / 1000} kg"
        else -> "$grams g"
    }

    fun displayStatus(status: String?): String =
        status.orEmpty().replace('_', ' ').lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
}
