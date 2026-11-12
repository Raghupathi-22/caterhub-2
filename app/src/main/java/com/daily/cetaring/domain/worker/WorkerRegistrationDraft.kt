package com.daily.cetaring.domain.worker

import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.WorkerProfileCatalog

/**
 * Immutable worker-registration draft. All transitions go through [WorkerRegistrationRules] so
 * dependent selections (role, skills) are revalidated whenever a parent choice changes.
 */
data class WorkerRegistrationDraft(
    val step: Int = 0,
    val categoryId: String = "",
    val roleId: String = "",
    val experienceYears: Int? = null,
    val skills: List<String> = emptyList(),
    val areas: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val bio: String = ""
) {
    /** Bundle-friendly representation used by the Compose saver to survive process death. */
    fun toSaveable(): List<Any> = listOf(
        step, categoryId, roleId, experienceYears ?: -1,
        ArrayList(skills), ArrayList(areas), ArrayList(languages), bio
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromSaveable(saved: List<Any?>): WorkerRegistrationDraft? = runCatching {
            WorkerRegistrationRules.sanitize(
                WorkerRegistrationDraft(
                    step = saved[0] as Int,
                    categoryId = saved[1] as String,
                    roleId = saved[2] as String,
                    experienceYears = (saved[3] as Int).takeIf { it >= 0 },
                    skills = (saved[4] as List<String>).toList(),
                    areas = (saved[5] as List<String>).toList(),
                    languages = (saved[6] as List<String>).toList(),
                    bio = saved[7] as String
                )
            )
        }.getOrNull()
    }
}

object WorkerRegistrationRules {
    const val FIRST_STEP = 0
    const val LAST_STEP = 4

    enum class Step(val shortTitle: String, val title: String, val description: String) {
        SERVICE("Service", "Choose your service", "Select the service category you offer professionally."),
        ROLE("Role", "Choose your professional role", "Pick the role that best describes the work you do."),
        EXPERIENCE(
            "Experience",
            "Your experience",
            "Tell us about your professional experience so we can match you with suitable bookings."
        ),
        DETAILS("Details", "Skills, areas & languages", "Select your skills, where you work and the languages you speak."),
        REVIEW("Review", "Review your profile", "Check your details before submitting for verification.")
    }

    fun stepInfo(step: Int): Step = Step.entries[step.coerceIn(FIRST_STEP, LAST_STEP)]

    fun isValidCategory(categoryId: String): Boolean =
        ServiceCatalog.workerRegistrationCategories.any { it.id == categoryId }

    fun isValidRole(categoryId: String, roleId: String): Boolean =
        roleId.isNotBlank() && ServiceCatalog.workerRegistrationRolesForCategory(categoryId).any { it.id == roleId }

    /** Catalogue skills offered for the draft's current role (empty until a valid role is chosen). */
    fun suggestedSkills(draft: WorkerRegistrationDraft): List<String> =
        if (isValidRole(draft.categoryId, draft.roleId)) {
            WorkerProfileCatalog.skillsForRole(draft.categoryId, draft.roleId)
        } else {
            emptyList()
        }

    /** Selecting another category clears the role and its skills unless the role also belongs to it. */
    fun selectCategory(draft: WorkerRegistrationDraft, categoryId: String): WorkerRegistrationDraft {
        if (!isValidCategory(categoryId) || categoryId == draft.categoryId) return draft
        val roleStillValid = isValidRole(categoryId, draft.roleId)
        return draft.copy(
            categoryId = categoryId,
            roleId = if (roleStillValid) draft.roleId else "",
            skills = if (roleStillValid) draft.skills else emptyList()
        )
    }

    /** Selecting another role keeps only skills that are also offered for the new role. */
    fun selectRole(draft: WorkerRegistrationDraft, roleId: String): WorkerRegistrationDraft {
        if (!isValidRole(draft.categoryId, roleId) || roleId == draft.roleId) return draft
        val allowed = WorkerProfileCatalog.skillsForRole(draft.categoryId, roleId)
        return draft.copy(
            roleId = roleId,
            skills = draft.skills.filter { skill -> allowed.any { it.equals(skill, ignoreCase = true) } }
        )
    }

    fun setExperience(draft: WorkerRegistrationDraft, years: Int?): WorkerRegistrationDraft =
        draft.copy(experienceYears = years?.coerceIn(0, WorkerProfileCatalog.MAX_EXPERIENCE_YEARS))

    /** Digits only, so negative values are impossible; blank clears the value. */
    fun parseExperienceInput(input: String): Int? =
        input.filter(Char::isDigit).take(2).toIntOrNull()

    fun toggleSkill(draft: WorkerRegistrationDraft, skill: String): WorkerRegistrationDraft =
        draft.copy(skills = toggle(draft.skills, skill, WorkerProfileCatalog.MAX_SKILLS))

    fun toggleArea(draft: WorkerRegistrationDraft, area: String): WorkerRegistrationDraft =
        draft.copy(areas = toggle(draft.areas, area, WorkerProfileCatalog.MAX_AREAS))

    fun toggleLanguage(draft: WorkerRegistrationDraft, language: String): WorkerRegistrationDraft =
        draft.copy(languages = toggle(draft.languages, language, WorkerProfileCatalog.MAX_LANGUAGES))

    fun setBio(draft: WorkerRegistrationDraft, bio: String): WorkerRegistrationDraft =
        draft.copy(bio = bio.take(WorkerProfileCatalog.MAX_BIO_LENGTH))

    /** Adds or removes [item] (case-insensitive), respecting [max] items and the backend field length. */
    fun toggle(items: List<String>, item: String, max: Int): List<String> {
        val value = WorkerProfileCatalog.normalizeItem(item) ?: return items
        val existing = items.firstOrNull { it.equals(value, ignoreCase = true) }
        if (existing != null) return items - existing
        return add(items, value, max)
    }

