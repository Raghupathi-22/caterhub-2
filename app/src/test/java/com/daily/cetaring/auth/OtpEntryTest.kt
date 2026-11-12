package com.daily.cetaring.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpEntryTest {

    private fun listening(): OtpEntryState = OtpEntryRules.startSession(OtpEntryState(), listenForSms = true)

    @Test
    fun newSessionClearsCodeAndListens() {
        val first = OtpEntryRules.onManualInput(listening(), "123")
        val next = OtpEntryRules.startSession(first, listenForSms = true)
        assertEquals("", next.code)
        assertEquals(first.sessionId + 1, next.sessionId)
        assertTrue(next.isListening)
        assertFalse(next.manuallyEdited)
    }

    @Test
    fun inputIsEnabledInEveryAutoDetectionState() {
        OtpAutoStatus.values().forEach { status ->
            assertTrue(OtpEntryState(autoStatus = status).inputEnabled)
        }
        assertTrue(listening().inputEnabled)
    }

    @Test
    fun autoDetectionPopulatesEmptyField() {
        val state = listening()
        val filled = OtpEntryRules.onAutoDetected(state, "482913", state.sessionId)
        assertEquals("482913", filled.code)
        assertEquals(OtpSource.AUTO, filled.source)
        assertEquals(OtpAutoStatus.DETECTED, filled.autoStatus)
        assertTrue(filled.isComplete)
    }

    @Test
    fun manualEntryWorksWithoutAnySms() {
        var state = listening()
        "905217".forEach { state = OtpEntryRules.onManualInput(state, state.code + it) }
        assertEquals("905217", state.code)
        assertEquals(OtpSource.MANUAL, state.source)
        assertTrue(OtpEntryRules.canVerify(state, isVerifying = false))
    }

    @Test
    fun autoFilledCodeCanBeEditedManually() {
        val state = listening()
        val auto = OtpEntryRules.onAutoDetected(state, "482913", state.sessionId)
        val backspaced = OtpEntryRules.onManualInput(auto, "48291")
        assertEquals("48291", backspaced.code)
        assertTrue(backspaced.manuallyEdited)
        val retyped = OtpEntryRules.onManualInput(backspaced, "482917")
        assertEquals("482917", retyped.code)
        assertEquals(OtpSource.MANUAL, retyped.source)
    }

    @Test
    fun lateSmsDoesNotOverwriteManualEntry() {
        val typed = OtpEntryRules.onManualInput(listening(), "1234")
        val late = OtpEntryRules.onAutoDetected(typed, "999999", typed.sessionId)
        assertEquals("1234", late.code)
        assertEquals(OtpAutoStatus.IGNORED_MANUAL, late.autoStatus)
    }

    @Test
    fun smsFillsFieldAgainAfterManualTextIsCleared() {
        val typed = OtpEntryRules.onManualInput(listening(), "12")
        val cleared = OtpEntryRules.onManualInput(typed, "")
        val filled = OtpEntryRules.onAutoDetected(cleared, "654321", cleared.sessionId)
        assertEquals("654321", filled.code)
    }

    @Test
    fun smsFromPreviousSessionIsIgnored() {
        val old = listening()
        val resent = OtpEntryRules.startSession(old, listenForSms = true)
        val stale = OtpEntryRules.onAutoDetected(resent, "111111", old.sessionId)
        assertEquals("", stale.code)
    }

    @Test
    fun invalidAutoFormatIsIgnored() {
        val state = listening()
        listOf(null, "", "12345", "1234567", "12a456").forEach { otp ->
            assertEquals("", OtpEntryRules.onAutoDetected(state, otp, state.sessionId).code)
        }
    }

    @Test
    fun pastingSixDigitsFillsAllBoxes() {
        val pasted = OtpEntryRules.onManualInput(listening(), "736401")
        assertEquals("736401", pasted.code)
    }

    @Test
    fun pastingWholeSmsExtractsOtp() {
        val state = OtpEntryRules.onManualInput(listening(), "12")
        val pasted = OtpEntryRules.onManualInput(state, "12Your CaterHub OTP is 736401. Valid for 5 minutes.")
        assertEquals("736401", pasted.code)
    }

    @Test
    fun pastingWithSpacesAndDashesIsSanitised() {
        assertEquals("736401", OtpEntryRules.onManualInput(listening(), "736 401").code)
        assertEquals("736401", OtpEntryRules.onManualInput(listening(), "736-401").code)
    }

    @Test
    fun seventhKeystrokeIsIgnored() {
        val full = OtpEntryRules.onManualInput(listening(), "123456")
        assertEquals("123456", OtpEntryRules.onManualInput(full, "1234567").code)
    }

    @Test
    fun nonDigitsAreDropped() {
        assertEquals("12", OtpEntryRules.onManualInput(listening(), "1a2").code)
    }

    @Test
    fun timeoutEndsListeningButKeepsInput() {
        val typed = OtpEntryRules.onManualInput(listening(), "12")
        val timedOut = OtpEntryRules.onAutoEnded(typed, typed.sessionId, OtpAutoStatus.TIMED_OUT)
        assertEquals(OtpAutoStatus.TIMED_OUT, timedOut.autoStatus)
        assertEquals("12", timedOut.code)
        assertTrue(timedOut.inputEnabled)
        val more = OtpEntryRules.onManualInput(timedOut, "123")
        assertEquals("123", more.code)
    }

    @Test
    fun staleTimeoutIsIgnored() {
        val old = listening()
        val resent = OtpEntryRules.startSession(old, listenForSms = true)
        assertTrue(OtpEntryRules.onAutoEnded(resent, old.sessionId, OtpAutoStatus.TIMED_OUT).isListening)
    }

    @Test
    fun verifyNeedsSixDigitsAndNoRequestInFlight() {
        val partial = OtpEntryRules.onManualInput(listening(), "12345")
        assertFalse(OtpEntryRules.canVerify(partial, isVerifying = false))
        val full = OtpEntryRules.onManualInput(partial, "123456")
        assertTrue(OtpEntryRules.canVerify(full, isVerifying = false))
        assertFalse(OtpEntryRules.canVerify(full, isVerifying = true))
    }

    @Test
    fun mobileIsMasked() {
        assertEquals("+91 ••••••5202", OtpEntryRules.maskMobile("9876545202"))
        assertEquals("+91 12", OtpEntryRules.maskMobile("12"))
    }
}
