package com.daily.cetaring.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SendOtpResponseTest {
    @Test
    fun parsesSuccessfulResponseWithNullOptionalMetadata() {
        val response = Gson().fromJson(
            """
            {
              "success": true,
              "message": null,
              "expiresInSeconds": null,
              "deliveryChannel": null
            }
            """.trimIndent(),
            SendOtpResponse::class.java
        )

        assertTrue(response.success)
        assertNull(response.message)
        assertNull(response.expiresInSeconds)
        assertNull(response.deliveryChannel)
    }

    @Test
    fun parsesSuccessfulResponseWithOmittedOptionalMetadata() {
        val response = Gson().fromJson(
            """{"success":true}""",
            SendOtpResponse::class.java
        )

        assertTrue(response.success)
        assertNull(response.message)
        assertNull(response.expiresInSeconds)
        assertNull(response.deliveryChannel)
    }
}
