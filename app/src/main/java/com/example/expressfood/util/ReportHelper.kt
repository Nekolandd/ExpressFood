package com.example.expressfood.util

import com.example.expressfood.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ReportHelper {

    data class DailySummary(
        val dateKey: String,
        val displayDate: String,
        val orderCount: Int,
        val totalAmount: Double
    )

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun groupOrdersByDate(orders: List<Order>): List<DailySummary> {
        return orders
            .groupBy { dayFormat.format(Date(it.createdAt)) }
            .map { (dateKey, dayOrders) ->
                DailySummary(
                    dateKey = dateKey,
                    displayDate = displayFormat.format(dayFormat.parse(dateKey) ?: Date()),
                    orderCount = dayOrders.size,
                    totalAmount = dayOrders.sumOf { it.total }
                )
            }
            .sortedByDescending { it.dateKey }
    }

    fun monthlyAccumulated(orders: List<Order>, monthKey: String? = null): Double {
        val targetMonth = monthKey ?: monthFormat.format(Date())
        return orders
            .filter { monthFormat.format(Date(it.createdAt)) == targetMonth }
            .sumOf { it.total }
    }

    fun currentMonthKey(): String = monthFormat.format(Date())

    fun filterByStatus(orders: List<Order>, status: com.example.expressfood.domain.model.OrderStatus?): List<Order> {
        if (status == null) return orders
        return orders.filter { it.status == status }
    }
}
