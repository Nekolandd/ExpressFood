package com.example.expressfood.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expressfood.data.local.toDomain
import com.example.expressfood.data.repository.OrderRepository
import com.example.expressfood.data.repository.ProductRepository
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.domain.model.Product
import com.example.expressfood.util.ReportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AdminOrdersViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val statusFilter = MutableStateFlow<OrderStatus?>(null)
    private val clientFilter = MutableStateFlow("")
    private val dateFilter = MutableStateFlow("")

    val orders: StateFlow<List<Order>> = combine(
        orderRepository.observeRemoteOrders(),
        statusFilter,
        clientFilter,
        dateFilter
    ) { orders, status, client, date ->
        orders
            .let { list -> ReportHelper.filterByStatus(list, status) }
            .let { list ->
                if (client.isBlank()) list
                else list.filter {
                    it.userName.contains(client, ignoreCase = true) ||
                        it.userId.contains(client, ignoreCase = true)
                }
            }
            .let { list ->
                if (date.isBlank()) list
                else list.filter {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(it.createdAt)) == date
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStatusFilter(status: OrderStatus?) {
        statusFilter.value = status
    }

    fun setClientFilter(query: String) {
        clientFilter.value = query
    }

    fun setDateFilter(date: String) {
        dateFilter.value = date
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
        }
    }
}

class AdminReportViewModel(
    orderRepository: OrderRepository
) : ViewModel() {

    val dailySummaries: StateFlow<List<ReportHelper.DailySummary>> = orderRepository.observeRemoteOrders()
        .map { ReportHelper.groupOrdersByDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotal: StateFlow<Double> = orderRepository.observeRemoteOrders()
        .map { ReportHelper.monthlyAccumulated(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}

class AdminProductsViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.getAllProductsFlow()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProduct(product: Product) {
        viewModelScope.launch { productRepository.saveProduct(product) }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch { productRepository.deleteProduct(productId) }
    }

    fun toggleEnabled(product: Product) {
        viewModelScope.launch {
            productRepository.saveProduct(product.copy(enabled = !product.enabled))
        }
    }

    fun createEmptyProduct(): Product = Product(
        id = UUID.randomUUID().toString(),
        name = "",
        price = 0.0,
        ingredients = "",
        estimatedTime = 15,
        rating = 4.0,
        imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400",
        enabled = true
    )
}
