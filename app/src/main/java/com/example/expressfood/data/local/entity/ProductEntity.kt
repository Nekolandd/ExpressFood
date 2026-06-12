package com.example.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val ingredients: String,
    val estimatedTime: Int,
    val rating: Double,
    val imageUrl: String,
    val enabled: Boolean
)
