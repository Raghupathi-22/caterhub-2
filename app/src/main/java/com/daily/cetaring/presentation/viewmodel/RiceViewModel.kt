package com.daily.cetaring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.cetaring.data.remote.dto.CreateRiceOrderRequest
import com.daily.cetaring.data.remote.dto.CreateRiceSampleRequest
import com.daily.cetaring.data.remote.dto.RiceOrderResponse
import com.daily.cetaring.data.remote.dto.RiceProductResponse
import com.daily.cetaring.data.remote.dto.RiceSampleResponse
import com.daily.cetaring.data.repository.RiceAuthenticationRequiredException
import com.daily.cetaring.data.repository.RiceConflictException
import com.daily.cetaring.data.repository.RiceRepository
import com.daily.cetaring.domain.rice.RiceOrderRules
import com.daily.cetaring.domain.rice.RiceQuote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface RiceProductState {
    data object Loading : RiceProductState
    data class Loaded(val product: RiceProductResponse) : RiceProductState
    data class Error(val message: String) : RiceProductState
}

enum class RicePendingAction { ORDER, SAMPLE }

data class RiceOrderForm(
    val customerName: String = "",
    val mobileNumber: String = "",
    val quantityText: String = "",
    val deliveryAddress: String = "",
    val area: String = "",
    val pincode: String = "",
    val preferredDeliveryDate: String? = null,
    val preferredDeliverySlot: String = "ANYTIME",
    val deliveryNotes: String = ""
) {
    val quantityKg: Int? get() = quantityText.trim().toIntOrNull()
}

data class RiceOrderFormErrors(
    val customerName: String? = null,
    val mobileNumber: String? = null,
    val quantity: String? = null,
    val deliveryAddress: String? = null,
    val area: String? = null,
    val pincode: String? = null
) {
    val hasErrors: Boolean get() = listOf(customerName, mobileNumber, quantity, deliveryAddress, area, pincode).any { it != null }
}

data class RiceSampleForm(
    val customerName: String = "",
    val mobileNumber: String = "",
    val nameError: String? = null,
    val mobileError: String? = null
)

data class RiceHistoryState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val orders: List<RiceOrderResponse> = emptyList(),
    val samples: List<RiceSampleResponse> = emptyList(),
    val error: String? = null
)

data class RiceUiState(
    val productState: RiceProductState = RiceProductState.Loading,
    val orderForm: RiceOrderForm = RiceOrderForm(),
    val orderErrors: RiceOrderFormErrors = RiceOrderFormErrors(),
    val showOrderConfirmation: Boolean = false,
    val orderSubmitting: Boolean = false,
    val orderError: String? = null,
    val placedOrder: RiceOrderResponse? = null,
    val sampleForm: RiceSampleForm = RiceSampleForm(),
    val sampleSubmitting: Boolean = false,
    val sampleError: String? = null,
    val sampleResult: RiceSampleResponse? = null,
    val history: RiceHistoryState = RiceHistoryState(),
    val authRequired: Boolean = false
) {
    val product: RiceProductResponse? get() = (productState as? RiceProductState.Loaded)?.product

    val quote: RiceQuote?
        get() {
            val product = product ?: return null
            val quantity = orderForm.quantityKg ?: return null
            return RiceOrderRules.quote(product, quantity)
        }
}

