package com.example.expressfood.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expressfood.data.repository.CartRepository
import com.example.expressfood.data.repository.OrderRepository
import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.util.ConnectivityObserver
import com.example.expressfood.util.OrderCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// gestiona cantidades del carrito y crea la orden con impuestos incluidos.
class CartViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val userId: String,
    private val userName: String
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = cartRepository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtotal: StateFlow<Double> = cartRepository.getCartItems()
        .map { OrderCalculator.calculateSubtotal(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val tax: StateFlow<Double> = subtotal
        .map { OrderCalculator.calculateTax(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val total: StateFlow<Double> = combine(subtotal, tax) { sub, t ->
        OrderCalculator.calculateTotal(sub, t)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _events = MutableSharedFlow<CartEvent>()
    val events = _events.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    fun increaseQuantity(productId: String) {
        viewModelScope.launch { cartRepository.increaseQuantity(productId) }
    }

    fun decreaseQuantity(productId: String) {
        viewModelScope.launch { cartRepository.decreaseQuantity(productId) }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch { cartRepository.removeItem(productId) }
    }

    // Convierte el carrito en una orden; si no hay red, queda pendiente de sincronizar.
    fun processOrder() {
        viewModelScope.launch {
            val items = cartItems.first()
            if (items.isEmpty()) {
                _events.emit(CartEvent.Error("El carrito está vacío"))
                return@launch
            }
            _isProcessing.value = true
            try {
                val isOnline = connectivityObserver.isCurrentlyOnline()
                val order = orderRepository.createOrderFromCart(userId, userName, items, isOnline)
                cartRepository.clearCart()
                _events.emit(CartEvent.OrderCreated(order.synced))
            } catch (e: Exception) {
                _events.emit(CartEvent.Error(e.message ?: "Error al procesar la orden"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    sealed class CartEvent {
        data class OrderCreated(val synced: Boolean) : CartEvent()
        data class Error(val message: String) : CartEvent()
    }
}
