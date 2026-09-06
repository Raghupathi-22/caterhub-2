package com.daily.cetaring.domain.catalog

import com.daily.cetaring.data.remote.dto.WorkerType

data class ServiceCategoryDefinition(
    val id: String,
    val title: String,
    val subtitle: String,
    val serviceType: String,
    val visualTone: CategoryVisualTone
)

enum class CategoryVisualTone {
    FOOD,
    DECORATION,
    ENTERTAINMENT,
    BEAUTY,
    PHOTOGRAPHY,
    RELIGIOUS,
    SUPPORT,
    RENTALS,
    TRANSPORT,
    OTHER
}

data class ServiceRoleDefinition(
    val id: String,
    val categoryId: String,
    val title: String,
    val workerType: WorkerType? = null,
    val defaultUnitPrice: Int? = null,
    val skillSuggestions: List<String> = emptyList()
) {
    val quoteOnly: Boolean get() = defaultUnitPrice == null
}

object ServiceCatalog {
    private val customerOnlyCategories = listOf(
        ServiceCategoryDefinition(
            "tent-tables-equipment",
            "Tent, Tables & Equipment",
            "Tents, tables, chairs, serving equipment and essential event rentals.",
            "RENTALS",
            CategoryVisualTone.RENTALS
        )
    )

    val categories = listOf(
        ServiceCategoryDefinition(
            "catering-food",
            "Catering & Food",
            "Meals, snacks, drinks & catering staff",
            "CATERING_FOOD",
            CategoryVisualTone.FOOD
        ),
        ServiceCategoryDefinition(
            "decoration",
            "Decoration",
            "Stage, mandap, entrance and backdrop decoration",
            "DECORATION",
            CategoryVisualTone.DECORATION
        ),
        ServiceCategoryDefinition(
            "entertainment",
            "Entertainment",
            "DJ, singers, dancers, bands & live shows",
            "ENTERTAINMENT",
            CategoryVisualTone.ENTERTAINMENT
        ),
        ServiceCategoryDefinition(
            "beauty",
            "Beauty",
            "Makeup, mehendi, hairstyling & beauty services",
            "BEAUTY",
            CategoryVisualTone.BEAUTY
        ),
        ServiceCategoryDefinition(
            "photography-video",
            "Photography & Video",
            "Photography, videography & live event coverage",
            "PHOTOGRAPHY_VIDEO",
            CategoryVisualTone.PHOTOGRAPHY
        ),
        ServiceCategoryDefinition(
            "religious-ceremony",
            "Religious & Ceremony",
            "Pujari, rituals & ceremony support",
            "RELIGIOUS_CEREMONY",
            CategoryVisualTone.RELIGIOUS
        ),
        ServiceCategoryDefinition(
            "event-support",
            "Event Support",
            "Event coordinators, hosts & support staff",
            "EVENT_SUPPORT",
            CategoryVisualTone.SUPPORT
        ),
        ServiceCategoryDefinition(
            "rentals",
            "Rentals",
            "Chairs, tables, furniture & event equipment",
            "RENTALS",
            CategoryVisualTone.RENTALS
        ),
        ServiceCategoryDefinition(
            "transport-logistics",
            "Transport & Logistics",
            "Event transport, delivery & logistics",
            "TRANSPORT_LOGISTICS",
            CategoryVisualTone.TRANSPORT
        ),
        ServiceCategoryDefinition(
            "other-event-services",
            "Other Event Services",
            "Additional services for your event",
            "OTHER_EVENT_SERVICES",
            CategoryVisualTone.OTHER
        )
    )

