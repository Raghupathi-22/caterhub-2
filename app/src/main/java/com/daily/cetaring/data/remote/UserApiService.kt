package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.UpdateUserProfileRequest
import com.daily.cetaring.data.remote.dto.UserDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH

interface UserApiService {
    @GET("users/me")
    suspend fun getMyProfile(@Header("Authorization") authorization: String): UserDTO

    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateUserProfileRequest
    ): UserDTO
}

