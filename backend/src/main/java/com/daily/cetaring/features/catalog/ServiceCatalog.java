package com.daily.cetaring.features.catalog;

import lombok.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ServiceCatalog {
    private ServiceCatalog() {
    }

    @Value
    public static class RoleDefinition {
        String id;
        String title;
        String workerType;
        List<String> skills;
    }

    @Value
    public static class CategoryDefinition {
        String id;
        String name;
        String description;
        String serviceType;
        String icon;
        String accent;
        List<String> services;
        List<RoleDefinition> roles;
    }

    private static final List<CategoryDefinition> CATEGORIES = List.of(
        new CategoryDefinition(
            "catering-food",
            "Catering & Food",
            "Meals, snacks, drinks & catering staff",
            "CATERING_FOOD",
            "restaurant",
            "#FF6B35",
            List.of("Meals", "Snacks", "Drinks", "Chef", "Catering Staff", "Helpers", "Bulk Cooking", "Live Counters"),
            List.of(
                new RoleDefinition("chef", "Chef", "CHEF", List.of("Bulk cooking", "South Indian cooking", "North Indian cooking", "Plating")),
                new RoleDefinition("serving-staff", "Serving Staff", "SERVING_STAFF", List.of("Table service", "Guest handling")),
                new RoleDefinition("kitchen-helper", "Kitchen Helper", "KITCHEN_HELPER", List.of("Kitchen support", "Prep assistance"))
            )
        ),
        new CategoryDefinition(
            "decoration",
            "Decoration",
            "Stage, flowers, lighting, tents & seating",
            "DECORATION",
            "celebration",
            "#8E44AD",
            List.of("Stage Decoration", "Flower Decoration", "Balloon Decoration", "Wedding Decoration", "Mandap Decoration", "Lighting", "Tent/Shamiana", "Chairs", "Tables", "Entrance Decoration"),
            List.of(
                new RoleDefinition("event-decorator", "Event Decorator", "EVENT_DECORATOR", List.of("Stage decoration", "Flower decoration", "Balloon decoration")),
                new RoleDefinition("lighting-technician", "Lighting Technician", "LIGHTING_TECHNICIAN", List.of("Lighting setup", "Fixture management")),
                new RoleDefinition("tent-shamiana-worker", "Tent/Shamiana Worker", "TENT_SHAMIANA_WORKER", List.of("Tent setup", "Event structure setup"))
            )
        ),
        new CategoryDefinition(
            "entertainment",
            "Entertainment",
            "DJ, singers, dancers, bands & live shows",
            "ENTERTAINMENT",
            "music_note",
            "#1E88E5",
            List.of("DJ", "Band/Melam", "Singer", "Dancer", "Anchor/Host", "Live Music", "Performers"),
            List.of(
                new RoleDefinition("dj", "DJ", "DJ", List.of("Live DJ", "Sound setup", "Playlist mixing")),
                new RoleDefinition("singer", "Singer", "SINGER", List.of("Live performance", "Stage singing")),
                new RoleDefinition("anchor", "Anchor / Host", "ANCHOR", List.of("Event hosting", "Crowd engagement"))
            )
        ),
        new CategoryDefinition(
            "beauty",
            "Beauty",
            "Makeup, mehndi, hairstyling & beauty services",
            "BEAUTY",
            "spa",
            "#E91E63",
            List.of("Bridal Makeup", "Party Makeup", "Mehndi", "Bridal Mehndi", "Arabic Mehndi", "Hairstyling", "Saree Draping"),
            List.of(
                new RoleDefinition("makeup-artist", "Makeup Artist", "MAKEUP_ARTIST", List.of("Bridal makeup", "Party makeup", "Hairstyling")),
                new RoleDefinition("mehendi-artist", "Mehndi Artist", "MEHENDI_ARTIST", List.of("Bridal mehndi", "Arabic mehndi", "Traditional mehndi")),
                new RoleDefinition("saree-drapist", "Saree Drapist", "SAREE_DRAPIST", List.of("Saree draping", "Styling support"))
            )
        ),
        new CategoryDefinition(
            "photography-video",
            "Photography & Video",
            "Photography, videography & event coverage",
            "PHOTOGRAPHY_VIDEO",
            "photo_camera",
            "#00ACC1",
            List.of("Photography", "Videography", "Cinematography", "Drone Photography", "Pre-Wedding Photography", "Live Streaming"),
            List.of(
                new RoleDefinition("photographer", "Photographer", "PHOTOGRAPHER", List.of("Wedding photography", "Candid photography")),
                new RoleDefinition("videographer", "Videographer", "VIDEOGRAPHER", List.of("Event videography", "Cinematic coverage")),
                new RoleDefinition("drone-operator", "Drone Operator", "DRONE_OPERATOR", List.of("Drone coverage", "Aerial video"))
            )
        ),
        new CategoryDefinition(
            "religious-ceremony",
            "Religious & Ceremony",
            "Pujari, rituals & ceremony support",
            "RELIGIOUS_CEREMONY",
            "temple_hindu",
            "#6D4C41",
            List.of("Pujari", "Pooja Services", "Ceremony Support"),
            List.of(
                new RoleDefinition("pujari", "Pujari", "PUJARI", List.of("Pooja services", "Ritual assistance")),
                new RoleDefinition("pandit", "Pandit", "PANDIT", List.of("Ceremony support", "Religious guidance"))
            )
        ),
        new CategoryDefinition(
            "event-support",
            "Event Support",
            "Event coordinators, hosts & support staff",
            "EVENT_SUPPORT",
            "groups",
            "#43A047",
            List.of("Event Coordinator", "Event Host", "Guest Management", "Helpers", "Cleaning Staff", "Event Support Staff"),
            List.of(
                new RoleDefinition("event-coordinator", "Event Coordinator", "EVENT_COORDINATOR", List.of("Guest management", "Execution support")),
                new RoleDefinition("host", "Event Host", "HOST", List.of("Hosting", "Stage flow")),
                new RoleDefinition("cleaning-staff", "Cleaning Staff", "CLEANING_STAFF", List.of("Cleanup", "Venue hygiene"))
            )
        ),
        new CategoryDefinition(
            "rentals",
            "Rentals",
            "Chairs, tables, furniture & event equipment",
            "RENTALS",
            "chair",
            "#FB8C00",
            List.of("Chairs", "Tables", "Stage", "Lighting", "Sound Equipment", "Furniture", "Tent"),
            List.of(
                new RoleDefinition("chair-rental", "Chair Rental", "CHAIR_RENTAL", List.of("Chair supply", "Seating setup")),
                new RoleDefinition("table-rental", "Table Rental", "TABLE_RENTAL", List.of("Table supply", "Event setup")),
                new RoleDefinition("tent-rental", "Tent Rental", "TENT_RENTAL", List.of("Tent setup", "Coverage setup"))
            )
        ),
        new CategoryDefinition(
            "transport-logistics",
            "Transport & Logistics",
            "Event transport, delivery & logistics",
            "TRANSPORT_LOGISTICS",
            "local_shipping",
            "#546E7A",
            List.of("Event Transport", "Driver", "Delivery", "Logistics"),
            List.of(
                new RoleDefinition("event-driver", "Event Driver", "EVENT_DRIVER", List.of("Event transport", "On-time routing")),
                new RoleDefinition("delivery-support", "Delivery Support", "GOODS_TRANSPORT_DRIVER", List.of("Vendor delivery", "Equipment movement"))
            )
        ),
        new CategoryDefinition(
            "other-event-services",
            "Other Event Services",
            "Additional services for your event",
            "OTHER_EVENT_SERVICES",
            "miscellaneous_services",
            "#5E35B1",
            List.of("Custom Event Services"),
            List.of(
                new RoleDefinition("custom-event-professional", "Custom Event Professional", "CUSTOM_EVENT_PROFESSIONAL", List.of("Custom requirements", "Specialized support"))
            )
        )
    );

    private static final Set<String> CATEGORY_IDS = CATEGORIES.stream()
        .map(CategoryDefinition::getServiceType)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private static final Map<String, CategoryDefinition> BY_ID = buildById();
    private static final Map<String, CategoryDefinition> BY_SERVICE_TYPE = buildByServiceType();

    private static Map<String, CategoryDefinition> buildById() {
        Map<String, CategoryDefinition> map = new LinkedHashMap<>();
        for (CategoryDefinition category : CATEGORIES) {
            map.put(category.getId(), category);
        }
        return Map.copyOf(map);
    }

    private static Map<String, CategoryDefinition> buildByServiceType() {
        Map<String, CategoryDefinition> map = new LinkedHashMap<>();
        for (CategoryDefinition category : CATEGORIES) {
            map.put(category.getServiceType(), category);
        }
        return Map.copyOf(map);
    }

    public static boolean isSupportedServiceType(String serviceType) {
        if (serviceType == null) {
            return false;
        }
        return CATEGORY_IDS.contains(serviceType.trim().toUpperCase(Locale.ROOT));
    }

    public static List<CategoryDefinition> categories() {
        return CATEGORIES;
    }

    public static CategoryDefinition categoryById(String id) {
        if (id == null) {
            return null;
        }
        return BY_ID.get(id.trim().toLowerCase(Locale.ROOT));
    }

    public static CategoryDefinition categoryByServiceType(String serviceType) {
        if (serviceType == null) {
            return null;
        }
        return BY_SERVICE_TYPE.get(serviceType.trim().toUpperCase(Locale.ROOT));
    }
}
