package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.data.remote.dto.SendOtpRequest
import com.daily.cetaring.data.remote.dto.SendOtpResponse
import com.daily.cetaring.data.remote.dto.VerifyOtpRequest
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/otp/send")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthResponse

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
