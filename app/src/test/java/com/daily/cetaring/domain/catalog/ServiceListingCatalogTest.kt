package com.daily.cetaring.domain.catalog

import com.daily.cetaring.presentation.components.ServiceImagery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceListingCatalogTest {

    @Test
    fun homeShowsAllEightCustomerCategoriesOnce() {
        val ids = ServiceCatalog.customerCategories.map { it.id }
        assertEquals(
            setOf(
                "catering-food", "decoration", "entertainment", "beauty", "photography-video",
                "religious-ceremony", "catering-staff", "tent-tables-equipment"
            ),
            ids.toSet()
        )
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun religiousCeremonyOffersOnlyPurohitBookedAsPujari() {
        val roles = ServiceCatalog.customerRolesForCategory("religious-ceremony")
        assertEquals(listOf("pujari"), roles.map { it.id })
        assertEquals("Purohit", roles.single().title)
        assertEquals(com.daily.cetaring.data.remote.dto.WorkerType.PUJARI, roles.single().workerType)
        assertNull("Purohit is quoted, not fixed-price", roles.single().defaultUnitPrice)
    }

    @Test
    fun decorationOffersFourDistinctOptionsWithExistingIdentifiers() {
        val roles = ServiceCatalog.customerRolesForCategory("decoration")
        assertEquals(
            listOf(
                "event-decorator" to "Event Decoration",
                "mandap-decorator" to "Stage & Mandap Decoration",
                "balloon-decorator" to "Balloon & Floral Decoration",
                "entrance-decorator" to "Entrance & Backdrop Decoration"
            ),
            roles.map { it.id to it.title }
        )
        assertTrue(roles.all { it.workerType != null })
        // Historical roles still resolve for existing bookings and worker profiles.
        listOf("priest", "pandit", "pooja-specialist", "homam-specialist", "stage-setup-worker", "backdrop-decorator")
            .forEach { id -> assertTrue(id, ServiceCatalog.roles.any { it.id == id }) }
    }

    @Test
    fun everyCustomerCategoryHasUniqueListings() {
        ServiceCatalog.customerCategories.forEach { category ->
            val listings = ServiceListingCatalog.listingsFor(category.id)
            assertTrue("${category.id} has no listings", listings.isNotEmpty())
            assertEquals("${category.id} has duplicates", listings.size, listings.map { it.id }.toSet().size)
            assertTrue(listings.all { it.categoryId == category.id })
        }
    }

    @Test
    fun nonCateringListingsUseExistingRoleIdentifiers() {
        ServiceCatalog.customerCategories.filter { it.id != ServiceListingCatalog.CATERING_CATEGORY_ID }.forEach { category ->
            val roleIds = ServiceCatalog.customerRolesForCategory(category.id).map { it.id }.toSet()
            val listings = ServiceListingCatalog.listingsFor(category.id)
            assertEquals(roleIds, listings.map { it.id }.toSet())
            assertTrue(listings.all { it.action == ServiceListingAction.BOOK_SERVICE })
        }
    }

    @Test
    fun entertainmentShowsOnlyTheSixCustomerCategories() {
        val titles = ServiceListingCatalog.listingsFor("entertainment").map { it.title }
        assertEquals(6, titles.size)
        assertEquals(
            ServiceCatalog.customerRolesForCategory("entertainment").map { it.title },
            titles
        )
    }

    @Test
    fun cateringOffersEventBookingThenEveryMenuCategory() {
        val listings = ServiceListingCatalog.listingsFor(ServiceListingCatalog.CATERING_CATEGORY_ID)
        assertEquals(ServiceListingCatalog.EVENT_CATERING_LISTING_ID, listings.first().id)
        assertEquals(ServiceListingAction.BOOK_CATERING, listings.first().action)
        val menuIds = listings.drop(1).map { it.id }
        assertEquals(MenuCatalog.categories.sortedBy { it.sortOrder }.map { it.id }, menuIds)
        assertTrue(listings.drop(1).all { it.action == ServiceListingAction.EXPLORE_MENU && it.unitPrice == null })
    }

    @Test
    fun pricesComeOnlyFromTheCatalogue() {
        ServiceCatalog.customerCategories.filter { it.id != ServiceListingCatalog.CATERING_CATEGORY_ID }.forEach { category ->
            val roles = ServiceCatalog.customerRolesForCategory(category.id).associateBy { it.id }
            ServiceListingCatalog.listingsFor(category.id).forEach { listing ->
                val role = roles.getValue(listing.id)
                assertEquals(role.defaultUnitPrice, listing.unitPrice)
                if (listing.unitPrice == null) assertNull(listing.priceUnit)
            }
        }
    }

    @Test
    fun everyBookableServiceHasItsOwnDescription() {
        ServiceCatalog.customerCategories.filter { it.id != ServiceListingCatalog.CATERING_CATEGORY_ID }.forEach { category ->
            ServiceListingCatalog.listingsFor(category.id).forEach { listing ->
                assertTrue(listing.description.isNotBlank())
                assertNotEquals("${listing.id} uses the generic fallback", category.subtitle, listing.description)
            }
        }
    }

    @Test
    fun rentalsArePricedPerItem() {
        ServiceCatalog.customerRolesForCategory("tent-tables-equipment").forEach { role ->
            assertEquals("item", ServiceListingCatalog.priceUnitFor("tent-tables-equipment", role))
        }
    }

    @Test
    fun searchMatchesAllTermsCaseInsensitively() {
        val listings = ServiceListingCatalog.listingsFor("entertainment")
        assertEquals(listings, ServiceListingCatalog.search(listings, "   "))
        val dj = ServiceListingCatalog.search(listings, "dj SOUND")
        assertTrue(dj.isNotEmpty())
        assertTrue(dj.all { "dj" in it.title.lowercase() || "dj" in it.description.lowercase() })
        assertTrue(ServiceListingCatalog.search(listings, "zzz-no-such-service").isEmpty())
    }

    @Test
    fun everyListingResolvesToAVisualWithoutRepeatingTheHeroPhoto() {
        ServiceCatalog.customerCategories.forEach { category ->
            val hero = ServiceImagery.bannerVisual(category.id).photoRes
            ServiceListingCatalog.listingsFor(category.id).forEach { listing ->
                val visual = ServiceImagery.serviceVisual(category.id, listing.id)
                if (hero != null) assertFalse(visual.photoRes == hero)
            }
        }
    }
}
