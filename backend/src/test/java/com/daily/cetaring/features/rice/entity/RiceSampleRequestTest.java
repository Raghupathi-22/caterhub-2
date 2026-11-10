package com.daily.cetaring.features.rice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiceSampleRequestTest {

    @Test
    void lifecycleForcesZeroPriceAndPickupOnly() {
        RiceSampleRequest s = RiceSampleRequest.builder().price(new BigDecimal("99")).fulfillmentType("DELIVERY").build();
        s.create();
        assertEquals(new BigDecimal("0.00"), s.getPrice());
        assertEquals("PICKUP", s.getFulfillmentType());
        assertEquals(RiceSampleRequest.Status.REQUESTED, s.getStatus());

        s.setPrice(new BigDecimal("10"));
        s.setFulfillmentType("HOME_DELIVERY");
        s.update();
        assertEquals(new BigDecimal("0.00"), s.getPrice());
        assertEquals("PICKUP", s.getFulfillmentType());
    }
}
