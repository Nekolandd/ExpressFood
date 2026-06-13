package com.example.expressfood.util

import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class ReportHelperTest {

    private fun order(total: Double, createdAt: Long) = Order(
        id = "o-$createdAt",
        userId = "u1",
        userName = "User",
        createdAt = createdAt,
        status = OrderStatus.PENDING,
        subtotal = total * 0.87,
        tax = total * 0.13,
        total = total,
        synced = true,
        items = emptyList()
    )

    @Test
    fun groupOrdersByDate_groupsOrdersAndSumsTotals() {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(2026, Calendar.JUNE, 12, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val timestamp = calendar.timeInMillis

        val summaries = ReportHelper.groupOrdersByDate(
            listOf(order(10.0, timestamp), order(15.0, timestamp))
        )

        assertEquals(1, summaries.size)
        assertEquals(2, summaries.first().orderCount)
        assertEquals(25.0, summaries.first().totalAmount, 0.001)
    }

    @Test
    fun monthlyAccumulated_sumsOrdersInMonth() {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(2026, Calendar.JUNE, 10, 10, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val timestamp = calendar.timeInMillis

        val total = ReportHelper.monthlyAccumulated(
            listOf(order(10.0, timestamp), order(5.0, timestamp)),
            "2026-06"
        )

        assertEquals(15.0, total, 0.001)
    }
}
