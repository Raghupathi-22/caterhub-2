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
    @SerializedName("user")
    val user: UserDTO
)

data class UserDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
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
    @SerializedName("roles")
    val roles: List<String> = emptyList()
)

data class SendOtpRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("userType")
    val userType: String? = null,
    @SerializedName("channel")
    val channel: String? = null
)

data class SendOtpResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName(value = "expiresInSeconds", alternate = ["expires_in_seconds"])
    val expiresInSeconds: Long? = null,
    @SerializedName(value = "deliveryChannel", alternate = ["delivery_channel"])
    val deliveryChannel: String? = null,
    @SerializedName(value = "resendCooldownSeconds", alternate = ["resend_cooldown_seconds"])
    val resendCooldownSeconds: Long? = null
)

data class VerifyOtpRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("name")
    val name: String? = null
)

data class UpdateUserProfileRequest(
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?
)

/** Unified customer sign-in / auto-registration (OTP purpose CUSTOMER_ACCESS). */
data class CustomerAccessVerifyRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String,
    @SerializedName("otp")
    val otp: String
)

data class CustomerAccessVerifyResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("auth")
    val auth: AuthResponse?,
    @SerializedName("registrationToken")
    val registrationToken: String?,
    @SerializedName("registrationExpiresInSeconds")
    val registrationExpiresInSeconds: Long?,
    @SerializedName("mobileNumber")
    val mobileNumber: String?
) {
    companion object {
        const val STATUS_AUTHENTICATED = "AUTHENTICATED"
        const val STATUS_REGISTRATION_REQUIRED = "REGISTRATION_REQUIRED"
    }
}

data class CustomerRegisterRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String,
    @SerializedName("registrationToken")
    val registrationToken: String,
    @SerializedName("fullName")
    val fullName: String
)