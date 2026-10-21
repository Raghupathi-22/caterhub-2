package com.daily.cetaring.domain.catalog

import com.daily.cetaring.data.remote.dto.WorkerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceCatalogTest {
    @Test
    fun cateringStaffCategoryIsCustomerVisible() {
        val category = ServiceCatalog.category("catering-staff")
        requireNotNull(category)
        assertEquals("Catering Staff", category.title)
        assertEquals("CATERING_STAFF", category.serviceType)
        assertTrue(ServiceCatalog.customerCategories.any { it.id == "catering-staff" })
    }

    @Test
    fun cateringStaffContainsExactlyRequestedWorkerRoles() {
        val roles = ServiceCatalog.customerRolesForCategory("catering-staff")
        assertEquals(
            setOf(WorkerType.CATERING_BOY, WorkerType.CATERING_GIRL, WorkerType.CHEF, WorkerType.KITCHEN_HELPER),
            roles.mapNotNull { it.workerType }.toSet()
        )
    }

    @Test
    fun workerRegistrationShowsOnlySixRetainedCategories() {
        assertEquals(
            listOf(
                "catering-food",
                "decoration",
                "entertainment",
                "beauty",
                "photography-video",
                "religious-ceremony"
            ),
            ServiceCatalog.workerRegistrationCategories.map { it.id }
        )
        val ids = ServiceCatalog.workerRegistrationCategories.map { it.id }
        assertTrue("event-support" !in ids && "rentals" !in ids && "transport-logistics" !in ids)
        // Removed categories must still resolve for existing worker profiles and bookings.
        assertTrue(ServiceCatalog.category("event-support") != null)
        assertTrue(ServiceCatalog.categoryForWorkerType(WorkerType.EVENT_DRIVER) != null)
    }

    @Test
    fun workerRegistrationCateringRolesExcludeCookAndWaiter() {
        val roles = ServiceCatalog.workerRegistrationRolesForCategory("catering-food")
        val types = roles.mapNotNull { it.workerType }
        assertTrue(WorkerType.COOK !in types)
        assertTrue(WorkerType.WAITER !in types)
        assertTrue(roles.none { it.title == "Cook" || it.title == "Waiter" })
        // Existing COOK/WAITER worker profiles still map to their category.
        assertEquals("catering-food", ServiceCatalog.categoryForWorkerType(WorkerType.COOK)?.id)
        assertEquals("catering-food", ServiceCatalog.categoryForWorkerType(WorkerType.WAITER)?.id)
    }

    @Test
    fun everyRetainedWorkerCategoryHasSelectableRoles() {
        ServiceCatalog.workerRegistrationCategories.forEach { category ->
            val roles = ServiceCatalog.workerRegistrationRolesForCategory(category.id)
            assertTrue("${category.id} has no roles", roles.isNotEmpty())
            assertTrue("${category.id} has too many roles", roles.size <= 6)
            assertTrue(roles.all { it.workerType != null && it.categoryId == category.id })
            assertEquals(roles.size, roles.map { it.id }.toSet().size)
            assertEquals(roles.size, roles.mapNotNull { it.workerType }.toSet().size)
        }
    }

    @Test
    fun workerRegistrationShowsSimplifiedBroadRolesWithExistingWorkerTypes() {
        val expected = mapOf(
            "catering-food" to listOf(
                "Chef" to WorkerType.CHEF,
                "Assistant Chef" to WorkerType.ASSISTANT_CHEF,
                "Specialty Chef" to WorkerType.BIRYANI_CHEF,
                "Kitchen Helper" to WorkerType.KITCHEN_HELPER,
                "Catering Service Staff" to WorkerType.SERVING_STAFF
            ),
            "decoration" to listOf(
                "Event Decorator" to WorkerType.EVENT_DECORATOR,
                "Stage & Mandap Decorator" to WorkerType.MANDAP_DECORATOR,
                "Balloon Decorator" to WorkerType.BALLOON_DECORATOR,
                "Floral Decorator" to WorkerType.FLOWER_DECORATOR,
                "Decoration Assistant" to WorkerType.STAGE_SETUP_WORKER
            ),
            "entertainment" to listOf(
                "DJ & Sound System" to WorkerType.DJ,
                "Singer" to WorkerType.SINGER,
                "Band & Melam Group" to WorkerType.BAND_MELAM_ARTIST,
                "Dancer" to WorkerType.DANCER,
                "Anchor" to WorkerType.ANCHOR,
                "Magician" to WorkerType.MAGICIAN
            ),
            "beauty" to listOf(
                "Makeup Artist" to WorkerType.MAKEUP_ARTIST,
                "Mehndi Artist" to WorkerType.MEHENDI_ARTIST,
                "Hairstylist" to WorkerType.HAIR_STYLIST,
                "Beautician" to WorkerType.BEAUTY_SPECIALIST,
                "Beauty Assistant" to WorkerType.SAREE_DRAPIST
            ),
            "photography-video" to listOf(
                "Photographer" to WorkerType.PHOTOGRAPHER,
                "Videographer" to WorkerType.VIDEOGRAPHER,
                "Candid Photographer" to WorkerType.EVENT_PHOTOGRAPHER,
                "Video Editor" to WorkerType.VIDEO_EDITOR,
                "Photography Assistant" to WorkerType.PHOTO_BOOTH_OPERATOR
            ),
            "religious-ceremony" to listOf(
                "Priest/Pujari" to WorkerType.PUJARI,
                "Vedic Ritual Specialist" to WorkerType.HOMAM_SPECIALIST,
                "Ceremony Assistant" to WorkerType.POOJA_SPECIALIST,
                "Traditional Ceremony Specialist" to WorkerType.WEDDING_RITUAL_SPECIALIST
            )
        )
        assertEquals(expected.keys.toList(), ServiceCatalog.workerRegistrationCategories.map { it.id })
        expected.forEach { (categoryId, roles) ->
            assertEquals(
                categoryId,
                roles,
                ServiceCatalog.workerRegistrationRolesForCategory(categoryId).map { it.title to it.workerType }
            )
        }
    }

    @Test
    fun registrationRoleIdsResolveToTheSameSubmittedWorkerType() {
        // Submission resolves the saved WorkerType via ServiceCatalog.roles by id; it must match the card.
        ServiceCatalog.workerRegistrationCategories.forEach { category ->
            ServiceCatalog.workerRegistrationRolesForCategory(category.id).forEach { role ->
                assertEquals(role.workerType, ServiceCatalog.roles.firstOrNull { it.id == role.id }?.workerType)
                assertEquals(role, ServiceCatalog.workerRegistrationRole(role.id))
                assertTrue(!ServiceCatalog.workerRegistrationRoleDescription(role.id).isNullOrBlank())
                assertEquals(category.id, ServiceCatalog.categoryForWorkerType(role.workerType)?.id)
            }
        }
        assertEquals(null, ServiceCatalog.workerRegistrationRole("cook"))
        assertEquals(null, ServiceCatalog.workerRegistrationRole("waiter"))
    }

    @Test
    fun legacyFineGrainedRolesStillResolveForExistingWorkers() {
        listOf(
            WorkerType.HEAD_CHEF, WorkerType.TANDOOR_CHEF, WorkerType.CATERING_BOY, WorkerType.MALE_SINGER,
            WorkerType.SOUND_TECHNICIAN, WorkerType.MC, WorkerType.PANDIT, WorkerType.WEDDING_PHOTOGRAPHER
        ).forEach { type ->
            assertTrue(type.name, ServiceCatalog.roles.any { it.workerType == type })
            assertTrue(type.name, ServiceCatalog.categoryForWorkerType(type) != null)
        }
    }

    @Test
    fun entertainmentCustomerRolesAreSimplifiedToSixCategories() {
        val roles = ServiceCatalog.customerRolesForCategory("entertainment")
        assertEquals(6, roles.size)
        assertEquals(
            listOf("dj", "singer", "band-melam-artist", "dancer", "anchor", "magician"),
            roles.map { it.id }
        )
        assertEquals(
            listOf(
                "DJ & Sound System",
                "Singers",
                "Band Melam Group",
                "Dancer",
                "Anchor",
                "Magician"
            ),
            roles.map { it.title }
        )
    }
}
