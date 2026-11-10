package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.CreateRiceOrderRequest
import com.daily.cetaring.data.remote.dto.CreateRiceSampleRequest
import com.daily.cetaring.data.remote.dto.RiceOrderResponse
import com.daily.cetaring.data.remote.dto.RiceProductResponse
import com.daily.cetaring.data.remote.dto.RiceSampleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RiceApiService {
    @GET("rice/product")
    suspend fun getProduct(): RiceProductResponse

    @POST("rice/orders")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Body request: CreateRiceOrderRequest
    ): RiceOrderResponse

    @GET("rice/orders/me")
    suspend fun getMyOrders(@Header("Authorization") authorization: String): List<RiceOrderResponse>

    @POST("rice/samples")
    suspend fun createSample(
        @Header("Authorization") authorization: String,
        @Body request: CreateRiceSampleRequest
    ): RiceSampleResponse

    @GET("rice/samples/me")
    suspend fun getMySamples(@Header("Authorization") authorization: String): List<RiceSampleResponse>
}
