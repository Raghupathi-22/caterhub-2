package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.RiceApiService
import com.daily.cetaring.data.remote.dto.CreateRiceOrderRequest
import com.daily.cetaring.data.remote.dto.CreateRiceSampleRequest
import com.daily.cetaring.data.remote.dto.RiceOrderResponse
import com.daily.cetaring.data.remote.dto.RiceProductResponse
import com.daily.cetaring.data.remote.dto.RiceSampleResponse
import com.daily.cetaring.diagnostics.ReleaseDiagnostics
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class RiceAuthenticationRequiredException(message: String) : IllegalStateException(message)

/** Raised on HTTP 409, e.g. when the server price no longer matches the total the customer confirmed. */
class RiceConflictException(message: String) : IllegalStateException(message)

class RiceRepository(
    private val riceApiService: RiceApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun getProduct(): RiceProductResponse {
        val product = call("rice product") { riceApiService.getProduct() }
        if (product.basePackPrice == null || product.basePackKg == null || product.minOrderKg == null) {
            ReleaseDiagnostics.error("CATERHUB_RICE_PRODUCT_INCOMPLETE")
            throw IllegalStateException("Village Farm Rice pricing is not available right now. Please try again later.")
        }
        return product
    }

    suspend fun createOrder(request: CreateRiceOrderRequest): RiceOrderResponse {
        val token = bearerToken()
        return call("rice order") { riceApiService.createOrder(token, request) }
    }

    suspend fun createSample(request: CreateRiceSampleRequest): RiceSampleResponse {
        val token = bearerToken()
        return call("sample request") { riceApiService.createSample(token, request) }
    }

    suspend fun getMyOrders(): List<RiceOrderResponse> {
        val token = bearerToken()
        return call("rice order") { riceApiService.getMyOrders(token) }
    }

    suspend fun getMySamples(): List<RiceSampleResponse> {
        val token = bearerToken()
        return call("sample request") { riceApiService.getMySamples(token) }
    }

    suspend fun isSignedIn(): Boolean = !authLocalDataSource.accessTokenFlow.first().isNullOrBlank()

    /** Name and mobile of the signed-in customer, used only to prefill forms. */
    suspend fun contactPrefill(): Pair<String?, String?> {
        val name = listOfNotNull(authLocalDataSource.firstNameFlow.first(), authLocalDataSource.lastNameFlow.first())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { null }
        return name to authLocalDataSource.phoneNumberFlow.first()
    }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) throw RiceAuthenticationRequiredException("Please sign in to continue.")
        return "Bearer $token"
    }

    private suspend fun <T> call(contextLabel: String, block: suspend () -> T): T = try {
        block()
    } catch (exception: Exception) {
        if (exception is kotlinx.coroutines.CancellationException) throw exception
        val statusCode = (exception as? HttpException)?.code()?.toString() ?: "NA"
        ReleaseDiagnostics.error(
            "CATERHUB_RICE_API_FAILURE context=${contextLabel.replace(' ', '_')} exception=${exception::class.java.simpleName} httpStatus=$statusCode"
        )
        val mapped = ApiErrorMapper.map(
            exception = exception,
            contextLabel = contextLabel,
            badRequestFallback = "Please check your details and try again.",
            defaultFallback = "Unable to complete your request. Please try again."
        )
        throw when {
            exception is HttpException && exception.code() == 401 ->
                RiceAuthenticationRequiredException("Your session has expired. Please sign in again.")
            exception is HttpException && exception.code() == 409 ->
                RiceConflictException(mapped.message ?: "This request could not be completed. Please review and try again.")
            // Parsing/reflection failures (e.g. JsonSyntaxException, ClassCastException) must not leak raw text.
            mapped === exception && exception !is HttpException && exception !is java.io.IOException ->
                IllegalStateException("We received an unexpected response from the server. Please try again.", exception)
            else -> mapped
        }
    }
}