    val roles = listOf(
        ServiceRoleDefinition("catering", "catering-food", "Catering", defaultUnitPrice = 699),
        ServiceRoleDefinition(
            "chef",
            "catering-food",
            "Chef",
            WorkerType.CHEF,
            2200,
            skillSuggestions = listOf("Bulk cooking", "South Indian cooking", "North Indian cooking", "Plating")
        ),
        ServiceRoleDefinition("head-chef", "catering-food", "Head Chef", WorkerType.HEAD_CHEF, 3000),
        ServiceRoleDefinition("assistant-chef", "catering-food", "Assistant Chef", WorkerType.ASSISTANT_CHEF, 1800),
        ServiceRoleDefinition("cook", "catering-food", "Cook", WorkerType.COOK, 1800),
        ServiceRoleDefinition("biryani-chef", "catering-food", "Biryani Chef", WorkerType.BIRYANI_CHEF, 2600),
        ServiceRoleDefinition("tandoor-chef", "catering-food", "Tandoor Chef", WorkerType.TANDOOR_CHEF, 2600),
        ServiceRoleDefinition("sweet-mithai-chef", "catering-food", "Sweet/Mithai Chef", WorkerType.SWEET_MITHAI_CHEF, 2500),
        ServiceRoleDefinition("kitchen-helper", "catering-food", "Kitchen Helper", WorkerType.KITCHEN_HELPER, 700),
        ServiceRoleDefinition("serving-staff", "catering-food", "Serving Staff", WorkerType.SERVING_STAFF, 900),
        ServiceRoleDefinition("waiter", "catering-food", "Waiter", WorkerType.WAITER, 950),
        ServiceRoleDefinition("catering-boy", "catering-food", "Catering Boy", WorkerType.CATERING_BOY, 850),
        ServiceRoleDefinition("catering-girl", "catering-food", "Catering Girl", WorkerType.CATERING_GIRL, 900),
        ServiceRoleDefinition("cleaner", "catering-food", "Cleaner", WorkerType.CLEANER, 800),
        ServiceRoleDefinition("catering-supervisor", "catering-food", "Catering Supervisor", WorkerType.CATERING_SUPERVISOR, 1800),

        ServiceRoleDefinition(
            "event-decorator",
            "decoration",
            "Event Decorator",
            WorkerType.EVENT_DECORATOR,
            skillSuggestions = listOf("Stage decoration", "Flower decoration", "Balloon decoration", "Lighting")
        ),
        ServiceRoleDefinition("wedding-decorator", "decoration", "Wedding Decorator", WorkerType.WEDDING_DECORATOR),
        ServiceRoleDefinition("stage-decorator", "decoration", "Stage Decorator", WorkerType.STAGE_DECORATOR),
        ServiceRoleDefinition("flower-decorator", "decoration", "Flower Decorator", WorkerType.FLOWER_DECORATOR),
        ServiceRoleDefinition("balloon-decorator", "decoration", "Balloon Decorator", WorkerType.BALLOON_DECORATOR),
        ServiceRoleDefinition("lighting-technician", "decoration", "Lighting Technician", WorkerType.LIGHTING_TECHNICIAN),
        ServiceRoleDefinition("tent-shamiana-worker", "decoration", "Tent/Shamiana Worker", WorkerType.TENT_SHAMIANA_WORKER),
        ServiceRoleDefinition("stage-setup-worker", "decoration", "Stage Setup Worker", WorkerType.STAGE_SETUP_WORKER),
        ServiceRoleDefinition("mandap-decorator", "decoration", "Mandap Decorator", WorkerType.MANDAP_DECORATOR),
        ServiceRoleDefinition("entrance-decorator", "decoration", "Entrance Decorator", WorkerType.ENTRANCE_DECORATOR),
        ServiceRoleDefinition("backdrop-decorator", "decoration", "Backdrop Decorator", WorkerType.BACKDROP_DECORATOR),
        ServiceRoleDefinition("decoration-supervisor", "decoration", "Decoration Supervisor", WorkerType.DECORATION_SUPERVISOR),
        ServiceRoleDefinition(
            "chair-rental",
            "rentals",
            "Chair Rental",
            WorkerType.CHAIR_RENTAL,
            50,
            skillSuggestions = listOf("Chairs", "Tables", "Event furniture setup")
        ),
        ServiceRoleDefinition("table-rental", "rentals", "Table Rental", WorkerType.TABLE_RENTAL, 60),

        ServiceRoleDefinition(
            "dj",
            "entertainment",
            "DJ",
            WorkerType.DJ,
            skillSuggestions = listOf("Live DJ", "Sound setup", "Playlist mixing", "Crowd engagement")
        ),
        ServiceRoleDefinition("sound-technician", "entertainment", "Sound Technician", WorkerType.SOUND_TECHNICIAN),
        ServiceRoleDefinition(
            "singer",
            "entertainment",
            "Singer",
            WorkerType.SINGER,
            skillSuggestions = listOf("Live performance", "Event singing", "Stage performance")
        ),
        ServiceRoleDefinition("male-singer", "entertainment", "Male Singer", WorkerType.MALE_SINGER),
        ServiceRoleDefinition("female-singer", "entertainment", "Female Singer", WorkerType.FEMALE_SINGER),
        ServiceRoleDefinition("band-member", "entertainment", "Band Member", WorkerType.BAND_MEMBER),
        ServiceRoleDefinition("band-leader", "entertainment", "Band Leader", WorkerType.BAND_LEADER),
        ServiceRoleDefinition("melam-artist", "entertainment", "Melam Artist", WorkerType.MELAM_ARTIST),
        ServiceRoleDefinition("band-melam-artist", "entertainment", "Band Melam Artist", WorkerType.BAND_MELAM_ARTIST),
        ServiceRoleDefinition("dancer", "entertainment", "Dancer", WorkerType.DANCER),
        ServiceRoleDefinition("dance-performer", "entertainment", "Dance Performer", WorkerType.DANCE_PERFORMER),
        ServiceRoleDefinition("dance-troupe", "entertainment", "Dance Troupe", WorkerType.DANCE_TROUPE),
        ServiceRoleDefinition("anchor", "entertainment", "Anchor", WorkerType.ANCHOR),
        ServiceRoleDefinition("mc", "entertainment", "MC", WorkerType.MC),
        ServiceRoleDefinition("magician", "entertainment", "Magician", WorkerType.MAGICIAN),
        ServiceRoleDefinition("folk-artist", "entertainment", "Folk Artist", WorkerType.FOLK_ARTIST),
        ServiceRoleDefinition("cultural-performer", "entertainment", "Cultural Performer", WorkerType.CULTURAL_PERFORMER),

        ServiceRoleDefinition(
            "makeup-artist",
            "beauty",
            "Makeup Artist",
            WorkerType.MAKEUP_ARTIST,
            skillSuggestions = listOf("Party makeup", "Bridal makeup", "Hairstyling", "Facial/beauty services")
        ),
        ServiceRoleDefinition("bridal-makeup-artist", "beauty", "Bridal Makeup Artist", WorkerType.BRIDAL_MAKEUP_ARTIST),
        ServiceRoleDefinition("groom-makeup-artist", "beauty", "Groom Makeup Artist", WorkerType.GROOM_MAKEUP_ARTIST),
        ServiceRoleDefinition("hair-stylist", "beauty", "Hair Stylist", WorkerType.HAIR_STYLIST),
        ServiceRoleDefinition(
            "mehendi-artist",
            "beauty",
            "Mehndi Artist",
            WorkerType.MEHENDI_ARTIST,
            skillSuggestions = listOf("Bridal mehndi", "Arabic mehndi", "Party mehndi", "Traditional mehndi")
        ),
        ServiceRoleDefinition("saree-drapist", "beauty", "Saree Drapist", WorkerType.SAREE_DRAPIST),
        ServiceRoleDefinition("beauty-specialist", "beauty", "Beauty Specialist", WorkerType.BEAUTY_SPECIALIST),

        ServiceRoleDefinition(
            "photographer",
            "photography-video",
            "Photographer",
            WorkerType.PHOTOGRAPHER,
            skillSuggestions = listOf("Wedding photography", "Event photography", "Candid photography")
        ),
        ServiceRoleDefinition("wedding-photographer", "photography-video", "Wedding Photographer", WorkerType.WEDDING_PHOTOGRAPHER),
        ServiceRoleDefinition("event-photographer", "photography-video", "Event Photographer", WorkerType.EVENT_PHOTOGRAPHER),
        ServiceRoleDefinition("videographer", "photography-video", "Videographer", WorkerType.VIDEOGRAPHER),
        ServiceRoleDefinition("wedding-videographer", "photography-video", "Wedding Videographer", WorkerType.WEDDING_VIDEOGRAPHER),
        ServiceRoleDefinition("drone-operator", "photography-video", "Drone Operator", WorkerType.DRONE_OPERATOR),
        ServiceRoleDefinition("photo-editor", "photography-video", "Photo Editor", WorkerType.PHOTO_EDITOR),
        ServiceRoleDefinition("video-editor", "photography-video", "Video Editor", WorkerType.VIDEO_EDITOR),
        ServiceRoleDefinition("photo-booth-operator", "photography-video", "Photo Booth Operator", WorkerType.PHOTO_BOOTH_OPERATOR),
        ServiceRoleDefinition("live-streaming-operator", "photography-video", "Live Streaming Operator", WorkerType.LIVE_STREAMING_OPERATOR),

        ServiceRoleDefinition(
            "pujari",
            "religious-ceremony",
            "Pujari",
            WorkerType.PUJARI,
            skillSuggestions = listOf("Pooja services", "Ritual assistance", "Ceremony support")
        ),
        ServiceRoleDefinition("priest", "religious-ceremony", "Priest", WorkerType.PRIEST),
        ServiceRoleDefinition("pandit", "religious-ceremony", "Pandit", WorkerType.PANDIT),
        ServiceRoleDefinition("pooja-specialist", "religious-ceremony", "Pooja Specialist", WorkerType.POOJA_SPECIALIST),
        ServiceRoleDefinition("homam-specialist", "religious-ceremony", "Homam Specialist", WorkerType.HOMAM_SPECIALIST),
        ServiceRoleDefinition("wedding-ritual-specialist", "religious-ceremony", "Wedding Ritual Specialist", WorkerType.WEDDING_RITUAL_SPECIALIST),

        ServiceRoleDefinition("event-manager", "event-support", "Event Manager", WorkerType.EVENT_MANAGER),
        ServiceRoleDefinition("event-supervisor", "event-support", "Event Supervisor", WorkerType.EVENT_SUPERVISOR),
        ServiceRoleDefinition(
            "event-coordinator",
            "event-support",
            "Event Coordinator",
            WorkerType.EVENT_COORDINATOR,
            skillSuggestions = listOf("Guest management", "Event coordination", "Support staff management")
        ),
        ServiceRoleDefinition("host", "event-support", "Host", WorkerType.HOST),
        ServiceRoleDefinition("registration-staff", "event-support", "Registration Staff", WorkerType.REGISTRATION_STAFF),
        ServiceRoleDefinition("usher", "event-support", "Usher", WorkerType.USHER),
        ServiceRoleDefinition("security-staff", "event-support", "Security Staff", WorkerType.SECURITY_STAFF),
        ServiceRoleDefinition("parking-staff", "event-support", "Parking Staff", WorkerType.PARKING_STAFF),
        ServiceRoleDefinition("general-helper", "event-support", "General Helper", WorkerType.GENERAL_HELPER),
        ServiceRoleDefinition("cleaning-staff", "event-support", "Cleaning Staff", WorkerType.CLEANING_STAFF),

        ServiceRoleDefinition("sofa-rental", "rentals", "Sofa Rental", WorkerType.SOFA_RENTAL),
        ServiceRoleDefinition("crockery-rental", "rentals", "Crockery Rental", WorkerType.CROCKERY_RENTAL),
        ServiceRoleDefinition("dining-equipment", "rentals", "Dining Equipment", WorkerType.DINING_EQUIPMENT),
        ServiceRoleDefinition("cooking-equipment", "rentals", "Cooking Equipment", WorkerType.COOKING_EQUIPMENT),
        ServiceRoleDefinition("generator", "rentals", "Generator", WorkerType.GENERATOR_OPERATOR),
        ServiceRoleDefinition("fan-cooler-rental", "rentals", "Fan/Cooler Rental", WorkerType.FAN_COOLER_RENTAL),
        ServiceRoleDefinition("tent-rental", "rentals", "Tent Rental", WorkerType.TENT_RENTAL),
        ServiceRoleDefinition("stage-equipment", "rentals", "Stage Equipment", WorkerType.STAGE_EQUIPMENT),

        ServiceRoleDefinition(
            "event-driver",
            "transport-logistics",
            "Event Driver",
            WorkerType.EVENT_DRIVER,
            skillSuggestions = listOf("Event transport", "Delivery", "Driver support", "Logistics support")
        ),
        ServiceRoleDefinition("guest-transport-driver", "transport-logistics", "Guest Transport Driver", WorkerType.GUEST_TRANSPORT_DRIVER),
        ServiceRoleDefinition("goods-transport-driver", "transport-logistics", "Goods Transport Driver", WorkerType.GOODS_TRANSPORT_DRIVER),
        ServiceRoleDefinition("loading-unloading-staff", "transport-logistics", "Loading/Unloading Staff", WorkerType.LOADING_UNLOADING_STAFF),

        ServiceRoleDefinition(
            "invitation-designer",
            "other-event-services",
            "Invitation Designer",
            WorkerType.INVITATION_DESIGNER,
            skillSuggestions = listOf("Custom invitation design", "Event branding", "Creative event support")
        ),
        ServiceRoleDefinition("cake-specialist", "other-event-services", "Cake Specialist", WorkerType.CAKE_SPECIALIST),
        ServiceRoleDefinition("return-gift-specialist", "other-event-services", "Return Gift Specialist", WorkerType.RETURN_GIFT_SPECIALIST),
        ServiceRoleDefinition("custom-event-professional", "other-event-services", "Custom Event Professional", WorkerType.CUSTOM_EVENT_PROFESSIONAL)
    )

