package com.example.expressfood.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Double get() = product.price * quantity
}
