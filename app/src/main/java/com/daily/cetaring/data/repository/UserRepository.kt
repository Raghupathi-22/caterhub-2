package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.UserApiService
import com.daily.cetaring.data.remote.dto.UpdateUserProfileRequest
import com.daily.cetaring.data.remote.dto.UserDTO
import kotlinx.coroutines.flow.first

class UserRepository(
    private val userApiService: UserApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun getMyProfile(): UserDTO {
        val user = executeNetworkCall { userApiService.getMyProfile(bearerToken()) }
        saveUser(user)
        return user
    }

    suspend fun updateMyProfile(request: UpdateUserProfileRequest): UserDTO {
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
        if (token.isNullOrBlank()) throw IllegalStateException("Your session has expired. Please sign in again.")
        return "Bearer $token"
    }

    private suspend fun <T> executeNetworkCall(block: suspend () -> T): T = try {
        block()
    } catch (exception: Exception) {
        throw mapNetworkException(exception)
    }

    private fun mapNetworkException(exception: Exception): Exception =
        ApiErrorMapper.map(
            exception = exception,
            contextLabel = "profile",
            badRequestFallback = "Please check your profile details and try again.",
            defaultFallback = "Unable to save profile. Please try again."
        )
}

