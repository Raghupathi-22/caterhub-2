package com.daily.cetaring.domain.catalog

import java.util.Locale

enum class ServiceListingAction {
    /** Opens the existing service request flow with this service pre-selected. */
    BOOK_SERVICE,
    /** Opens the existing catering booking flow. */
    BOOK_CATERING,
    /** Opens the existing menu screen, filtered to this menu category. */
    EXPLORE_MENU
}

/**
 * A customer-facing card on a category listing screen. Titles, prices and identifiers come from
 * [ServiceCatalog] / [MenuCatalog]; [unitPrice] is null when the service is quoted, so the UI never
 * shows an invented price. No ratings, reviews or popularity data exist in the catalogue, so none are modelled.
 */
data class ServiceListing(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val unitPrice: Int?,
    val priceUnit: String?,
    val action: ServiceListingAction
)

object ServiceListingCatalog {
    const val CATERING_CATEGORY_ID = "catering-food"
    const val EVENT_CATERING_LISTING_ID = "event-catering"

    private val itemPricedCategories = setOf("tent-tables-equipment", "rentals")

    private val descriptions = mapOf(
        "catering-boy" to "Service staff for food serving, buffet counters and guest assistance.",
        "catering-girl" to "Service staff for serving, welcome counters and guest assistance.",
        "chef" to "A chef to cook your event menu on site.",
        "kitchen-helper" to "Kitchen support for preparation, washing and clean-up.",

        "event-decorator" to "Complete decoration for weddings, receptions, birthdays and functions.",
        "balloon-decorator" to "Balloon arches, flower garlands and themed balloon and floral decor.",
        "stage-setup-worker" to "Crew to build and set up the stage for your event.",
        "mandap-decorator" to "Stage and traditional mandap decoration for weddings and ceremonies.",
        "entrance-decorator" to "Entrance arches, pathways and themed backdrops for stages and photo areas.",
        "backdrop-decorator" to "Themed backdrops for stages, photo areas and receptions.",
        "decoration-supervisor" to "An on-site supervisor to coordinate the decoration team.",

        "tent-rental" to "Tent and shamiana setup for outdoor functions.",
        "table-rental" to "Tables for dining, buffet counters and registration desks.",
        "chair-rental" to "Chairs for your guests, priced per chair.",
        "dining-equipment" to "Plates, serving dishes and buffet equipment.",

        "dj" to "DJ with sound system for music, announcements and dancing.",
        "singer" to "Live singers for receptions, sangeet and family functions.",
        "band-melam-artist" to "Band and traditional melam group for processions and ceremonies.",
        "dancer" to "Dance performances for sangeet, receptions and celebrations.",
        "anchor" to "A host to run your event programme and engage guests.",
        "magician" to "A magic show to entertain guests of all ages.",

        "photographer" to "Photo coverage of your wedding, function or celebration.",
        "videographer" to "Video coverage of your event, from rituals to reception.",
        "live-streaming-operator" to "Stream your event live for guests who cannot attend.",

        "makeup-artist" to "Party and event makeup for you and your family.",
        "bridal-makeup-artist" to "Bridal makeup for the wedding day and related functions.",
        "groom-makeup-artist" to "Grooming and makeup for the groom.",
        "hair-stylist" to "Hairstyling for brides, family members and guests.",
        "mehendi-artist" to "Bridal, Arabic and traditional mehndi designs.",
        "saree-drapist" to "Saree draping for brides and family members.",
        "beauty-specialist" to "Beauty and skin-care services before your event.",

        "pujari" to "Experienced Purohit for poojas, homams and traditional ceremonies.",
        "priest" to "A priest to conduct ceremonies and rituals.",
        "pandit" to "A pandit for muhurtham guidance and ceremonies.",
        "pooja-specialist" to "Pooja arrangements and rituals for special occasions.",
        "homam-specialist" to "Homam and havan rituals performed with traditional procedure.",
        "wedding-ritual-specialist" to "Guidance and support for traditional wedding rituals."
    )

    fun descriptionFor(categoryId: String, roleId: String): String =
        descriptions[roleId] ?: ServiceCatalog.category(categoryId)?.subtitle.orEmpty()

    /** Unit shown next to a fixed price. Rentals are priced per item even though they map to a worker type. */
    fun priceUnitFor(categoryId: String, role: ServiceRoleDefinition): String =
        if (categoryId in itemPricedCategories || role.workerType == null) "item" else "person"

    fun listingsFor(categoryId: String): List<ServiceListing> {
        if (categoryId == CATERING_CATEGORY_ID) return cateringListings()
        return ServiceCatalog.customerRolesForCategory(categoryId)
            .distinctBy { it.id }
            .map { role ->
                ServiceListing(
                    id = role.id,
                    categoryId = categoryId,
                    title = role.title,
                    description = descriptionFor(categoryId, role.id),
                    unitPrice = role.defaultUnitPrice,
                    priceUnit = role.defaultUnitPrice?.let { priceUnitFor(categoryId, role) },
                    action = ServiceListingAction.BOOK_SERVICE
                )
            }
    }

    private fun cateringListings(): List<ServiceListing> {
        val eventCatering = ServiceListing(
            id = EVENT_CATERING_LISTING_ID,
            categoryId = CATERING_CATEGORY_ID,
            title = "Event Catering",
            description = "Choose your menu, guest count and event details in one catering booking.",
            unitPrice = null,
            priceUnit = null,
            action = ServiceListingAction.BOOK_CATERING
        )
        val menuCategories = MenuCatalog.categories
            .sortedBy { it.sortOrder }
            .map { menuCategory ->
                ServiceListing(
                    id = menuCategory.id,
                    categoryId = CATERING_CATEGORY_ID,
                    title = menuCategory.displayName,
                    description = menuCategory.description,
                    unitPrice = null,
                    priceUnit = null,
                    action = ServiceListingAction.EXPLORE_MENU
                )
            }
        return (listOf(eventCatering) + menuCategories).distinctBy { it.id }
    }

    fun search(listings: List<ServiceListing>, query: String): List<ServiceListing> {
        val terms = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return listings
        return listings.filter { listing ->
            val haystack = "${listing.title} ${listing.description}".lowercase(Locale.ROOT)
            terms.all { haystack.contains(it) }
        }
    }
}
