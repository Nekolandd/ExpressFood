package com.example.expressfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expressfood.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

data class CartItemWithProduct(
    val productId: String,
    val quantity: Int,
    val id: String,
    val name: String,
    val price: Double,
    val ingredients: String,
    val estimatedTime: Int,
    val rating: Double,
    val imageUrl: String,
    val enabled: Boolean
)

@Dao
interface CartDao {

    @Query(
        """
        SELECT c.productId, c.quantity,
               p.id, p.name, p.price, p.ingredients, p.estimatedTime, p.rating, p.imageUrl, p.enabled
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
        ORDER BY p.name ASC
        """
    )
    fun getCartWithProducts(): Flow<List<CartItemWithProduct>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    fun getTotalItemCount(): Flow<Int>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getByProductId(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
