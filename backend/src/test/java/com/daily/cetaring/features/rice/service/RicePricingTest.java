package com.daily.cetaring.features.rice.service;

import com.daily.cetaring.features.rice.entity.RiceProduct;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RicePricingTest {

    static RiceProduct product() {
        return RiceProduct.builder()
            .id(1L).code(RiceProduct.VILLAGE_FARM_RICE).name("Village Farm Rice").active(true)
            .basePackKg(50).basePackPrice(new BigDecimal("5299.00"))
            .minOrderKg(50).orderStepKg(1).maxOrderKg(5000)
            .sampleEnabled(true).sampleWeightGrams(500)
            .build();
    }

    @Test
    void minimumQuantityCostsBasePackPrice() {
        RicePricing.Quote quote = RicePricing.quote(product(), 50);
        assertEquals(new BigDecimal("5299.00"), quote.subtotal());
        assertEquals(new BigDecimal("5299.00"), quote.total());
        assertNull(quote.deliveryCharge(), "delivery charges are not assumed");
    }

    @Test
    void additionalQuantityIsProRata() {
        assertEquals(new BigDecimal("10598.00"), RicePricing.quote(product(), 100).total());
        assertEquals(new BigDecimal("7948.50"), RicePricing.quote(product(), 75).total());
        // 5299 * 51 / 50 = 5404.98
        assertEquals(new BigDecimal("5404.98"), RicePricing.quote(product(), 51).total());
    }

    @Test
    void configuredDeliveryChargeIsAddedToTotal() {
        RiceProduct p = product();
        p.setDeliveryCharge(new BigDecimal("150"));
        RicePricing.Quote quote = RicePricing.quote(p, 50);
        assertEquals(new BigDecimal("150.00"), quote.deliveryCharge());
        assertEquals(new BigDecimal("5449.00"), quote.total());
    }

    @Test
    void rejectsBelowMinimumAboveMaximumAndOffStep() {
        RiceProduct p = product();
        assertTrue(assertThrows(IllegalArgumentException.class, () -> RicePricing.validateQuantity(p, 49))
            .getMessage().contains("Minimum order quantity is 50 kg"));
        assertThrows(IllegalArgumentException.class, () -> RicePricing.validateQuantity(p, 0));
        assertThrows(IllegalArgumentException.class, () -> RicePricing.validateQuantity(p, null));
        assertThrows(IllegalArgumentException.class, () -> RicePricing.validateQuantity(p, 5001));
        p.setOrderStepKg(25);
        assertDoesNotThrow(() -> RicePricing.validateQuantity(p, 75));
        assertThrows(IllegalArgumentException.class, () -> RicePricing.validateQuantity(p, 60));
    }

    @Test
    void usesBackendConfiguredPrice() {
        RiceProduct p = product();
        p.setBasePackPrice(new BigDecimal("6000.00"));
        assertEquals(new BigDecimal("6000.00"), RicePricing.quote(p, 50).total());
    }
}
