package com.example.expressfood.data.repository

import com.example.expressfood.data.local.AppDatabase
import com.example.expressfood.data.local.entity.CartItemEntity
import com.example.expressfood.data.local.toDomain
import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CartRepository(
    private val database: AppDatabase
) {
    private val cartDao get() = database.cartDao()

    fun getCartItems(): Flow<List<CartItem>> =
        cartDao.getCartWithProducts().map { items -> items.map { it.toDomain() } }

    fun getCartItemCount(): Flow<Int> = cartDao.getTotalItemCount()

    suspend fun addProduct(product: Product) = withContext(Dispatchers.IO) {
        val existing = cartDao.getByProductId(product.id)
        if (existing != null) {
            cartDao.updateQuantity(product.id, existing.quantity + 1)
        } else {
            cartDao.insert(CartItemEntity(productId = product.id, quantity = 1))
        }
    }

    suspend fun increaseQuantity(productId: String) = withContext(Dispatchers.IO) {
        val existing = cartDao.getByProductId(productId) ?: return@withContext
        cartDao.updateQuantity(productId, existing.quantity + 1)
    }

    suspend fun decreaseQuantity(productId: String) = withContext(Dispatchers.IO) {
        val existing = cartDao.getByProductId(productId) ?: return@withContext
        if (existing.quantity <= 1) {
            cartDao.deleteByProductId(productId)
        } else {
            cartDao.updateQuantity(productId, existing.quantity - 1)
        }
    }

    suspend fun removeItem(productId: String) = withContext(Dispatchers.IO) {
        cartDao.deleteByProductId(productId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clear()
    }

    suspend fun isEmpty(): Boolean = withContext(Dispatchers.IO) {
        getCartItems().first().isEmpty()
    }
}
