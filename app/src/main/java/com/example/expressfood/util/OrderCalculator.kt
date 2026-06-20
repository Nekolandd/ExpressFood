package com.example.expressfood.util

import com.example.expressfood.domain.model.CartItem

// calcula subtotal, impuesto del 13% y total del pedido.
object OrderCalculator {

    const val TAX_RATE = 0.13

    fun calculateSubtotal(items: List<CartItem>): Double =
        items.sumOf { it.lineTotal }

    fun calculateTax(subtotal: Double): Double =
        subtotal * TAX_RATE

    fun calculateTotal(subtotal: Double, tax: Double): Double =
        subtotal + tax

    fun calculateFromCart(items: List<CartItem>): Triple<Double, Double, Double> {
        val subtotal = calculateSubtotal(items)
        val tax = calculateTax(subtotal)
        val total = calculateTotal(subtotal, tax)
        return Triple(subtotal, tax, total)
    }
}
