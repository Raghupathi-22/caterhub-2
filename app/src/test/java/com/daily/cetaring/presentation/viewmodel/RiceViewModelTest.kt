package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.data.remote.dto.CreateRiceOrderRequest
import com.daily.cetaring.data.remote.dto.CreateRiceSampleRequest
import com.daily.cetaring.data.remote.dto.RiceOrderResponse
import com.daily.cetaring.data.remote.dto.RiceProductResponse
import com.daily.cetaring.data.remote.dto.RiceSampleResponse
import com.daily.cetaring.data.repository.RiceAuthenticationRequiredException
import com.daily.cetaring.data.repository.RiceConflictException
import com.daily.cetaring.data.repository.RiceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RiceViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<RiceRepository>()
    private val product = RiceProductResponse(
        name = "Village Farm Rice",
        active = true,
        basePackKg = 50,
        basePackPrice = BigDecimal("5299.00"),
        minOrderKg = 50,
        orderStepKg = 1,
        maxOrderKg = 5000,
        sampleEnabled = true,
        sampleWeightGrams = 500,
        samplePrice = BigDecimal.ZERO
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getProduct() } returns product
        coEvery { repository.contactPrefill() } returns (null to null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun loadedViewModel(): RiceViewModel = RiceViewModel(repository).also { it.loadProduct() }

    private fun RiceViewModel.fillValidOrder(quantity: String = "75") = updateOrderForm {
        it.copy(
            customerName = "Asha",
            mobileNumber = "98765 43210",
            quantityText = quantity,
            deliveryAddress = "12 Main Road",
            area = "Anna Nagar",
            pincode = "600040"
        )
    }

    @Test
    fun productLoadPrefillsMinimumQuantity() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        assertEquals("50", viewModel.uiState.value.orderForm.quantityText)
        assertEquals(BigDecimal("5299.00"), viewModel.uiState.value.quote!!.total)
    }

    @Test
    fun productLoadFailureShowsErrorState() = runTest {
        coEvery { repository.getProduct() } throws IOException("offline")
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.productState is RiceProductState.Error)
    }

    @Test
    fun belowMinimumQuantityBlocksConfirmation() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.fillValidOrder(quantity = "40")
        viewModel.requestOrderConfirmation()
        assertFalse(viewModel.uiState.value.showOrderConfirmation)
        assertEquals("Minimum order quantity is 50 kg.", viewModel.uiState.value.orderErrors.quantity)
        coVerify(exactly = 0) { repository.createOrder(any()) }
    }

    @Test
    fun confirmedOrderSendsDisplayedTotalAndNormalizedMobile() = runTest {
        val request = slot<CreateRiceOrderRequest>()
        coEvery { repository.createOrder(capture(request)) } returns RiceOrderResponse(id = 1, orderNumber = "RO-1", totalAmount = BigDecimal("7948.50"))
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.fillValidOrder()
        viewModel.requestOrderConfirmation()
        assertTrue(viewModel.uiState.value.showOrderConfirmation)
        viewModel.confirmOrder()
        advanceUntilIdle()

        assertEquals(BigDecimal("7948.50"), request.captured.expectedTotalAmount)
        assertEquals("9876543210", request.captured.mobileNumber)
        assertEquals(75, request.captured.quantityKg)
        assertEquals("RO-1", viewModel.uiState.value.placedOrder?.orderNumber)
    }

    @Test
    fun retryAfterNetworkFailureReusesIdempotencyKey() = runTest {
        val keys = mutableListOf<String>()
        coEvery { repository.createOrder(any()) } answers {
            keys += firstArg<CreateRiceOrderRequest>().idempotencyKey
            if (keys.size == 1) throw IOException("timeout") else RiceOrderResponse(id = 1)
        }
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.fillValidOrder()
        viewModel.requestOrderConfirmation()
        viewModel.confirmOrder()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.orderError)

        viewModel.requestOrderConfirmation()
        viewModel.confirmOrder()
        advanceUntilIdle()

        assertEquals(2, keys.size)
        assertEquals(keys[0], keys[1])
        assertNotNull(viewModel.uiState.value.placedOrder)
    }

    @Test
    fun doubleTapSubmitsOnlyOnce() = runTest {
        coEvery { repository.createOrder(any()) } returns RiceOrderResponse(id = 1)
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.fillValidOrder()
        viewModel.requestOrderConfirmation()
        viewModel.confirmOrder()
        viewModel.confirmOrder()
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.createOrder(any()) }
    }

    @Test
    fun guestOrderRequestsAuthAndResumesAfterLogin() = runTest {
        var calls = 0
        coEvery { repository.createOrder(any()) } answers {
            calls++
            if (calls == 1) throw RiceAuthenticationRequiredException("Please sign in to continue.") else RiceOrderResponse(id = 9)
        }
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.fillValidOrder()
        viewModel.requestOrderConfirmation()
        viewModel.confirmOrder()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.authRequired)

        viewModel.onAuthRequiredHandled()
        viewModel.resumePendingAfterAuth()
        advanceUntilIdle()
        assertEquals(9L, viewModel.uiState.value.placedOrder?.id)
    }

    @Test
    fun priceConflictShowsMessageAndReloadsProduct() = runTest {
        coEvery { repository.createOrder(any()) } throws RiceConflictException("The rice price has changed.")
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.fillValidOrder()
        viewModel.requestOrderConfirmation()
        viewModel.confirmOrder()
        advanceUntilIdle()
        assertEquals("The rice price has changed.", viewModel.uiState.value.orderError)
        coVerify(exactly = 2) { repository.getProduct() }
    }

    @Test
    fun sampleRequestSendsNoPriceAndShowsConfirmation() = runTest {
        val request = slot<CreateRiceSampleRequest>()
        coEvery { repository.createSample(capture(request)) } returns RiceSampleResponse(
            id = 3, requestNumber = "RS-3", price = BigDecimal.ZERO, fulfillmentType = "PICKUP", status = "REQUESTED"
        )
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.updateSampleForm(name = "Asha", mobile = "9876543210")
        viewModel.submitSample()
        advanceUntilIdle()
        assertEquals("9876543210", request.captured.mobileNumber)
        assertEquals("RS-3", viewModel.uiState.value.sampleResult?.requestNumber)
    }

    @Test
    fun invalidSampleFormIsNotSubmitted() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.updateSampleForm(name = "", mobile = "123")
        viewModel.submitSample()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.sampleForm.nameError)
        assertNotNull(viewModel.uiState.value.sampleForm.mobileError)
        coVerify(exactly = 0) { repository.createSample(any()) }
    }

    @Test
    fun disabledSampleIsNotSubmitted() = runTest {
        coEvery { repository.getProduct() } returns product.copy(sampleEnabled = false)
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.updateSampleForm(name = "Asha", mobile = "9876543210")
        viewModel.submitSample()
        advanceUntilIdle()
        assertEquals("Free samples are not available right now.", viewModel.uiState.value.sampleError)
        assertNull(viewModel.uiState.value.sampleResult)
    }

    @Test
    fun historyShowsOrdersAndSamples() = runTest {
        coEvery { repository.isSignedIn() } returns true
        coEvery { repository.getMyOrders() } returns listOf(RiceOrderResponse(id = 1))
        coEvery { repository.getMySamples() } returns listOf(RiceSampleResponse(id = 2))
        val viewModel = loadedViewModel()
        viewModel.loadHistory()
        advanceUntilIdle()
        val history = viewModel.uiState.value.history
        assertTrue(history.loaded)
        assertEquals(1, history.orders.size)
        assertEquals(1, history.samples.size)
    }
}
