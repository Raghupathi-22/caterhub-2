package com.daily.cetaring.features.rice.service;

import com.daily.cetaring.features.rice.entity.RiceProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Pure pricing and quantity rules for bulk rice, driven entirely by {@link RiceProduct} configuration. */
public final class RicePricing {
    private RicePricing() {}

    public record Quote(int quantityKg, BigDecimal subtotal, BigDecimal deliveryCharge, BigDecimal total) {}

    public static void validateQuantity(RiceProduct product, Integer quantityKg) {
        if (quantityKg == null) {
            throw new IllegalArgumentException("Please enter the rice quantity in kg.");
        }
        int min = product.getMinOrderKg();
        int max = product.getMaxOrderKg();
        int step = Math.max(1, product.getOrderStepKg());
        if (quantityKg < min) {
            throw new IllegalArgumentException("Minimum order quantity is " + min + " kg.");
        }
        if (quantityKg > max) {
            throw new IllegalArgumentException("Maximum order quantity is " + max + " kg. Please contact us for larger orders.");
        }
        if ((quantityKg - min) % step != 0) {
            throw new IllegalArgumentException("Quantity must increase in steps of " + step + " kg above " + min + " kg.");
        }
    }

    /** Price is pro-rata to the base pack: e.g. ₹5,299 per 50 kg → 75 kg costs ₹7,948.50. */
    public static Quote quote(RiceProduct product, int quantityKg) {
        validateQuantity(product, quantityKg);
        BigDecimal subtotal = product.getBasePackPrice()
            .multiply(BigDecimal.valueOf(quantityKg))
            .divide(BigDecimal.valueOf(product.getBasePackKg()), 2, RoundingMode.HALF_UP);
        BigDecimal delivery = product.getDeliveryCharge() == null
            ? null
            : product.getDeliveryCharge().setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = delivery == null ? subtotal : subtotal.add(delivery);
        return new Quote(quantityKg, subtotal, delivery, total);
    }
}
