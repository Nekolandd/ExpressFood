package com.example.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val createdAt: Long,
    val status: String,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val synced: Boolean
)
