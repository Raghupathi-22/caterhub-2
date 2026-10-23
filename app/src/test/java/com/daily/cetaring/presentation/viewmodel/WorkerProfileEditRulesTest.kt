package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.presentation.viewmodel.WorkerProfileEditRules.Field
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileEditRulesTest {

    private fun validate(
        name: String = "Ravi Kumar",
        age: String = "32",
        skills: String = "Biryani",
        areas: String = "Madhapur",
        languages: String = "Telugu",
        bio: String = ""
    ) = WorkerProfileEditRules.validate(name, age, skills, areas, languages, bio)

    @Test
    fun validProfilePasses() {
        assertTrue(validate().isEmpty())
        assertTrue(validate(age = "").isEmpty())
        assertTrue(validate(age = "18").isEmpty())
        assertTrue(validate(age = "75").isEmpty())
    }

    @Test
    fun ageOutsideAllowedRangeIsRejected() {
        assertTrue(Field.AGE in validate(age = "17"))
        assertTrue(Field.AGE in validate(age = "76"))
    }

    @Test
    fun requiredFieldsAreRejectedWhenBlank() {
        val errors = validate(name = " ", skills = "", areas = "  ", languages = "")
        assertEquals(setOf(Field.NAME, Field.SKILLS, Field.AREAS, Field.LANGUAGES), errors.keys)
        assertTrue(Field.NAME in validate(name = "A"))
    }

    @Test
    fun overlongBioIsRejected() {
        assertTrue(Field.BIO in validate(bio = "x".repeat(WorkerProfileEditRules.MAX_TEXT_LENGTH + 1)))
    }

    @Test
    fun requestIsTrimmedAndOptionalFieldsBecomeNull() {
        val request = WorkerProfileEditRules.toRequest("  Ravi   Kumar ", " ", " Biryani ", " Madhapur ", " Telugu ", "   ")
        assertEquals("Ravi Kumar", request.fullName)
        assertNull(request.age)
        assertEquals("Biryani", request.skills)
        assertEquals("Madhapur", request.preferredAreas)
        assertEquals("Telugu", request.languages)
        assertNull(request.bio)
        assertEquals(40, WorkerProfileEditRules.toRequest("Ravi", "40", "a", "b", "c", "").age)
    }
}
