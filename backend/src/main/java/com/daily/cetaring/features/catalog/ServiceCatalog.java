package com.daily.cetaring.features.catalog;

import java.util.Set;

public final class ServiceCatalog {
    private ServiceCatalog() {
    }

    private static final Set<String> CATEGORY_IDS = Set.of(
        "CATERING_FOOD",
        "DECORATION",
        "ENTERTAINMENT",
        "BEAUTY",
        "PHOTOGRAPHY_VIDEO",
        "RELIGIOUS_CEREMONY",
        "EVENT_SUPPORT",
        "RENTALS",
        "TRANSPORT_LOGISTICS",
        "OTHER_EVENT_SERVICES"
    );

    public static boolean isSupportedServiceType(String serviceType) {
        if (serviceType == null) {
            return false;
        }
        return CATEGORY_IDS.contains(serviceType.trim().toUpperCase());
    }
}

