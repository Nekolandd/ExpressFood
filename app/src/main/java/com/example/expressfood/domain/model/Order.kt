package com.example.expressfood.domain.model

data class Order(
    val id: String,
    val userId: String,
    val userName: String = "",
    val createdAt: Long,
    val status: OrderStatus,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val synced: Boolean,
    val items: List<OrderItem>
)
