package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** Backend-driven Village Farm Rice configuration. Prices and limits must never be hardcoded in the app. */
data class RiceProductResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("highlights") val highlights: List<String>? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("basePackKg") val basePackKg: Int? = null,
    @SerializedName("basePackPrice") val basePackPrice: BigDecimal? = null,
    @SerializedName("minOrderKg") val minOrderKg: Int? = null,
    @SerializedName("orderStepKg") val orderStepKg: Int? = null,
    @SerializedName("maxOrderKg") val maxOrderKg: Int? = null,
    @SerializedName("deliveryCharge") val deliveryCharge: BigDecimal? = null,
    @SerializedName("paymentNote") val paymentNote: String? = null,
    @SerializedName("sampleEnabled") val sampleEnabled: Boolean? = null,
    @SerializedName("sampleWeightGrams") val sampleWeightGrams: Int? = null,
    @SerializedName("samplePrice") val samplePrice: BigDecimal? = null,
    @SerializedName("sampleFulfillmentType") val sampleFulfillmentType: String? = null,
    @SerializedName("samplePickupLocation") val samplePickupLocation: String? = null,
    @SerializedName("samplePickupInstructions") val samplePickupInstructions: String? = null
)

data class CreateRiceOrderRequest(
    @SerializedName("customerName") val customerName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("quantityKg") val quantityKg: Int,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("area") val area: String,
    @SerializedName("pincode") val pincode: String,
    @SerializedName("preferredDeliveryDate") val preferredDeliveryDate: String?,
    @SerializedName("preferredDeliverySlot") val preferredDeliverySlot: String?,
    @SerializedName("deliveryNotes") val deliveryNotes: String?,
    @SerializedName("expectedTotalAmount") val expectedTotalAmount: BigDecimal,
    @SerializedName("idempotencyKey") val idempotencyKey: String
)

data class CreateRiceSampleRequest(
    @SerializedName("customerName") val customerName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("idempotencyKey") val idempotencyKey: String
)

data class RiceOrderResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("orderNumber") val orderNumber: String? = null,
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("quantityKg") val quantityKg: Int? = null,
    @SerializedName("subtotal") val subtotal: BigDecimal? = null,
    @SerializedName("deliveryCharge") val deliveryCharge: BigDecimal? = null,
    @SerializedName("totalAmount") val totalAmount: BigDecimal? = null,
    @SerializedName("deliveryAddress") val deliveryAddress: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("pincode") val pincode: String? = null,
    @SerializedName("preferredDeliveryDate") val preferredDeliveryDate: String? = null,
    @SerializedName("preferredDeliverySlot") val preferredDeliverySlot: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("paymentStatus") val paymentStatus: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class RiceSampleResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("requestNumber") val requestNumber: String? = null,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("sampleWeightGrams") val sampleWeightGrams: Int? = null,
    @SerializedName("price") val price: BigDecimal? = null,
    @SerializedName("fulfillmentType") val fulfillmentType: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("pickupLocation") val pickupLocation: String? = null,
    @SerializedName("pickupTime") val pickupTime: String? = null,
    @SerializedName("pickupInstructions") val pickupInstructions: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)
