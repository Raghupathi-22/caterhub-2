package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.AuthApiService
import com.daily.cetaring.data.remote.dto.CustomerAccessVerifyRequest
import com.daily.cetaring.data.remote.dto.CustomerAccessVerifyResponse
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryCustomerAccessTest {
    private val api = mockk<AuthApiService>()
    private val local = mockk<AuthLocalDataSource>(relaxed = true)
    private val repository = AuthRepository(api, local)
    private val gson = Gson()

    private val authenticatedJson = """
        {"status":"AUTHENTICATED","mobileNumber":"+919876543210",
         "auth":{"access_token":"access","refresh_token":"refresh","token_type":"Bearer","expires_in":900,
                 "user":{"id":3,"username":"cust","phone_number":"+919876543210","is_active":true,
                         "is_verified":true,"roles":["ROLE_CUSTOMER"]}}}
    """.trimIndent()

    private val registrationJson = """
        {"status":"REGISTRATION_REQUIRED","registrationToken":"ticket",
         "registrationExpiresInSeconds":600,"mobileNumber":"+919876543210"}
    """.trimIndent()

    @Test
    fun backendResponsesParseIncludingNestedSnakeCaseTokens() {
        val authenticated = gson.fromJson(authenticatedJson, CustomerAccessVerifyResponse::class.java)
        assertEquals("access", authenticated.auth?.accessToken)
        assertEquals(listOf("ROLE_CUSTOMER"), authenticated.auth?.user?.roles)

        val registration = gson.fromJson(registrationJson, CustomerAccessVerifyResponse::class.java)
        assertEquals("ticket", registration.registrationToken)
        assertNull(registration.auth)
    }

    @Test
    fun authenticatedVerificationStoresSession() = runTest {
        coEvery { api.verifyCustomerAccess(any()) } returns
            gson.fromJson(authenticatedJson, CustomerAccessVerifyResponse::class.java)

        repository.verifyCustomerAccess(CustomerAccessVerifyRequest("9876543210", "123456"))

        coVerify(exactly = 1) { local.saveTokens("access", "refresh") }
    }

    @Test
    fun registrationRequiredDoesNotStoreAnySession() = runTest {
        coEvery { api.verifyCustomerAccess(any()) } returns
            gson.fromJson(registrationJson, CustomerAccessVerifyResponse::class.java)

        repository.verifyCustomerAccess(CustomerAccessVerifyRequest("9876543210", "123456"))

        coVerify(exactly = 0) { local.saveTokens(any(), any()) }
    }

    @Test
    fun malformedResponseIsRejectedWithoutSession() = runTest {
        coEvery { api.verifyCustomerAccess(any()) } returns
            CustomerAccessVerifyResponse("AUTHENTICATED", null, null, null, null)

        val result = runCatching {
            repository.verifyCustomerAccess(CustomerAccessVerifyRequest("9876543210", "123456"))
        }

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        coVerify(exactly = 0) { local.saveTokens(any(), any()) }
    }
}