    val customerCategories: List<ServiceCategoryDefinition> = listOfNotNull(
        category("catering-food"),
        category("decoration"),
        category("tent-tables-equipment"),
        category("entertainment"),
        category("photography-video"),
        category("beauty"),
        category("religious-ceremony")
    )

    fun category(id: String): ServiceCategoryDefinition? =
        categories.firstOrNull { it.id == id } ?: customerOnlyCategories.firstOrNull { it.id == id }

    fun rolesForCategory(categoryId: String): List<ServiceRoleDefinition> =
        roles.filter { it.categoryId == categoryId }

    fun customerRolesForCategory(categoryId: String): List<ServiceRoleDefinition> = when (categoryId) {
        "decoration" -> roles.filter {
            it.id in setOf(
                "stage-setup-worker",
                "mandap-decorator",
                "entrance-decorator",
                "backdrop-decorator",
                "decoration-supervisor"
            )
        }

        "photography-video" -> listOfNotNull(
            roles.firstOrNull { it.id == "photographer" }?.copy(
                title = "Photography",
                defaultUnitPrice = null
            ),
            roles.firstOrNull { it.id == "videographer" }?.copy(
                title = "Videography",
                defaultUnitPrice = null
            ),
            roles.firstOrNull { it.id == "live-streaming-operator" }?.copy(
                title = "Live Streaming",
                defaultUnitPrice = null
            )
        )

        "tent-tables-equipment" -> listOfNotNull(
            roles.firstOrNull { it.id == "tent-rental" }?.copy(
                categoryId = "tent-tables-equipment",
                title = "Tent Setup",
                defaultUnitPrice = null
            ),
            roles.firstOrNull { it.id == "table-rental" }?.copy(
                categoryId = "tent-tables-equipment",
                title = "Table Rental",
                defaultUnitPrice = 60
            ),
            roles.firstOrNull { it.id == "chair-rental" }?.copy(
                categoryId = "tent-tables-equipment",
                title = "Chair Rental",
                defaultUnitPrice = 50
            ),
            roles.firstOrNull { it.id == "dining-equipment" }?.copy(
                categoryId = "tent-tables-equipment",
                title = "Dish & Serving Equipment",
                defaultUnitPrice = null
            )
        )

        else -> rolesForCategory(categoryId)
    }

