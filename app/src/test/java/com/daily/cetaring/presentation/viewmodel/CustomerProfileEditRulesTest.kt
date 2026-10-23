package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.presentation.viewmodel.CustomerProfileEditRules.Field
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerProfileEditRulesTest {

    @Test
    fun validProfilePasses() {
        assertTrue(CustomerProfileEditRules.validate("Asha", "Rao", "asha@example.com").isEmpty())
        assertTrue(CustomerProfileEditRules.validate("Asha", "", "").isEmpty())
    }

    @Test
    fun blankFirstNameIsRejected() {
        assertTrue(Field.FIRST_NAME in CustomerProfileEditRules.validate("  ", "Rao", ""))
    }

    @Test
    fun invalidOrOverlongEmailIsRejected() {
        assertTrue(Field.EMAIL in CustomerProfileEditRules.validate("Asha", "", "not-an-email"))
        val longEmail = "a".repeat(CustomerProfileEditRules.MAX_EMAIL_LENGTH) + "@x.com"
        assertTrue(Field.EMAIL in CustomerProfileEditRules.validate("Asha", "", longEmail))
    }

    @Test
    fun overlongLastNameIsRejected() {
        val long = "x".repeat(CustomerProfileEditRules.MAX_NAME_LENGTH + 1)
        assertTrue(Field.LAST_NAME in CustomerProfileEditRules.validate("Asha", long, ""))
    }

    @Test
    fun requestIsTrimmedAndNeverChangesVerifiedPhone() {
        val request = CustomerProfileEditRules.toRequest("  Asha   Devi ", " ", " asha@example.com ")
        assertEquals("Asha Devi", request.firstName)
        assertNull(request.lastName)
        assertEquals("asha@example.com", request.email)
        assertNull(request.phoneNumber)
    }
}
