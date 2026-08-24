package com.daily.cetaring.data.remote

import com.daily.cetaring.config.AppConfig
import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

/**
 * Refreshes the short-lived access token automatically when the backend
 * returns HTTP 401. The refresh call uses a separate OkHttp client so it
 * cannot recursively invoke this authenticator.
 */
class AuthTokenAuthenticator(
    private val authLocalDataSource: AuthLocalDataSource
) : Authenticator {

    private val lock = Any()
    private val gson = Gson()
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        synchronized(lock) {
            val failedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()

            val currentToken = runBlocking { authLocalDataSource.accessTokenFlow.first() }
            if (!currentToken.isNullOrBlank() && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = runBlocking { authLocalDataSource.refreshTokenFlow.first() }
            if (refreshToken.isNullOrBlank()) return null

            val refreshed = refresh(refreshToken) ?: return null

            runBlocking {
                authLocalDataSource.saveTokens(refreshed.accessToken, refreshed.refreshToken)
                authLocalDataSource.saveUserData(
                    refreshed.user.id.toString(),
                    refreshed.user.username,
                    refreshed.user.email,
                    refreshed.user.firstName,
                    refreshed.user.lastName,
                    refreshed.user.phoneNumber,
                    refreshed.user.roles
                )
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.accessToken}")
                .build()
        }
    }

    private fun refresh(refreshToken: String): AuthResponse? {
        return try {
            val bodyJson = gson.toJson(RefreshTokenPayload(refreshToken))
            val request = Request.Builder()
                .url(AppConfig.apiBaseUrl + "auth/refresh")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            refreshClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return null
                gson.fromJson(body, AuthResponse::class.java)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    private data class RefreshTokenPayload(
        @SerializedName("refresh_token") val refreshToken: String
    )
}
