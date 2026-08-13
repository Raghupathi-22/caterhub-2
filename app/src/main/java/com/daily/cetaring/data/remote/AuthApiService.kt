package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.LoginRequest
import com.daily.cetaring.data.remote.dto.RegisterRequest
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)
}

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)
