package com.daily.cetaring.domain.catalog

import com.daily.cetaring.data.remote.dto.WorkerType

/**
 * Single source of truth for the structured choices a worker makes when registering or editing
 * their profile: role-specific skills, Hyderabad service areas, languages and experience presets.
 *
 * The backend stores skills, preferred areas and languages as free text (comma-separated, max
 * [MAX_FIELD_LENGTH] characters), so selections are joined with [joinList] and existing profiles
 * are read back with [parseList]. No API or database change is needed.
 */
object WorkerProfileCatalog {
    /** Backend column / DTO limit for skills, preferredAreas and languages. */
    const val MAX_FIELD_LENGTH = 1000
    const val MAX_BIO_LENGTH = 500
    const val MAX_CUSTOM_ITEM_LENGTH = 40
    const val MIN_CUSTOM_ITEM_LENGTH = 2
    const val MAX_SKILLS = 15
    const val MAX_AREAS = 20
    const val MAX_LANGUAGES = 10
    /** Mirrors [com.daily.cetaring.data.remote.dto.WorkerOnboardingValidator]. */
    const val MAX_EXPERIENCE_YEARS = 60

    data class ExperiencePreset(val years: Int, val label: String)

    val experiencePresets: List<ExperiencePreset> = listOf(
        ExperiencePreset(0, "Fresher"),
        ExperiencePreset(1, "1 year"),
        ExperiencePreset(2, "2 years"),
        ExperiencePreset(3, "3 years"),
        ExperiencePreset(5, "5 years"),
        ExperiencePreset(10, "10+ years")
    )

    fun experienceLabel(years: Int?): String = when {
        years == null -> "Not selected"
        years == 0 -> "Fresher (0 years)"
        years == 1 -> "1 year"
        else -> "$years years"
    }

