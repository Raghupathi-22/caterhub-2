package com.daily.cetaring.booking

import com.daily.cetaring.data.remote.dto.CateringStaffLineItem
import com.daily.cetaring.data.remote.dto.ServiceRequestRequest
import com.daily.cetaring.data.repository.ApiErrorMapper
import com.daily.cetaring.data.repository.ServerOfflineException
import com.daily.cetaring.presentation.components.EventTimeRules
import com.daily.cetaring.presentation.screens.SubmittedServiceRequestSummary
import com.daily.cetaring.presentation.screens.SubmittedServiceRequestSummarySaver
import com.daily.cetaring.presentation.screens.bookingReferenceFor
import com.daily.cetaring.presentation.screens.newBookingIdempotencyKey
import com.google.gson.Gson
import androidx.compose.runtime.saveable.SaverScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.time.LocalDateTime

class BookingSubmissionTest {

    private fun httpError(code: Int, body: String): HttpException =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    private fun map(e: Exception) = ApiErrorMapper.map(e, "booking", "Please check your booking details.", "Unable to submit.")

    @Test
    fun `server error shows the reference id but never raw server text`() {
        val mapped = map(httpError(500,
            """{"code":500,"message":"Something went wrong on our server. Please try again. (Ref: 3b1fdd8d)","data":{"error":"An unexpected error occurred","errorId":"3b1fdd8d"}}"""))

        assertTrue(mapped is ServerOfflineException)
        assertEquals("Something went wrong on our server. Please try again. (Ref: 3b1fdd8d)", mapped.message)
    }

    @Test
    fun `server error without reference keeps the generic message`() {
        val mapped = map(httpError(502, "<html>Bad gateway</html>"))
        assertEquals("Something went wrong on our server. Please try again.", mapped.message)
    }

    @Test
    fun `validation error shows the backend field message`() {
        val mapped = map(httpError(400,
            """{"code":400,"message":"Area must be 100 characters or fewer.","data":{"area":"Area must be 100 characters or fewer."}}"""))
        assertTrue(mapped is IllegalArgumentException)
        assertEquals("Area must be 100 characters or fewer.", mapped.message)
    }

    @Test
    fun `problem detail bodies are readable`() {
        val mapped = map(httpError(400, """{"type":"about:blank","title":"Bad Request","status":400,"detail":"Invalid request content."}"""))
        assertEquals("Invalid request content.", mapped.message)
    }

    @Test
    fun `event start must be later than now in India`() {
        val now = LocalDateTime.of(2026, 11, 11, 12, 0)
        assertTrue(EventTimeRules.startsInFuture("2026-11-12", "18:25", now))
        assertTrue(EventTimeRules.startsInFuture("2026-11-11", "12:05", now))
        assertFalse(EventTimeRules.startsInFuture("2026-11-11", "11:55", now))
        assertFalse(EventTimeRules.startsInFuture("2026-11-10", "18:25", now))
        assertFalse(EventTimeRules.startsInFuture("", "18:25", now))
    }

    @Test
    fun `idempotency keys are unique and match the backend header pattern`() {
        val first = newBookingIdempotencyKey()
        val second = newBookingIdempotencyKey()
        assertNotEquals(first, second)
        assertTrue(first.matches(Regex("[A-Za-z0-9_-]{1,64}")))
    }

    @Test
    fun `booking reference is stable and readable`() {
        assertEquals("CH-SR-000042", bookingReferenceFor(42))
    }

    @Test
    fun `non catering request serialises staffing for atomic backend creation`() {
        val json = Gson().toJson(ServiceRequestRequest(
            serviceType = "DECORATION", eventType = "WEDDING", eventDate = "2026-11-12",
            startTime = "18:00", endTime = "22:00", location = "hyf", area = "mani",
            selectedServices = listOf("Event Decorator"), totalAmount = BigDecimal(1200),
            staffing = listOf(CateringStaffLineItem(
                workerType = com.daily.cetaring.data.remote.dto.WorkerType.EVENT_DECORATOR,
                requiredWorkers = 1, payment = BigDecimal(1200)))
        ))
        assertTrue(json.contains("\"staffing\":[{\"workerType\":\"EVENT_DECORATOR\""))
    }

    @Test
    fun `success summary with reference survives state restoration`() {
        val scope = SaverScope { true }
        val summary = SubmittedServiceRequestSummary(
            "Catering Staff", "Wedding", "2026-11-12", "18:25", "21:55", "mani, hyf", "CH-SR-000042", "₹850")
        val saved = with(SubmittedServiceRequestSummarySaver) { scope.save(summary) }!!
        assertEquals(summary, SubmittedServiceRequestSummarySaver.restore(saved))
    }
}
