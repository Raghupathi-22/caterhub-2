package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    val user: UserDTO
)

data class UserDTO(
    val id: Long,
    val username: String,
    val email: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val roles: List<String> = emptyList()
)

data class SendOtpRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("userType")
    val userType: String? = null
)

data class SendOtpResponse(
    val success: Boolean,
    val message: String,
    val expiresInSeconds: Long
)

data class VerifyOtpRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String,
    val otp: String,
    val purpose: String,
    val name: String? = null
)

data class UpdateUserProfileRequest(
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val email: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?
)
