package com.example.expressfood.util

import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderCalculatorTest {

    private val items = listOf(
        CartItem(Product("1", "Pizza", 10.0, "Queso", 20, 4.5, "url", true), 2),
        CartItem(Product("2", "Ensalada", 5.0, "Lechuga", 10, 4.0, "url", true), 1)
    )

    @Test
    fun calculateFromCart_returnsSubtotalTaxAndTotal() {
        val (subtotal, tax, total) = OrderCalculator.calculateFromCart(items)
        assertEquals(25.0, subtotal, 0.001)
        assertEquals(3.25, tax, 0.001)
        assertEquals(28.25, total, 0.001)
    }
}
