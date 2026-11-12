package com.daily.cetaring.config

import java.net.URLEncoder

object SupportContact {
    const val SUPPORT_PHONE_NATIONAL = "9959095202"
    const val SUPPORT_PHONE_WHATSAPP = "919959095202"
    const val SUPPORT_EMAIL = "caterhub.support@gmail.com"
    const val WHATSAPP_GREETING = "Hello CaterHub Support, I need assistance."
    const val EMAIL_SUBJECT = "CaterHub Customer Support"

    val dialUri: String = "tel:$SUPPORT_PHONE_NATIONAL"
    val whatsAppUrl: String = "https://wa.me/$SUPPORT_PHONE_WHATSAPP?text=${encode(WHATSAPP_GREETING)}"
    val emailUri: String = "mailto:$SUPPORT_EMAIL?subject=${encode(EMAIL_SUBJECT)}"

    // URLEncoder is form-encoding; wa.me and mailto expect %20 rather than '+' for spaces.
    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}