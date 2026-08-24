package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.AuthApiService
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.SendOtpRequest
import com.daily.cetaring.data.remote.dto.SendOtpResponse
import com.daily.cetaring.data.remote.dto.VerifyOtpRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketTimeoutException

class ServerOfflineException(message: String = "Server is unavailable. Please try again shortly.") : IOException(message)

class AuthRepository(
    private val apiService: AuthApiService,
    private val localDataSource: AuthLocalDataSource
) {

    val accessTokenFlow: Flow<String?> = localDataSource.accessTokenFlow
    val refreshTokenFlow: Flow<String?> = localDataSource.refreshTokenFlow
    val usernameFlow: Flow<String?> = localDataSource.usernameFlow
    val userIdFlow: Flow<String?> = localDataSource.userIdFlow
    val emailFlow: Flow<String?> = localDataSource.emailFlow
    val firstNameFlow: Flow<String?> = localDataSource.firstNameFlow
    val lastNameFlow: Flow<String?> = localDataSource.lastNameFlow
    val phoneNumberFlow: Flow<String?> = localDataSource.phoneNumberFlow
    val rolesFlow: Flow<String?> = localDataSource.rolesFlow

    suspend fun sendOtp(request: SendOtpRequest): SendOtpResponse {
        // OTP send is a public authentication operation. Do not call the protected
        // health endpoint first: a health/auth failure must not be misreported as
        // an expired user session.
        return executeNetworkCall { apiService.sendOtp(request) }
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): AuthResponse {
        // OTP verification is also public. Authentication starts only after the
        // OTP has been verified successfully.
        val response = executeNetworkCall { apiService.verifyOtp(request) }
        saveAuthResponse(response)
        return response
    }

    suspend fun refreshToken(token: String): AuthResponse {
        val response = executeNetworkCall {
            apiService.refreshToken(com.daily.cetaring.data.remote.RefreshTokenRequest(token))
        }
        saveAuthResponse(response)
        return response
    }

    suspend fun logout() {
        val refreshToken = localDataSource.refreshTokenFlow.first()
        refreshToken?.let {
            try {
                apiService.logout(com.daily.cetaring.data.remote.LogoutRequest(it))
            } catch (e: Exception) {
                // Logout should clear local state even when the server is unavailable.
            }
        }
        localDataSource.clearAll()
    }

    suspend fun getAccessToken(): String? = accessTokenFlow.first()
    suspend fun getRefreshToken(): String? = refreshTokenFlow.first()

    private suspend fun <T> executeNetworkCall(block: suspend () -> T): T {
        return try {
            block()
        } catch (exception: Exception) {
            throw mapNetworkException(exception)
        }
    }

    private suspend fun saveAuthResponse(response: AuthResponse) {
        localDataSource.saveTokens(response.accessToken, response.refreshToken)
        localDataSource.saveUserData(
            response.user.id.toString(),
            response.user.username,
            response.user.email,
            response.user.firstName,
            response.user.lastName,
            response.user.phoneNumber,
            response.user.roles
        )
    }

    private fun mapNetworkException(exception: Exception): Exception {
        return when (exception) {
            is ServerOfflineException -> exception
            is SocketTimeoutException -> ServerOfflineException("Connection timed out. Please try again.")
            is EOFException, is ProtocolException -> IllegalStateException("Received an invalid response from the server. Please try again.")
            is IOException -> ServerOfflineException()
            is HttpException -> when (exception.code()) {
                400 -> IllegalArgumentException(apiErrorMessage(exception) ?: "Please check your details and try again.")
                401 -> IllegalArgumentException(
                    apiErrorMessage(exception)
                        ?: "The authentication service rejected this request. Please make sure the CaterHub server is updated and try again."
                )
                403 -> IllegalArgumentException("You do not have permission to perform this action.")
                404 -> IllegalArgumentException("Requested service was not found. Please update the app or try again later.")
                in 500..599 -> ServerOfflineException()
                else -> IllegalArgumentException("Request failed. Please check your details and try again.")
            }
            else -> exception
        }
    }

    private fun apiErrorMessage(exception: HttpException): String? {
        return exception.response()?.errorBody()?.string()
            ?.let { body -> Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1) }
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
    }
}
