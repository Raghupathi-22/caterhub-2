package com.daily.cetaring.features.rice.dto;

import com.daily.cetaring.features.rice.entity.RiceOrder;
import com.daily.cetaring.features.rice.entity.RiceSampleRequest;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RiceDtos {
    private RiceDtos() {}

    private static final String IDEMPOTENCY_PATTERN = "^[A-Za-z0-9_-]{8,64}$";

    @Data @Builder
    public static class ProductResponse {
        public Long id;
        public String code;
        public String name;
        public String tagline;
        public String description;
        public List<String> highlights;
        public String imageUrl;
        public Boolean active;
        public Integer basePackKg;
        public BigDecimal basePackPrice;
        public Integer minOrderKg;
        public Integer orderStepKg;
        public Integer maxOrderKg;
        /** Null when delivery charges are confirmed by the team after ordering. */
        public BigDecimal deliveryCharge;
        public String paymentNote;
        public Boolean sampleEnabled;
        public Integer sampleWeightGrams;
        public BigDecimal samplePrice;
        public String sampleFulfillmentType;
        public String samplePickupLocation;
        public String samplePickupInstructions;
        public LocalDateTime updatedAt;
    }

    @Data @Builder
    public static class QuoteResponse {
        public Integer quantityKg;
        public Integer basePackKg;
        public BigDecimal basePackPrice;
        public BigDecimal subtotal;
        public BigDecimal deliveryCharge;
        public BigDecimal totalAmount;
        public Boolean deliveryChargeToBeConfirmed;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOrderRequest {
        @NotBlank @Size(max = 120) public String customerName;
        @NotBlank @Size(max = 20) public String mobileNumber;
        @NotNull @Min(1) public Integer quantityKg;
        @NotBlank @Size(max = 1000) public String deliveryAddress;
        @NotBlank @Size(max = 100) public String area;
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must be a 6-digit pincode") public String pincode;
        @FutureOrPresent public LocalDate preferredDeliveryDate;
        @Size(max = 30) public String preferredDeliverySlot;
        @Size(max = 1000) public String deliveryNotes;
        /** Total shown to the customer; if it no longer matches the server price the order is rejected. */
        @DecimalMin("0.00") public BigDecimal expectedTotalAmount;
        @NotBlank @Pattern(regexp = IDEMPOTENCY_PATTERN) public String idempotencyKey;
    }

    /** Deliberately has no price, quantity or address: samples are a fixed free pickup. */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateSampleRequest {
        @NotBlank @Size(max = 120) public String customerName;
        @NotBlank @Size(max = 20) public String mobileNumber;
        @NotBlank @Pattern(regexp = IDEMPOTENCY_PATTERN) public String idempotencyKey;
    }

    @Data @Builder
    public static class OrderResponse {
        public Long id;
        public String orderNumber;
        public Long customerUserId;
        public String productName;
        public String customerName;
        public String mobileNumber;
        public Integer quantityKg;
        public Integer basePackKg;
        public BigDecimal basePackPrice;
        public BigDecimal subtotal;
        public BigDecimal deliveryCharge;
        public BigDecimal totalAmount;
        public String deliveryAddress;
        public String area;
        public String pincode;
        public LocalDate preferredDeliveryDate;
        public String preferredDeliverySlot;
        public String deliveryNotes;
        public RiceOrder.Status status;
        public RiceOrder.PaymentStatus paymentStatus;
        public String adminNotes;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    @Data @Builder
    public static class SampleResponse {
        public Long id;
        public String requestNumber;
        public Long customerUserId;
        public String productName;
        public String customerName;
        public String mobileNumber;
        public Integer sampleWeightGrams;
        public BigDecimal price;
        public String fulfillmentType;
        public RiceSampleRequest.Status status;
        public String pickupLocation;
        public LocalDateTime pickupTime;
        public String pickupInstructions;
        public String adminNotes;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateOrderStatusRequest {
        public RiceOrder.Status status;
        public RiceOrder.PaymentStatus paymentStatus;
        @Size(max = 2000) public String adminNotes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateSampleRequest {
        public RiceSampleRequest.Status status;
        @Size(max = 1000) public String pickupLocation;
        public LocalDateTime pickupTime;
        @Size(max = 2000) public String pickupInstructions;
        @Size(max = 2000) public String adminNotes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateProductRequest {
        @NotBlank @Size(max = 120) public String name;
        @Size(max = 255) public String tagline;
        @Size(max = 4000) public String description;
        @Size(max = 10) public List<@NotBlank @Size(max = 200) String> highlights;
        @Size(max = 500) @Pattern(regexp = "^(https://\\S+)?$", message = "must be an https URL") public String imageUrl;
        @NotNull public Boolean active;
        @NotNull @Min(1) @Max(10000) public Integer basePackKg;
        @NotNull @DecimalMin("1.00") @Digits(integer = 10, fraction = 2) public BigDecimal basePackPrice;
        @NotNull @Min(1) @Max(100000) public Integer minOrderKg;
        @NotNull @Min(1) @Max(10000) public Integer orderStepKg;
        @NotNull @Min(1) @Max(100000) public Integer maxOrderKg;
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) public BigDecimal deliveryCharge;
        @Size(max = 2000) public String paymentNote;
        @NotNull public Boolean sampleEnabled;
        @NotNull @Min(1) @Max(5000) public Integer sampleWeightGrams;
        @Size(max = 1000) public String samplePickupLocation;
        @Size(max = 2000) public String samplePickupInstructions;
    }
}
