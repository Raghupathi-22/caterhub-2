package com.daily.cetaring.domain.catalog

import com.daily.cetaring.data.remote.dto.WorkerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileCatalogTest {

    private val allRegistrationRoles = ServiceCatalog.workerRegistrationCategories
        .flatMap { category -> ServiceCatalog.workerRegistrationRolesForCategory(category.id).map { category.id to it.id } }

    @Test
    fun everyRegistrationRoleHasCuratedDistinctSkills() {
        allRegistrationRoles.forEach { (categoryId, roleId) ->
            assertTrue("Missing curated skills for $roleId", WorkerProfileCatalog.hasCuratedSkills(roleId))
            val skills = WorkerProfileCatalog.skillsForRole(categoryId, roleId)
            assertTrue("$roleId needs at least 4 skills", skills.size >= 4)
            assertEquals("$roleId has duplicate skills", skills.size, skills.map { it.lowercase() }.toSet().size)
        }
    }

    @Test
    fun skillsAreRoleSpecific() {
        val chef = WorkerProfileCatalog.skillsForRole("catering-food", "chef")
        val makeup = WorkerProfileCatalog.skillsForRole("beauty", "makeup-artist")
        val mehndi = WorkerProfileCatalog.skillsForRole("beauty", "mehendi-artist")
        assertTrue("Bridal Makeup" in makeup)
        assertTrue("Arabic Mehndi" in mehndi)
        assertTrue("Biryani" in chef)
        assertTrue(chef.none { it in makeup || it in mehndi })
        assertFalse("Bridal Makeup" in mehndi)
    }

    @Test
    fun unknownRoleFallsBackToCategorySuggestions() {
        assertEquals(
            ServiceCatalog.skillSuggestionsFor("catering-food", "unknown"),
            WorkerProfileCatalog.skillsForRole("catering-food", "unknown")
        )
    }

    @Test
    fun areasAndLanguagesAreSortedAndUnique() {
        val areas = WorkerProfileCatalog.serviceAreas
        assertEquals(areas.sorted(), areas)
        assertEquals(areas.size, areas.map { it.lowercase() }.toSet().size)
        listOf("Gachibowli", "Madhapur", "Kondapur", "LB Nagar", "Secunderabad", "Hitech City").forEach {
            assertTrue(it in areas)
        }
        val languages = WorkerProfileCatalog.languages
        assertEquals(languages.size, languages.toSet().size)
        assertTrue(languages.containsAll(listOf("Telugu", "Hindi", "English", "Urdu", "Tamil", "Kannada", "Malayalam")))
    }

    @Test
    fun parseListSplitsTrimsAndDeduplicatesStoredValues() {
        assertEquals(
            listOf("Biryani", "Bulk cooking", "Live Counters"),
            WorkerProfileCatalog.parseList(" Biryani ,biryani;Bulk   cooking\nLive Counters, ,  ,123")
        )
        assertTrue(WorkerProfileCatalog.parseList(null).isEmpty())
        assertTrue(WorkerProfileCatalog.parseList("   ").isEmpty())
    }

    @Test
    fun joinListRoundTripsWithParseList() {
        val items = listOf("Telugu", "Hindi", "English")
        val joined = WorkerProfileCatalog.joinList(items + "telugu")
        assertEquals("Telugu, Hindi, English", joined)
        assertEquals(items, WorkerProfileCatalog.parseList(joined))
    }

    @Test
    fun customItemValidation() {
        assertEquals("Live Dosa Counter", WorkerProfileCatalog.validateCustomItem("  Live   Dosa Counter ", emptyList()).getOrNull())
        assertNull(WorkerProfileCatalog.validateCustomItem("ju", emptyList()).exceptionOrNull())
        assertNotNull(WorkerProfileCatalog.validateCustomItem("a", emptyList()).exceptionOrNull())
        assertNotNull(WorkerProfileCatalog.validateCustomItem("123", emptyList()).exceptionOrNull())
        assertNotNull(WorkerProfileCatalog.validateCustomItem("x".repeat(41), emptyList()).exceptionOrNull())
        assertNotNull(WorkerProfileCatalog.validateCustomItem("Cooking<script>", emptyList()).exceptionOrNull())
        assertNotNull(WorkerProfileCatalog.validateCustomItem("biryani", listOf("Biryani")).exceptionOrNull())
    }

    @Test
    fun registrationRoleIsResolvedFromSavedWorkerType() {
        val chef = WorkerProfileCatalog.registrationRoleFor(WorkerType.CHEF)
        assertEquals("chef", chef?.id)
        assertNull(WorkerProfileCatalog.registrationRoleFor(null))
    }

    @Test
    fun experienceLabels() {
        assertEquals("Fresher (0 years)", WorkerProfileCatalog.experienceLabel(0))
        assertEquals("1 year", WorkerProfileCatalog.experienceLabel(1))
        assertEquals("7 years", WorkerProfileCatalog.experienceLabel(7))
        assertEquals(0, WorkerProfileCatalog.experiencePresets.first().years)
    }
}
