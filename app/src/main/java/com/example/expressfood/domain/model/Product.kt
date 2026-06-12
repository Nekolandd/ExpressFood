package com.example.expressfood.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val ingredients: String,
    val estimatedTime: Int,
    val rating: Double,
    val imageUrl: String,
    val enabled: Boolean = true
)