    /** Keyed by the canonical registration role ids in [ServiceCatalog.workerRegistrationRolesForCategory]. */
    private val roleSkills: Map<String, List<String>> = mapOf(
        // Catering & Food
        "chef" to listOf(
            "South Indian Cooking", "North Indian Cooking", "Biryani", "Vegetarian Cooking",
            "Non-Vegetarian Cooking", "Bulk Cooking", "Live Counters", "Menu Planning", "Tandoor"
        ),
        "assistant-chef" to listOf(
            "Food Preparation", "Vegetable Cutting", "Marination", "Gravy Preparation",
            "Rice Preparation", "Bulk Cooking Support", "Live Counter Support"
        ),
        "biryani-chef" to listOf(
            "Hyderabadi Dum Biryani", "Veg Biryani", "Mutton Biryani", "Chicken Biryani",
            "Tandoor", "Sweets & Mithai", "Haleem", "Bulk Cooking"
        ),
        "kitchen-helper" to listOf(
            "Vegetable Cutting", "Dish Washing", "Kitchen Cleaning", "Food Preparation",
            "Loading & Unloading", "Stock Handling"
        ),
        "serving-staff" to listOf(
            "Table Service", "Buffet Service", "Guest Service", "Food Serving",
            "Event Setup", "Beverage Service", "Counter Service"
        ),
        // Decoration
        "event-decorator" to listOf(
            "Wedding Decoration", "Stage Decoration", "Mandap Decoration", "Reception Decoration",
            "Birthday Decoration", "Floral Decoration", "Entrance Decoration", "Backdrop Design"
        ),
        "mandap-decorator" to listOf(
            "Mandap Decoration", "Stage Decoration", "Wedding Stage", "Reception Stage",
            "Backdrop Design", "Floral Stage Decor", "Lighting Setup"
        ),
        "balloon-decorator" to listOf(
            "Balloon Arches", "Balloon Walls", "Birthday Decoration", "Theme Decoration",
            "Baby Shower Decoration", "Naming Ceremony Decoration"
        ),
        "flower-decorator" to listOf(
            "Fresh Flower Decoration", "Garland Making", "Floral Mandap", "Car Decoration",
            "Entrance Floral Decor", "Floral Backdrops", "Pellikuthuru Decoration"
        ),
        "stage-setup-worker" to listOf(
            "Stage Setup", "Tent & Shamiana", "Lighting Setup", "Decoration Support",
            "Material Handling", "Dismantling"
        ),
        // Entertainment
        "dj" to listOf(
            "Wedding DJ", "Party DJ", "Sound Setup", "Music Mixing", "Live Event DJ",
            "Sangeet DJ", "Lighting Effects"
        ),
        "singer" to listOf(
            "Telugu Songs", "Hindi Songs", "Devotional Songs", "Wedding Songs",
            "Classical Music", "Ghazals", "Live Band Vocals"
        ),
        "band-melam-artist" to listOf(
            "Wedding Band", "Nadaswaram", "Melam", "Dhol", "Brass Band", "Baraat Procession"
        ),
        "dancer" to listOf(
            "Sangeet Choreography", "Classical Dance", "Folk Dance", "Bollywood Dance",
            "Group Performance", "Kuchipudi", "Bharatanatyam"
        ),
        "anchor" to listOf(
            "Wedding Anchoring", "Sangeet Hosting", "Corporate Events", "Birthday Hosting",
            "Games & Activities", "Telugu Anchoring", "Bilingual Hosting"
        ),
        "magician" to listOf(
            "Stage Magic", "Close-up Magic", "Kids Magic Show", "Comedy Magic", "Balloon Art"
        ),
        // Beauty
        "makeup-artist" to listOf(
            "Bridal Makeup", "Party Makeup", "HD Makeup", "Airbrush Makeup", "Groom Makeup",
            "Traditional Makeup", "Reception Makeup"
        ),
        "mehendi-artist" to listOf(
            "Bridal Mehndi", "Arabic Mehndi", "Traditional Mehndi", "Engagement Mehndi",
            "Party Mehndi", "Kids Mehndi"
        ),
        "hair-stylist" to listOf(
            "Bridal Hairstyling", "Party Hairstyling", "Traditional Hairstyles", "Hair Setting",
            "Groom Hairstyling", "Bun & Braid Styles"
        ),
        "beauty-specialist" to listOf(
            "Facial", "Waxing", "Threading", "Manicure & Pedicure", "Nail Art", "Pre-Bridal Care"
        ),
        "saree-drapist" to listOf(
            "Saree Draping", "Pleating", "Dupatta Setting", "Jewellery Setting",
            "Bridal Dressing Support", "Makeup Assistance"
        ),
        // Photography & Video
        "photographer" to listOf(
            "Wedding Photography", "Candid Photography", "Portrait Photography",
            "Event Photography", "Traditional Photography", "Pre-Wedding Shoots"
        ),
        "videographer" to listOf(
            "Wedding Videography", "Cinematic Videos", "Traditional Videography",
            "Live Streaming", "Drone Videography", "Highlight Reels"
        ),
        "event-photographer" to listOf(
            "Candid Photography", "Wedding Candids", "Event Photography", "Couple Portraits",
            "Kids & Family Shoots"
        ),
        "video-editor" to listOf(
            "Wedding Film Editing", "Highlight Reels", "Photo Retouching", "Album Design",
            "Color Grading", "Social Media Reels"
        ),
        "photo-booth-operator" to listOf(
            "Photo Booth Setup", "Instant Prints", "Lighting Assistance", "Equipment Handling",
            "Shoot Coordination"
        ),
        // Religious & Ceremony
        "pujari" to listOf(
            "Wedding Rituals", "Homam", "Griha Pravesham", "Satyanarayana Vratham", "Pooja",
            "Traditional Ceremonies"
        ),
        "homam-specialist" to listOf(
            "Ganapathi Homam", "Navagraha Homam", "Sudarshana Homam", "Rudrabhishekam",
            "Vedic Chanting", "Shanti Pooja"
        ),
        "pooja-specialist" to listOf(
            "Pooja Arrangements", "Pooja Samagri Setup", "Ritual Assistance", "Ceremony Support",
            "Prasadam Preparation"
        ),
        "wedding-ritual-specialist" to listOf(
            "Wedding Rituals", "Engagement Ceremony", "Pellikoduku & Pellikuthuru", "Kalyanam",
            "Upanayanam", "Traditional Ceremonies"
        )
    )

