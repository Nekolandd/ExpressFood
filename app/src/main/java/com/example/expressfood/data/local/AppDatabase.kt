package com.example.expressfood.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expressfood.data.local.dao.CartDao
import com.example.expressfood.data.local.dao.OrderDao
import com.example.expressfood.data.local.dao.ProductDao
import com.example.expressfood.data.local.entity.CartItemEntity
import com.example.expressfood.data.local.entity.OrderEntity
import com.example.expressfood.data.local.entity.OrderItemEntity
import com.example.expressfood.data.local.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
}
