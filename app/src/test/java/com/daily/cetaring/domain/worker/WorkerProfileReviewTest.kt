package com.daily.cetaring.domain.worker

import com.daily.cetaring.domain.worker.WorkerRegistrationRules as Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileReviewTest {

    private fun completeDraft(bio: String = "Bridal specialist") = WorkerRegistrationDraft(
        step = Rules.LAST_STEP,
        categoryId = "beauty",
        roleId = "makeup-artist",
        experienceYears = 3,
        skills = listOf("Bridal Makeup", "HD Makeup"),
        areas = listOf("Gachibowli"),
        languages = listOf("Telugu"),
        bio = bio
    )

    @Test
    fun emptyDraftHasZeroStrength() {
        assertEquals(0, Rules.profileStrength(WorkerRegistrationDraft()))
        assertFalse(Rules.isReadyToSubmit(WorkerRegistrationDraft()))
    }

    @Test
    fun completeProfileIsHundredPercent() {
        assertEquals(100, Rules.profileStrength(completeDraft()))
        assertTrue(Rules.checklist(completeDraft()).all { it.done })
    }

    @Test
    fun missingOptionalBioIsSubmittableButNotHundred() {
        val draft = completeDraft(bio = "  ")
        assertEquals(90, Rules.profileStrength(draft))
        assertTrue(Rules.isReadyToSubmit(draft))
        val bio = Rules.checklist(draft).single { !it.done }
        assertFalse(bio.required)
        assertEquals(4, bio.step)
    }

    @Test
    fun neverHundredWhenRequiredItemIsMissing() {
        val variants = listOf(
            completeDraft().copy(skills = emptyList()),
            completeDraft().copy(areas = emptyList()),
            completeDraft().copy(languages = emptyList()),
            completeDraft().copy(experienceYears = null),
            completeDraft().copy(roleId = "")
        )
        variants.forEach { draft ->
            assertTrue(Rules.profileStrength(draft) < 100)
            assertFalse(Rules.isReadyToSubmit(draft))
            assertTrue(Rules.checklist(draft).any { it.required && !it.done })
        }
    }

    @Test
    fun checklistPointsToCorrectEditSteps() {
        val steps = Rules.checklist(WorkerRegistrationDraft()).associate { it.label to it.step }
        assertEquals(0, steps["Service"])
        assertEquals(1, steps["Professional role"])
        assertEquals(2, steps["Experience"])
        assertEquals(3, steps["Skills"])
        assertEquals(3, steps["Service areas"])
        assertEquals(3, steps["Languages"])
    }

    @Test
    fun editFromReviewPreservesOtherValues() {
        val editing = Rules.goTo(completeDraft(), 2)
        assertEquals(2, editing.step)
        val changed = Rules.setExperience(editing, 7)
        val back = Rules.returnToReview(changed)
        assertEquals(Rules.LAST_STEP, back.step)
        assertEquals(7, back.experienceYears)
        assertEquals(completeDraft().skills, back.skills)
        assertEquals(completeDraft().areas, back.areas)
        assertEquals(completeDraft().bio, back.bio)
    }

    @Test
    fun returnToReviewStopsAtFirstIncompleteStep() {
        val changedCategory = Rules.selectCategory(Rules.goTo(completeDraft(), 0), "decoration")
        val next = Rules.returnToReview(changedCategory)
        assertEquals(1, next.step)
        assertEquals("", next.roleId)
        assertNotNull(Rules.stepError(next, 1))
    }

    @Test
    fun submitRequestStillUsesExistingContract() {
        val request = Rules.toCreateRequest(completeDraft(bio = ""))
        assertNotNull(request)
    }
}