    /** Suggested skills for the exact role; falls back to category suggestions for roles without a curated list. */
    fun skillsForRole(categoryId: String, roleId: String?): List<String> {
        val curated = roleId?.let { roleSkills[it] }.orEmpty()
        return (if (curated.isNotEmpty()) curated else ServiceCatalog.skillSuggestionsFor(categoryId, roleId))
            .distinctBy { it.lowercase() }
    }

    fun hasCuratedSkills(roleId: String): Boolean = roleSkills.containsKey(roleId)

    /** Hyderabad / Secunderabad / Cyberabad service areas, alphabetical. */
    val serviceAreas: List<String> = listOf(
        "Ameerpet", "Attapur", "Banjara Hills", "Begumpet", "Bachupally", "Chanda Nagar",
        "Dilsukhnagar", "Financial District", "Gachibowli", "Himayatnagar", "Hitech City",
        "Jubilee Hills", "Kokapet", "Kompally", "Kondapur", "Kothapet", "Kukatpally",
        "LB Nagar", "Madhapur", "Malkajgiri", "Manikonda", "Mehdipatnam", "Miyapur",
        "Nallagandla", "Nanakramguda", "Narsingi", "Nizampet", "Old City", "Puppalaguda",
        "Secunderabad", "Shamshabad", "Somajiguda", "Tarnaka", "Tolichowki", "Uppal",
        "Vanasthalipuram"
    ).sorted()

    val languages: List<String> = listOf(
        "Telugu", "Hindi", "English", "Urdu", "Tamil", "Kannada", "Malayalam",
        "Marathi", "Bengali", "Odia"
    )

    /** Splits a stored comma/semicolon/newline separated value into clean, de-duplicated items. */
    fun parseList(raw: String?): List<String> =
        raw.orEmpty()
            .split(',', ';', '\n', '|')
            .mapNotNull { normalizeItem(it) }
            .distinctBy { it.lowercase() }

    fun joinList(items: List<String>): String =
        items.mapNotNull { normalizeItem(it) }.distinctBy { it.lowercase() }.joinToString(", ")

    /** Trims and collapses whitespace; returns null for blank values or values without letters. */
    fun normalizeItem(raw: String): String? {
        val cleaned = raw.trim().replace(Regex("\\s+"), " ")
        return cleaned.takeIf { it.isNotEmpty() && it.any(Char::isLetter) }
    }

    /**
     * Validates a worker-typed custom entry (skill or area). Returns the normalized value or an error.
     * Items must be 2-40 characters of letters, digits, spaces and simple punctuation.
     */
    fun validateCustomItem(raw: String, existing: List<String>): Result<String> {
        val value = normalizeItem(raw)
            ?: return Result.failure(IllegalArgumentException("Enter a name with letters."))
        return when {
            value.length < MIN_CUSTOM_ITEM_LENGTH ->
                Result.failure(IllegalArgumentException("Use at least $MIN_CUSTOM_ITEM_LENGTH characters."))
            value.length > MAX_CUSTOM_ITEM_LENGTH ->
                Result.failure(IllegalArgumentException("Keep it under $MAX_CUSTOM_ITEM_LENGTH characters."))
            !value.matches(Regex("[\\p{L}\\p{N} &'()./+-]+")) ->
                Result.failure(IllegalArgumentException("Only letters, numbers and & ' ( ) . / + - are allowed."))
            value.count(Char::isLetter) < MIN_CUSTOM_ITEM_LENGTH ->
                Result.failure(IllegalArgumentException("Use at least $MIN_CUSTOM_ITEM_LENGTH letters."))
            existing.any { it.equals(value, ignoreCase = true) } ->
                Result.failure(IllegalArgumentException("\"$value\" is already selected."))
            else -> Result.success(value)
        }
    }

    /** Registration role that saves [workerType]; used to show role-specific skills when editing. */
    fun registrationRoleFor(workerType: WorkerType?): ServiceRoleDefinition? {
        if (workerType == null) return null
        return ServiceCatalog.workerRegistrationCategories.asSequence()
            .flatMap { ServiceCatalog.workerRegistrationRolesForCategory(it.id).asSequence() }
            .firstOrNull { it.workerType == workerType }
    }
}
