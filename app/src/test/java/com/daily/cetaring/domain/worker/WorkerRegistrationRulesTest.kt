package com.daily.cetaring.domain.worker

import com.daily.cetaring.data.remote.dto.WorkerType
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.WorkerProfileCatalog
import com.daily.cetaring.domain.worker.WorkerRegistrationRules as Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerRegistrationRulesTest {

    private fun completeDraft(step: Int = Rules.LAST_STEP) = WorkerRegistrationDraft(
        step = step,
        categoryId = "beauty",
        roleId = "makeup-artist",
        experienceYears = 3,
        skills = listOf("Bridal Makeup", "HD Makeup"),
        areas = listOf("Gachibowli", "Madhapur"),
        languages = listOf("Telugu", "English"),
        bio = "  Bridal specialist  "
    )

    // --- Step 1: category -------------------------------------------------------------------

    @Test
    fun noCategoryIsPreselectedAndServiceIsRequired() {
        val draft = WorkerRegistrationDraft()
        assertEquals("", draft.categoryId)
        assertNotNull(Rules.stepError(draft, 0))
        assertEquals(0, Rules.next(draft).step)
    }

    @Test
    fun selectingCategoryAllowsContinuing() {
        val draft = Rules.selectCategory(WorkerRegistrationDraft(), "decoration")
        assertEquals("decoration", draft.categoryId)
        assertNull(Rules.stepError(draft, 0))
        assertEquals(1, Rules.next(draft).step)
    }

    @Test
    fun categoryNotOfferedAtRegistrationIsIgnored() {
        assertEquals("", Rules.selectCategory(WorkerRegistrationDraft(), "rentals").categoryId)
    }

    @Test
    fun changingCategoryClearsRoleAndSkillsButKeepsOtherAnswers() {
        val changed = Rules.selectCategory(completeDraft(), "catering-food")
        assertEquals("catering-food", changed.categoryId)
        assertEquals("", changed.roleId)
        assertTrue(changed.skills.isEmpty())
        assertEquals(listOf("Gachibowli", "Madhapur"), changed.areas)
        assertEquals(listOf("Telugu", "English"), changed.languages)
        assertEquals(3, changed.experienceYears)
    }

    @Test
    fun reselectingSameCategoryKeepsEverything() {
        val draft = completeDraft()
        assertEquals(draft, Rules.selectCategory(draft, "beauty"))
    }

    // --- Step 2: role -----------------------------------------------------------------------

    @Test
    fun rolesAreFilteredByCategory() {
        val catering = Rules.selectCategory(WorkerRegistrationDraft(), "catering-food")
        assertEquals("chef", Rules.selectRole(catering, "chef").roleId)
        assertEquals("", Rules.selectRole(catering, "makeup-artist").roleId)
        ServiceCatalog.workerRegistrationRolesForCategory("catering-food").forEach {
            assertEquals("catering-food", it.categoryId)
        }
    }

    @Test
    fun roleIsRequired() {
        val draft = Rules.selectCategory(WorkerRegistrationDraft(step = 1), "entertainment")
        assertNotNull(Rules.stepError(draft, 1))
        assertNull(Rules.stepError(Rules.selectRole(draft, "dj"), 1))
    }

    @Test
    fun changingRoleDropsSkillsNotOfferedForNewRole() {
        val chef = WorkerRegistrationDraft(
            categoryId = "catering-food",
            roleId = "chef",
            skills = listOf("Biryani", "Live Counters", "Menu Planning", "Tandoor")
        )
        val specialty = Rules.selectRole(chef, "biryani-chef")
        assertEquals("biryani-chef", specialty.roleId)
        assertEquals(listOf("Tandoor"), specialty.skills)

        assertTrue(Rules.selectRole(chef, "serving-staff").skills.isEmpty())
    }

    @Test
    fun changingRoleKeepsSharedSkills() {
        val pujari = WorkerRegistrationDraft(
            categoryId = "religious-ceremony",
            roleId = "pujari",
            skills = listOf("Wedding Rituals", "Homam")
        )
        assertEquals(listOf("Wedding Rituals"), Rules.selectRole(pujari, "wedding-ritual-specialist").skills)
    }

    @Test
    fun suggestedSkillsMatchExactRole() {
        val suggested = Rules.suggestedSkills(WorkerRegistrationDraft(categoryId = "catering-food", roleId = "chef"))
        assertEquals(WorkerProfileCatalog.skillsForRole("catering-food", "chef"), suggested)
        assertTrue(suggested.none { it.contains("Makeup") || it.contains("Mehndi") })
        assertTrue(Rules.suggestedSkills(WorkerRegistrationDraft(categoryId = "beauty")).isEmpty())
    }

    // --- Step 3: experience -----------------------------------------------------------------

    @Test
    fun fresherIsValidExperience() {
        val draft = Rules.setExperience(WorkerRegistrationDraft(), 0)
        assertEquals(0, draft.experienceYears)
        assertNull(Rules.stepError(draft, 2))
    }

    @Test
    fun missingExperienceIsInvalid() {
        assertNotNull(Rules.stepError(WorkerRegistrationDraft(), 2))
    }

    @Test
    fun experienceIsClampedAndParsedSafely() {
        assertEquals(WorkerProfileCatalog.MAX_EXPERIENCE_YEARS, Rules.setExperience(WorkerRegistrationDraft(), 99).experienceYears)
        assertEquals(0, Rules.setExperience(WorkerRegistrationDraft(), -4).experienceYears)
        assertEquals(5, Rules.parseExperienceInput("-5"))
        assertEquals(12, Rules.parseExperienceInput("12a3"))
        assertNull(Rules.parseExperienceInput(""))
        assertNull(Rules.parseExperienceInput("abc"))
    }

    @Test
    fun outOfRangeExperienceIsRejected() {
        assertNotNull(Rules.stepError(WorkerRegistrationDraft(experienceYears = 61), 2))
        assertNotNull(Rules.stepError(WorkerRegistrationDraft(experienceYears = -1), 2))
    }

    // --- Step 4: skills, areas, languages -----------------------------------------------------

    @Test
    fun multipleSkillsAreasAndLanguagesCanBeToggled() {
        var draft = WorkerRegistrationDraft(categoryId = "beauty", roleId = "makeup-artist")
        draft = Rules.toggleSkill(draft, "Bridal Makeup")
        draft = Rules.toggleSkill(draft, "Party Makeup")
        draft = Rules.toggleArea(draft, "Gachibowli")
        draft = Rules.toggleArea(draft, "Kondapur")
        draft = Rules.toggleLanguage(draft, "Telugu")
        draft = Rules.toggleLanguage(draft, "Hindi")
        assertEquals(listOf("Bridal Makeup", "Party Makeup"), draft.skills)
        assertEquals(listOf("Gachibowli", "Kondapur"), draft.areas)
        assertEquals(listOf("Telugu", "Hindi"), draft.languages)

        assertEquals(listOf("Kondapur"), Rules.toggleArea(draft, "gachibowli").areas)
    }

    @Test
    fun toggleIgnoresBlankAndDuplicateValuesAndRespectsLimits() {
        assertEquals(listOf("A b"), Rules.toggle(listOf("A b"), "   ", 5))
        assertEquals(listOf("Telugu"), Rules.add(listOf("Telugu"), " telugu ", 5))
        assertEquals(listOf("One", "Two"), Rules.toggle(listOf("One", "Two"), "Three", 2))
    }

    @Test
    fun selectionsNeverExceedBackendFieldLength() {
        var items = emptyList<String>()
        repeat(60) { items = Rules.add(items, "Skill number ${"x".repeat(20)} $it", 100) }
        assertTrue(WorkerProfileCatalog.joinList(items).length <= WorkerProfileCatalog.MAX_FIELD_LENGTH)
    }

    @Test
    fun detailsStepRequiresSkillsAreasAndLanguages() {
        val base = completeDraft(step = 3)
        assertNull(Rules.stepError(base, 3))
        assertNotNull(Rules.stepError(base.copy(skills = emptyList()), 3))
        assertNotNull(Rules.stepError(base.copy(areas = emptyList()), 3))
        assertNotNull(Rules.stepError(base.copy(languages = emptyList()), 3))
    }

    // --- Navigation -------------------------------------------------------------------------

    @Test
    fun stepNavigationMovesForwardOnlyWhenValid() {
        var draft = Rules.next(Rules.selectCategory(WorkerRegistrationDraft(), "photography-video"))
        assertEquals(1, draft.step)
        assertEquals(1, Rules.next(draft).step)
        draft = Rules.next(Rules.selectRole(draft, "photographer"))
        assertEquals(2, draft.step)
        draft = Rules.next(Rules.setExperience(draft, 0))
        assertEquals(3, draft.step)
        assertEquals(3, Rules.next(draft).step)
        draft = Rules.previous(draft)
        assertEquals(2, draft.step)
        assertEquals(0, Rules.previous(Rules.previous(Rules.previous(draft))).step)
    }

    @Test
    fun goingBackPreservesEnteredInformation() {
        val review = completeDraft()
        assertEquals(review.copy(step = 2), Rules.previous(Rules.previous(review)))
    }

    @Test
    fun editFromReviewJumpsBackAndCannotSkipIncompleteSteps() {
        val editing = Rules.goTo(completeDraft(), 1)
        assertEquals(1, editing.step)
        assertEquals(completeDraft().skills, editing.skills)
        assertEquals(Rules.LAST_STEP, Rules.goTo(editing, Rules.LAST_STEP).step)

        assertEquals(0, Rules.goTo(WorkerRegistrationDraft(categoryId = "beauty"), Rules.LAST_STEP).step)
    }

    // --- Draft persistence --------------------------------------------------------------------

    @Test
    fun draftSurvivesSaveAndRestore() {
        val draft = completeDraft()
        assertEquals(draft, WorkerRegistrationDraft.fromSaveable(draft.toSaveable()))

        val partial = WorkerRegistrationDraft(step = 2, categoryId = "beauty", roleId = "makeup-artist")
        assertEquals(partial, WorkerRegistrationDraft.fromSaveable(partial.toSaveable()))
    }

    @Test
    fun restoredDraftIsSanitized() {
        val restored = WorkerRegistrationDraft.fromSaveable(completeDraft().copy(roleId = "chef").toSaveable())!!
        assertEquals("", restored.roleId)
        assertTrue(restored.skills.isEmpty())
        assertEquals(1, restored.step)
        assertNull(WorkerRegistrationDraft.fromSaveable(listOf("garbage")))
    }

    // --- Review & submission ------------------------------------------------------------------

    @Test
    fun reviewReportsCompletionAndOutstandingIssues() {
        assertEquals(100, Rules.completion(completeDraft()))
        assertTrue(Rules.completion(completeDraft().copy(bio = "")) < 100)
        assertTrue(Rules.validationSummary(completeDraft()).isEmpty())
        val issues = Rules.validationSummary(completeDraft().copy(experienceYears = null, languages = emptyList()))
        assertEquals(listOf(2, 3), issues.map { it.first })
    }

    @Test
    fun emptyBioIsOptional() {
        val draft = completeDraft().copy(bio = "")
        assertNull(Rules.stepError(draft, Rules.LAST_STEP))
        assertNull(Rules.toCreateRequest(draft)!!.bio)
    }

    @Test
    fun bioIsLimited() {
        assertEquals(WorkerProfileCatalog.MAX_BIO_LENGTH, Rules.setBio(completeDraft(), "a".repeat(900)).bio.length)
    }

    @Test
    fun createRequestUsesExistingContract() {
        val request = Rules.toCreateRequest(completeDraft())!!
        assertEquals(WorkerType.MAKEUP_ARTIST, request.workerType)
        assertEquals(3, request.experienceYears)
        assertEquals("Bridal Makeup, HD Makeup", request.skills)
        assertEquals("Gachibowli, Madhapur", request.preferredAreas)
        assertEquals("Telugu, English", request.languages)
        assertEquals("Bridal specialist", request.bio)
    }

    @Test
    fun createRequestIsNullWhileIncomplete() {
        assertNull(Rules.toCreateRequest(completeDraft().copy(skills = emptyList())))
        assertNull(Rules.toCreateRequest(WorkerRegistrationDraft()))
    }

    @Test
    fun everyRegistrationRoleProducesItsWorkerType() {
        ServiceCatalog.workerRegistrationCategories.forEach { category ->
            ServiceCatalog.workerRegistrationRolesForCategory(category.id).forEach { role ->
                val draft = WorkerRegistrationDraft(
                    categoryId = category.id,
                    roleId = role.id,
                    experienceYears = 0,
                    skills = listOf(WorkerProfileCatalog.skillsForRole(category.id, role.id).first()),
                    areas = listOf("Uppal"),
                    languages = listOf("Telugu")
                )
                assertEquals(role.workerType, Rules.toCreateRequest(draft)?.workerType)
            }
        }
    }
}
