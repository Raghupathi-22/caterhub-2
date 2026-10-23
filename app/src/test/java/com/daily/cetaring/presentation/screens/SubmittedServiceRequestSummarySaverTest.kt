package com.daily.cetaring.presentation.screens

import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.Serializable

/**
 * Regression tests for the crash when tapping "Back to Home" (or "View My Bookings" / system Back)
 * on the service-request success screen (Decoration, Beauty, DJ, Photography, other services).
 *
 * Leaving a navigation destination makes Compose's SaveableStateHolder call
 * [SaveableStateRegistry.performSave], which throws if any rememberSaveable value cannot be stored
 * in an Android Bundle.
 */
class SubmittedServiceRequestSummarySaverTest {

    private val summary = SubmittedServiceRequestSummary(
        categoryTitle = "Decoration",
        eventType = "Wedding",
        eventDate = "2026-11-02",
        startTime = "18:00",
        endTime = "22:00",
        location = "Madhapur, Plot 12"
    )

    /** Mirrors the Android `canBeSavedToBundle` rules used by the activity-level registry. */
    private fun canBeSavedToBundle(value: Any): Boolean = when (value) {
        is SnapshotMutableState<*> -> value.value?.let(::canBeSavedToBundle) ?: true
        is String, is Number, is Boolean, is Char -> true
        is List<*> -> value.all { it == null || canBeSavedToBundle(it) }
        is Serializable -> true
        else -> false
    }

    private fun registry() = SaveableStateRegistry(restoredValues = null, canBeSaved = ::canBeSavedToBundle)

    @Test
    fun `unsaveable summary state reproduces the leave-screen crash`() {
        val registry = registry()
        val rawState = mutableStateOf<SubmittedServiceRequestSummary?>(null)
        registry.registerProvider("submittedSummary") { rawState }
        rawState.value = summary

        assertThrows(IllegalStateException::class.java) { registry.performSave() }
    }

    @Test
    fun `summary saver keeps performSave from crashing when leaving success screen`() {
        val registry = registry()
        val scope = SaverScope { registry.canBeSaved(it) }
        val state = mutableStateOf<SubmittedServiceRequestSummary?>(summary)
        registry.registerProvider("submittedSummary") {
            with(SubmittedServiceRequestSummarySaver) { scope.save(state.value) }?.let { mutableStateOf(it) }
        }

        val saved = registry.performSave()

        assertTrue(saved.containsKey("submittedSummary"))
    }

    @Test
    fun `summary saver round-trips every service category summary`() {
        val scope = SaverScope(::canBeSavedToBundle)
        listOf("Decoration", "Beauty Services", "DJ", "Photography", "Event Support").forEach { title ->
            val original = summary.copy(categoryTitle = title)
            val saved = with(SubmittedServiceRequestSummarySaver) { scope.save(original) }!!

            assertTrue(canBeSavedToBundle(saved))
            assertEquals(original, SubmittedServiceRequestSummarySaver.restore(saved))
        }
    }

    @Test
    fun `no submission saves nothing and incomplete data restores to form state`() {
        val scope = SaverScope(::canBeSavedToBundle)
        assertNull(with(SubmittedServiceRequestSummarySaver) { scope.save(null) })
        assertNull(SubmittedServiceRequestSummarySaver.restore(arrayListOf("incomplete")))
    }
}
