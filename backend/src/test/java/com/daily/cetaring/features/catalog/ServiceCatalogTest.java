package com.daily.cetaring.features.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCatalogTest {
    @Test
    void cateringStaffCategoryIsSupported() {
        assertTrue(ServiceCatalog.isSupportedServiceType("CATERING_STAFF"));
        assertNotNull(ServiceCatalog.categoryById("catering-staff"));
        assertEquals("CATERING_STAFF", ServiceCatalog.categoryById("catering-staff").getServiceType());
    }
}
