package com.daily.cetaring.data.remote

import com.daily.cetaring.BuildConfig
import com.daily.cetaring.config.AppConfig
import com.daily.cetaring.data.local.AuthLocalDataSource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 20L
    private const val WRITE_TIMEOUT_SECONDS = 20L

    @Volatile
    private var authLocalDataSource: AuthLocalDataSource? = null

    fun initializeAuth(localDataSource: AuthLocalDataSource) {
        authLocalDataSource = localDataSource
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val localDataSource = authLocalDataSource
            ?: error("ApiClient.initializeAuth() must be called before using the API")

        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor(maxRetries = 1))
            .addInterceptor(loggingInterceptor)
            .authenticator(AuthTokenAuthenticator(localDataSource))
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val healthApiService: HealthApiService by lazy { retrofit.create(HealthApiService::class.java) }
    val workerApiService: WorkerApiService by lazy { retrofit.create(WorkerApiService::class.java) }
    val bookingApiService: BookingApiService by lazy { retrofit.create(BookingApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
}