    fun categoryForWorkerType(workerType: WorkerType): ServiceCategoryDefinition? {
        val role = roles.firstOrNull { it.workerType == workerType } ?: return null
        return category(role.categoryId)
    }

    private val categorySkillSuggestions = mapOf(
        "catering-food" to listOf(
            "Biryani", "Bulk cooking", "South Indian cooking", "North Indian cooking",
            "Sweets", "Snacks", "Live counters", "Plating", "Kitchen assistance"
        ),
        "decoration" to listOf(
            "Stage decoration", "Flower decoration", "Balloon decoration", "Lighting",
            "Mandap decoration", "Wedding decoration", "Tent setup"
        ),
        "entertainment" to listOf(
            "DJ", "Singer", "Dancer", "Band", "Anchor", "Event host", "Live performance"
        ),
        "beauty" to listOf(
            "Mehndi", "Bridal mehndi", "Party makeup", "Bridal makeup",
            "Hairstyling", "Saree draping", "Facial/beauty services"
        ),
        "photography-video" to listOf(
            "Wedding photography", "Event photography", "Videography",
            "Cinematography", "Drone photography", "Live streaming"
        ),
        "religious-ceremony" to listOf("Pujari", "Pooja services", "Ritual assistance", "Ceremony support"),
        "event-support" to listOf("Event coordinator", "Host", "Event helper", "Guest management", "Support staff"),
        "rentals" to listOf("Chairs", "Tables", "Stage equipment", "Sound equipment", "Lighting equipment", "Event furniture"),
        "transport-logistics" to listOf("Event transport", "Delivery", "Driver", "Logistics support"),
        "other-event-services" to listOf("Event planning", "Custom requirements", "Other event services")
    )

    fun skillSuggestionsFor(categoryId: String, roleId: String?): List<String> {
        val roleSuggestions = roleId
            ?.let { selectedRoleId -> roles.firstOrNull { it.id == selectedRoleId }?.skillSuggestions }
            .orEmpty()
        return if (roleSuggestions.isNotEmpty()) roleSuggestions else categorySkillSuggestions[categoryId].orEmpty()
    }

    fun skillPlaceholderFor(categoryId: String, roleId: String?): String {
        val suggestions = skillSuggestionsFor(categoryId, roleId).take(4)
        return if (suggestions.isEmpty()) "e.g. Professional event services" else "e.g. ${suggestions.joinToString(", ")}"
    }
}