    /** Adds [item] if it is new and fits; never removes. */
    fun add(items: List<String>, item: String, max: Int): List<String> {
        val value = WorkerProfileCatalog.normalizeItem(item) ?: return items
        if (items.any { it.equals(value, ignoreCase = true) } || items.size >= max) return items
        val next = items + value
        return if (WorkerProfileCatalog.joinList(next).length > WorkerProfileCatalog.MAX_FIELD_LENGTH) items else next
    }

    /** Backward jumps are always allowed; forward jumps only when every earlier step is complete. */
    fun goTo(draft: WorkerRegistrationDraft, step: Int): WorkerRegistrationDraft {
        val target = step.coerceIn(FIRST_STEP, LAST_STEP)
        val reachable = (FIRST_STEP until target).all { stepError(draft, it) == null }
        return if (target <= draft.step || reachable) draft.copy(step = target) else draft
    }

    fun next(draft: WorkerRegistrationDraft): WorkerRegistrationDraft =
        if (draft.step < LAST_STEP && stepError(draft, draft.step) == null) draft.copy(step = draft.step + 1) else draft

    fun previous(draft: WorkerRegistrationDraft): WorkerRegistrationDraft =
        draft.copy(step = (draft.step - 1).coerceAtLeast(FIRST_STEP))

    /** User-facing reason the given step cannot be completed yet, or null when it is valid. */
    fun stepError(draft: WorkerRegistrationDraft, step: Int): String? = when (step) {
        0 -> if (isValidCategory(draft.categoryId)) null else "Please choose the service you offer."
        1 -> if (isValidRole(draft.categoryId, draft.roleId)) null else "Please choose your professional role."
        2 -> when (val years = draft.experienceYears) {
            null -> "Please select your experience."
            !in 0..WorkerProfileCatalog.MAX_EXPERIENCE_YEARS ->
                "Experience must be between 0 and ${WorkerProfileCatalog.MAX_EXPERIENCE_YEARS} years."
            else -> null
        }
        3 -> when {
            draft.skills.isEmpty() -> "Select at least one skill."
            draft.areas.isEmpty() -> "Select at least one preferred service area."
            draft.languages.isEmpty() -> "Select at least one language."
            else -> null
        }
        4 -> (0..3).firstNotNullOfOrNull { stepError(draft, it) }
            ?: if (draft.bio.trim().length > WorkerProfileCatalog.MAX_BIO_LENGTH) {
                "Bio must be ${WorkerProfileCatalog.MAX_BIO_LENGTH} characters or fewer."
            } else {
                null
            }
        else -> null
    }

    /** Outstanding problems as (step, message), in step order, for the review summary. */
    fun validationSummary(draft: WorkerRegistrationDraft): List<Pair<Int, String>> =
        (0..3).mapNotNull { step -> stepError(draft, step)?.let { step to it } }

    /** Completion percentage; the optional bio counts towards a complete profile but is never required. */
    fun completion(draft: WorkerRegistrationDraft): Int {
        val checks = listOf(
            isValidCategory(draft.categoryId),
            isValidRole(draft.categoryId, draft.roleId),
            draft.experienceYears != null,
            draft.skills.isNotEmpty(),
            draft.areas.isNotEmpty(),
            draft.languages.isNotEmpty(),
            draft.bio.isNotBlank()
        )
        return checks.count { it } * 100 / checks.size
    }

    /** Drops anything that is no longer valid, e.g. a draft restored after a catalogue update. */
    fun sanitize(draft: WorkerRegistrationDraft): WorkerRegistrationDraft {
        val categoryId = draft.categoryId.takeIf(::isValidCategory).orEmpty()
        val roleId = draft.roleId.takeIf { isValidRole(categoryId, it) }.orEmpty()
        fun clean(items: List<String>, max: Int) = items.fold(emptyList<String>()) { acc, item -> add(acc, item, max) }
        val cleaned = draft.copy(
            step = draft.step.coerceIn(FIRST_STEP, LAST_STEP),
            categoryId = categoryId,
            roleId = roleId,
            experienceYears = draft.experienceYears?.takeIf { it in 0..WorkerProfileCatalog.MAX_EXPERIENCE_YEARS },
            skills = if (roleId.isEmpty()) emptyList() else clean(draft.skills, WorkerProfileCatalog.MAX_SKILLS),
            areas = clean(draft.areas, WorkerProfileCatalog.MAX_AREAS),
            languages = clean(draft.languages, WorkerProfileCatalog.MAX_LANGUAGES),
            bio = draft.bio.take(WorkerProfileCatalog.MAX_BIO_LENGTH)
        )
        val firstIncomplete = (FIRST_STEP until cleaned.step).firstOrNull { stepError(cleaned, it) != null }
        return if (firstIncomplete != null) cleaned.copy(step = firstIncomplete) else cleaned
    }

    /** Builds the existing backend request; null while the draft is incomplete. */
    fun toCreateRequest(draft: WorkerRegistrationDraft): CreateWorkerProfileRequest? {
        if (stepError(draft, LAST_STEP) != null) return null
        val workerType = ServiceCatalog.workerRegistrationRole(draft.roleId)?.workerType ?: return null
        val experience = draft.experienceYears ?: return null
        return CreateWorkerProfileRequest(
            workerType = workerType,
            experienceYears = experience,
            skills = WorkerProfileCatalog.joinList(draft.skills),
            preferredAreas = WorkerProfileCatalog.joinList(draft.areas),
            languages = WorkerProfileCatalog.joinList(draft.languages),
            bio = draft.bio.trim().replace(Regex("[ \\t]+"), " ").ifBlank { null }
        )
    }
}