class RiceViewModel(private val repository: RiceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RiceUiState())
    val uiState: StateFlow<RiceUiState> = _uiState.asStateFlow()

    private var pendingAction: RicePendingAction? = null
    // Kept until the server confirms success so retries after timeouts never create duplicates.
    private var orderIdempotencyKey: String? = null
    private var sampleIdempotencyKey: String? = null
    private var prefilled = false

    fun loadProduct(force: Boolean = false) {
        val current = _uiState.value.productState
        if (!force && current is RiceProductState.Loaded) return
        _uiState.update { it.copy(productState = RiceProductState.Loading) }
        viewModelScope.launch {
            try {
                val product = repository.getProduct()
                _uiState.update { state ->
                    val form = state.orderForm
                    val quantity = form.quantityKg
                    val adjusted = if (quantity == null || (product.minOrderKg != null && quantity < product.minOrderKg)) {
                        form.copy(quantityText = product.minOrderKg?.toString().orEmpty())
                    } else form
                    state.copy(productState = RiceProductState.Loaded(product), orderForm = adjusted)
                }
                prefillContact()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(productState = RiceProductState.Error(exception.message ?: "Unable to load Village Farm Rice details."))
                }
            }
        }
    }

    private suspend fun prefillContact() {
        if (prefilled) return
        prefilled = true
        val (name, phone) = runCatching { repository.contactPrefill() }.getOrNull() ?: return
        val mobile = phone?.let { RiceOrderRules.normalizeMobile(it) }.orEmpty()
        _uiState.update { state ->
            state.copy(
                orderForm = state.orderForm.copy(
                    customerName = state.orderForm.customerName.ifBlank { name.orEmpty() },
                    mobileNumber = state.orderForm.mobileNumber.ifBlank { mobile }
                ),
                sampleForm = state.sampleForm.copy(
                    customerName = state.sampleForm.customerName.ifBlank { name.orEmpty() },
                    mobileNumber = state.sampleForm.mobileNumber.ifBlank { mobile }
                )
            )
        }
    }

    fun updateOrderForm(transform: (RiceOrderForm) -> RiceOrderForm) {
        _uiState.update { it.copy(orderForm = transform(it.orderForm), orderErrors = RiceOrderFormErrors(), orderError = null) }
    }

    fun incrementQuantity() {
        val product = _uiState.value.product ?: return
        val current = _uiState.value.orderForm.quantityKg ?: product.minOrderKg ?: return
        updateOrderForm { it.copy(quantityText = RiceOrderRules.increment(product, current).toString()) }
    }

    fun decrementQuantity() {
        val product = _uiState.value.product ?: return
        val current = _uiState.value.orderForm.quantityKg ?: product.minOrderKg ?: return
        updateOrderForm { it.copy(quantityText = RiceOrderRules.decrement(product, current).toString()) }
    }

    /** Validates the form and, when valid, shows the final-price confirmation. */
    fun requestOrderConfirmation() {
        val state = _uiState.value
        val product = state.product ?: return
        if (!RiceOrderRules.isOrderable(product)) {
            _uiState.update { it.copy(orderError = "Village Farm Rice ordering is currently unavailable.") }
            return
        }
        val errors = validateOrder(product, state.orderForm)
        _uiState.update { it.copy(orderErrors = errors, showOrderConfirmation = !errors.hasErrors && it.quote != null) }
    }

    fun dismissOrderConfirmation() {
        _uiState.update { it.copy(showOrderConfirmation = false) }
    }

    fun confirmOrder() {
        _uiState.update { it.copy(showOrderConfirmation = false) }
        submitOrder()
    }

    private fun submitOrder() {
        val state = _uiState.value
        if (state.orderSubmitting) return
        val product = state.product ?: return
        val form = state.orderForm
        val errors = validateOrder(product, form)
        val quote = state.quote
        if (errors.hasErrors || quote == null) {
            _uiState.update { it.copy(orderErrors = errors) }
            return
        }
        val key = orderIdempotencyKey ?: newIdempotencyKey().also { orderIdempotencyKey = it }
        _uiState.update { it.copy(orderSubmitting = true, orderError = null) }
        viewModelScope.launch {
            try {
                val order = repository.createOrder(
                    CreateRiceOrderRequest(
                        customerName = form.customerName.trim(),
                        mobileNumber = RiceOrderRules.normalizeMobile(form.mobileNumber)!!,
                        quantityKg = quote.quantityKg,
                        deliveryAddress = form.deliveryAddress.trim(),
                        area = form.area.trim(),
                        pincode = form.pincode.trim(),
                        preferredDeliveryDate = form.preferredDeliveryDate,
                        preferredDeliverySlot = form.preferredDeliverySlot,
                        deliveryNotes = form.deliveryNotes.trim().ifBlank { null },
                        expectedTotalAmount = quote.total,
                        idempotencyKey = key
                    )
                )
                orderIdempotencyKey = null
                _uiState.update { it.copy(orderSubmitting = false, placedOrder = order, history = it.history.copy(loaded = false)) }
            } catch (exception: RiceAuthenticationRequiredException) {
                pendingAction = RicePendingAction.ORDER
                _uiState.update { it.copy(orderSubmitting = false, authRequired = true) }
            } catch (exception: RiceConflictException) {
                _uiState.update { it.copy(orderSubmitting = false, orderError = exception.message) }
                loadProduct(force = true)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(orderSubmitting = false, orderError = exception.message ?: "Unable to place your order. Please try again.")
                }
            }
        }
    }

    fun startNewOrder() {
        orderIdempotencyKey = null
        _uiState.update { state ->
            state.copy(
                placedOrder = null,
                orderError = null,
                orderErrors = RiceOrderFormErrors(),
                orderForm = state.orderForm.copy(
                    quantityText = state.product?.minOrderKg?.toString().orEmpty(),
                    preferredDeliveryDate = null,
                    deliveryNotes = ""
                )
            )
        }
    }

    fun updateSampleForm(name: String? = null, mobile: String? = null) {
        _uiState.update { state ->
            state.copy(
                sampleForm = state.sampleForm.copy(
                    customerName = name ?: state.sampleForm.customerName,
                    mobileNumber = mobile ?: state.sampleForm.mobileNumber,
                    nameError = null,
                    mobileError = null
                ),
                sampleError = null
            )
        }
    }

    fun submitSample() {
        val state = _uiState.value
        if (state.sampleSubmitting) return
        val product = state.product ?: return
        if (product.sampleEnabled != true || product.active != true) {
            _uiState.update { it.copy(sampleError = "Free samples are not available right now.") }
            return
        }
        val form = state.sampleForm
        val nameError = if (form.customerName.isBlank()) "Enter your name." else null
        val mobile = RiceOrderRules.normalizeMobile(form.mobileNumber)
        val mobileError = if (mobile == null) "Enter a valid 10-digit mobile number." else null
        if (nameError != null || mobileError != null) {
            _uiState.update { it.copy(sampleForm = form.copy(nameError = nameError, mobileError = mobileError)) }
            return
        }
        val key = sampleIdempotencyKey ?: newIdempotencyKey().also { sampleIdempotencyKey = it }
        _uiState.update { it.copy(sampleSubmitting = true, sampleError = null) }
        viewModelScope.launch {
            try {
                val sample = repository.createSample(
                    CreateRiceSampleRequest(customerName = form.customerName.trim(), mobileNumber = mobile!!, idempotencyKey = key)
                )
                sampleIdempotencyKey = null
                _uiState.update { it.copy(sampleSubmitting = false, sampleResult = sample, history = it.history.copy(loaded = false)) }
            } catch (exception: RiceAuthenticationRequiredException) {
                pendingAction = RicePendingAction.SAMPLE
                _uiState.update { it.copy(sampleSubmitting = false, authRequired = true) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(sampleSubmitting = false, sampleError = exception.message ?: "Unable to request a sample. Please try again.")
                }
            }
        }
    }

    fun clearSampleResult() {
        _uiState.update { it.copy(sampleResult = null) }
    }

    fun onAuthRequiredHandled() {
        _uiState.update { it.copy(authRequired = false) }
    }

    fun resumePendingAfterAuth() {
        val action = pendingAction ?: return
        pendingAction = null
        prefilled = false
        viewModelScope.launch { prefillContact() }
        when (action) {
            RicePendingAction.ORDER -> submitOrder()
            RicePendingAction.SAMPLE -> submitSample()
        }
    }

    fun clearPendingAfterAuth() {
        pendingAction = null
    }

    fun showError(message: String) {
        _uiState.update { it.copy(orderError = message, sampleError = message) }
    }

    fun loadHistory(force: Boolean = false) {
        val history = _uiState.value.history
        if (history.loading || (history.loaded && !force)) return
        _uiState.update { it.copy(history = it.history.copy(loading = true, error = null)) }
        viewModelScope.launch {
            try {
                if (!repository.isSignedIn()) {
                    _uiState.update { it.copy(history = RiceHistoryState(loaded = true)) }
                    return@launch
                }
                val orders = repository.getMyOrders()
                val samples = repository.getMySamples()
                _uiState.update { it.copy(history = RiceHistoryState(loaded = true, orders = orders, samples = samples)) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(history = it.history.copy(loading = false, error = exception.message ?: "Unable to load your rice orders."))
                }
            }
        }
    }

    private fun validateOrder(product: RiceProductResponse, form: RiceOrderForm) = RiceOrderFormErrors(
        customerName = if (form.customerName.isBlank()) "Enter your name." else null,
        mobileNumber = if (RiceOrderRules.normalizeMobile(form.mobileNumber) == null) "Enter a valid 10-digit mobile number." else null,
        quantity = RiceOrderRules.validateQuantity(product, form.quantityKg),
        deliveryAddress = if (form.deliveryAddress.isBlank()) "Enter the delivery address." else null,
        area = if (form.area.isBlank()) "Enter your area or locality." else null,
        pincode = if (!RiceOrderRules.isValidPincode(form.pincode.trim())) "Enter a valid 6-digit pincode." else null
    )

    private fun newIdempotencyKey(): String = UUID.randomUUID().toString()
}
