package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.UserApiService
import com.daily.cetaring.data.remote.dto.UpdateUserProfileRequest
import com.daily.cetaring.data.remote.dto.UserDTO
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketTimeoutException

class UserRepository(
    private val userApiService: UserApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun getMyProfile(): UserDTO {
        ensureBackendAvailable()
        val user = executeNetworkCall { userApiService.getMyProfile(bearerToken()) }
        saveUser(user)
        return user
    }

    suspend fun updateMyProfile(request: UpdateUserProfileRequest): UserDTO {
        ensureBackendAvailable()
        val user = executeNetworkCall { userApiService.updateMyProfile(bearerToken(), request) }
        saveUser(user)
        return user
    }

    private suspend fun saveUser(user: UserDTO) {
        authLocalDataSource.saveUserData(
            user.id.toString(),
            user.username,
            user.email,
            user.firstName,
            user.lastName,
            user.phoneNumber,
            user.roles
        )
    }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) throw IllegalStateException("Please login again")
        return "Bearer $token"
    }

    private suspend fun ensureBackendAvailable() {
        try {
            val health = executeNetworkCall { healthApiService.health() }
            if (!health.isUp) throw ServerOfflineException("Unable to connect to CaterHub")
        } catch (exception: ServerOfflineException) {
            throw exception
        } catch (exception: Exception) {
            throw mapNetworkException(exception)
        }
    }

    private suspend fun <T> executeNetworkCall(block: suspend () -> T): T = try {
        block()
    } catch (exception: Exception) {
        throw mapNetworkException(exception)
    }

    private fun mapNetworkException(exception: Exception): Exception = when (exception) {
        is ServerOfflineException -> exception
        is SocketTimeoutException -> ServerOfflineException("Unable to connect to CaterHub. Please check your internet connection and try again.")
        is EOFException, is ProtocolException -> IllegalStateException("Something went wrong. Please try again.")
        is IOException -> ServerOfflineException("Unable to connect to CaterHub. Please check your internet connection and try again.")
        is HttpException -> when (exception.code()) {
            400, 422 -> IllegalArgumentException("Please check your profile details and try again.")
            401 -> IllegalStateException("Please login again")
            403 -> IllegalArgumentException("You do not have permission to view this profile.")
            404 -> IllegalArgumentException("Profile was not found.")
            409 -> IllegalArgumentException("Email or phone number is already in use.")
            in 500..599 -> ServerOfflineException("Something went wrong. Please try again.")
            else -> IllegalArgumentException("Unable to save profile. Please try again.")
        }
        else -> exception
    }
}

