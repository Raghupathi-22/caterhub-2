package com.daily.cetaring.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OtpMessageParserTest {
    @Test
    fun extractsOtpWhenNearKeyword() {
        val message = "CaterHub OTP is 123456. Valid for 5 minutes."
        assertEquals("123456", OtpMessageParser.extractOtp(message))
    }

    @Test
    fun extractsOtpFromMultipleNumbersByKeywordProximity() {
        val message = "Need help call 1800123456. Your verification code: 654321."
        assertEquals("654321", OtpMessageParser.extractOtp(message))
    }

    @Test
    fun returnsNullWhenNoSixDigitCode() {
        val message = "Welcome to CaterHub. No OTP generated."
        assertNull(OtpMessageParser.extractOtp(message))
    }
}
