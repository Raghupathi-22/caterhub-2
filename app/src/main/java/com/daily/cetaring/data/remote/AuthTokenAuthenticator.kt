package com.daily.cetaring.data.remote

import com.daily.cetaring.config.AppConfig
import com.daily.cetaring.data.local.AuthLocalDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Automatically refreshes an expired access token once and retries the original request.
 * The refresh request uses a separate OkHttp client so a failed refresh cannot recurse
 * through this authenticator.
 */
class AuthTokenAuthenticator(
    private val authLocalDataSource: AuthLocalDataSource
) : Authenticator {

    private val refreshLock = Any()

    private val refreshApi: AuthApiService by lazy {
        val refreshClient = OkHttpClient.Builder().build()
        Retrofit.Builder()
            .baseUrl(AppConfig.apiBaseUrl)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val path = response.request.url.encodedPath
        if (path.endsWith("/auth/refresh") ||
            path.endsWith("/auth/otp/send") ||
            path.endsWith("/auth/otp/verify")) {
            return null
        }

        val failedAuthorization = response.request.header("Authorization")

        synchronized(refreshLock) {
            // Another request may have refreshed the token while this request was waiting.
            val currentToken = runBlocking { authLocalDataSource.getAccessToken() }
            if (!currentToken.isNullOrBlank()) {
                val currentAuthorization = "Bearer $currentToken"
                if (failedAuthorization != currentAuthorization) {
                    return response.request.newBuilder()
                        .header("Authorization", currentAuthorization)
                        .build()
                }
            }

            val refreshToken = runBlocking { authLocalDataSource.getRefreshToken() }
            if (refreshToken.isNullOrBlank()) return null

            return try {
                val authResponse = runBlocking {
                    refreshApi.refreshToken(RefreshTokenRequest(refreshToken))
                }

                runBlocking {
                    authLocalDataSource.saveTokens(
                        authResponse.accessToken,
                        authResponse.refreshToken
                    )
                    authLocalDataSource.saveUserData(
                        authResponse.user.id.toString(),
                        authResponse.user.username,
                        authResponse.user.email,
                        authResponse.user.firstName,
                        authResponse.user.lastName,
                        authResponse.user.phoneNumber,
                        authResponse.user.roles
                    )
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${authResponse.accessToken}")
                    .build()
            } catch (exception: Exception) {
                if (exception is HttpException && exception.code() in 400..403) {
                    // The refresh token is no longer usable. Clear the stale session so
                    // the next protected request cannot repeatedly retry a dead token.
                    runBlocking { authLocalDataSource.clearAll() }
                }
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
