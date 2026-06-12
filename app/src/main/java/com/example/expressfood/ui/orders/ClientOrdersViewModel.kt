package com.example.expressfood.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expressfood.data.repository.OrderRepository
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.util.ReportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ClientOrdersViewModel(
    orderRepository: OrderRepository,
    userId: String
) : ViewModel() {

    private val statusFilter = MutableStateFlow<OrderStatus?>(null)

    val orders: StateFlow<List<Order>> = combine(
        orderRepository.getOrdersForUser(userId),
        statusFilter
    ) { orders, filter ->
        ReportHelper.filterByStatus(orders, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStatusFilter(status: OrderStatus?) {
        statusFilter.value = status
    }
}

class ClientReportViewModel(
    orderRepository: OrderRepository,
    userId: String
) : ViewModel() {

    val dailySummaries: StateFlow<List<ReportHelper.DailySummary>> = orderRepository.getOrdersForUser(userId)
        .map { ReportHelper.groupOrdersByDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotal: StateFlow<Double> = orderRepository.getOrdersForUser(userId)
        .map { ReportHelper.monthlyAccumulated(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
