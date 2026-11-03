package com.daily.cetaring.booking

import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.BookingRepository
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.presentation.components.EventCalendarRules
import com.daily.cetaring.presentation.components.EventTimeRules
import com.daily.cetaring.presentation.viewmodel.BookingViewModel
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** The redesigned selectors must feed the same draft values the booking API already expects. */
@OptIn(ExperimentalCoroutinesApi::class)
class BookingScheduleStateTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BookingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val authRepository = mockk<AuthRepository>(relaxed = true)
        every { authRepository.rolesFlow } returns flowOf(null)
        viewModel = BookingViewModel(mockk<BookingRepository>(relaxed = true), authRepository, mockk<WorkerRepository>(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `event type, date and time survive moving between steps`() {
        val event = com.daily.cetaring.domain.catalog.EventTypeCatalog.eventTypes.first { it.id == "pooja" }
        val date = LocalDate.now().plusDays(10)

        viewModel.updateDraft { it.copy(eventType = event.backendValue, guestCount = 150) }
        viewModel.updateDraft { it.copy(eventDate = EventCalendarRules.toIsoDate(date)) }
        viewModel.updateDraft { it.copy(eventTime = EventTimeRules.toStorage(EventTimeRules.of12Hour(7, 30, pm = true))) }
        // Later steps update unrelated fields; schedule values must be preserved.
        viewModel.updateDraft { it.copy(specialInstructions = "Parking at rear") }

        val draft = viewModel.draft.value
        assertEquals("Religious Ceremony", draft.eventType)
        assertEquals(date.toString(), draft.eventDate)
        assertEquals("19:30", draft.eventTime)
        assertEquals("${date}T19:30:00", draft.eventDateTimeIso())
        assertTrue(viewModel.validateStep(3) is BookingValidationResult.Valid)
    }

    @Test
    fun `past date is rejected by the existing step validation`() {
        val yesterday = LocalDate.now().minusDays(1)
        viewModel.updateDraft { it.copy(eventDate = EventCalendarRules.toIsoDate(yesterday), eventTime = "10:00") }
        assertTrue(viewModel.validateStep(3) is BookingValidationResult.Invalid)
    }

    @Test
    fun `missing time keeps the schedule step invalid`() {
        viewModel.updateDraft { it.copy(eventDate = LocalDate.now().plusDays(3).toString(), eventTime = "") }
        assertTrue(viewModel.validateStep(3) is BookingValidationResult.Invalid)
    }
}
