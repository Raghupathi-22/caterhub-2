package com.daily.cetaring.config

import org.junit.Assert.assertEquals
import org.junit.Test

class SupportContactTest {
    @Test
    fun callActionDialsSupportNumber() {
        assertEquals("tel:9959095202", SupportContact.dialUri)
    }

    @Test
    fun whatsAppActionUsesWaMeWithEncodedGreeting() {
        assertEquals(
            "https://wa.me/919959095202?text=Hello%20CaterHub%20Support%2C%20I%20need%20assistance.",
            SupportContact.whatsAppUrl
        )
    }

    @Test
    fun emailActionUsesSupportMailboxAndSubject() {
        assertEquals("caterhub.support@gmail.com", SupportContact.SUPPORT_EMAIL)
        assertEquals(
            "mailto:caterhub.support@gmail.com?subject=CaterHub%20Customer%20Support",
            SupportContact.emailUri
        )
    }
}