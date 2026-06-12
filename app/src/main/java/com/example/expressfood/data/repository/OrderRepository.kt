package com.example.expressfood.data.repository

import com.example.expressfood.data.local.AppDatabase
import com.example.expressfood.data.local.toDomain
import com.example.expressfood.data.local.toEntity
import com.example.expressfood.data.remote.FirestoreOrderDataSource
import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderItem
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.util.OrderCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.UUID

class OrderRepository(
    private val database: AppDatabase,
    private val remote: FirestoreOrderDataSource = FirestoreOrderDataSource()
) {
    private val orderDao get() = database.orderDao()

    fun getOrdersForUser(userId: String): Flow<List<Order>> =
        orderDao.getOrdersForUser(userId).map { list ->
            list.map { it.order.toDomain(it.items) }
        }

    fun getAllOrders(): Flow<List<Order>> =
        orderDao.getAllOrders().map { list ->
            list.map { it.order.toDomain(it.items) }
        }

    fun observeRemoteOrders(): Flow<List<Order>> = remote.observeAllOrders()

    /**
     * Escucha cambios en Firestore del usuario y los persiste en Room (offline-first).
     */
    fun observeAndSyncUserOrders(userId: String): Flow<List<Order>> =
        remote.observeOrdersForUser(userId)
            .onEach { orders -> syncRemoteOrdersToLocal(orders) }
            .flowOn(Dispatchers.IO)

    suspend fun createOrderFromCart(
        userId: String,
        userName: String,
        cartItems: List<CartItem>,
        isOnline: Boolean
    ): Order = withContext(Dispatchers.IO) {
        val (subtotal, tax, total) = OrderCalculator.calculateFromCart(cartItems)
        val orderId = UUID.randomUUID().toString()
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                quantity = cartItem.quantity,
                unitPrice = cartItem.product.price
            )
        }
        var synced = false
        val order = Order(
            id = orderId,
            userId = userId,
            userName = userName,
            createdAt = System.currentTimeMillis(),
            status = OrderStatus.PENDING,
            subtotal = subtotal,
            tax = tax,
            total = total,
            synced = synced,
            items = orderItems
        )
        orderDao.insertOrder(order.toEntity())
        orderDao.insertItems(orderItems.map { it.toEntity(orderId) })

        if (isOnline) {
            try {
                remote.uploadOrder(order)
                orderDao.markSynced(orderId)
                synced = true
            } catch (_: Exception) {
                synced = false
            }
        }

        order.copy(synced = synced)
    }

    suspend fun syncUnsyncedOrders(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val unsynced = orderDao.getUnsyncedOrders()
            var syncedCount = 0
            unsynced.forEach { entity ->
                val items = orderDao.getItemsForOrder(entity.id)
                val order = entity.toDomain(items)
                remote.uploadOrder(order)
                orderDao.markSynced(entity.id)
                syncedCount++
            }
            syncedCount
        }
    }

    suspend fun pullRemoteOrdersForUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val orders = remote.fetchOrdersForUser(userId)
            syncRemoteOrdersToLocal(orders)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) =
        withContext(Dispatchers.IO) {
            remote.updateOrderStatus(orderId, status)
            orderDao.updateStatus(orderId, status.name)
        }

    suspend fun syncRemoteOrderToLocal(order: Order) = withContext(Dispatchers.IO) {
        val syncedOrder = order.copy(synced = true)
        orderDao.insertOrder(syncedOrder.toEntity())
        orderDao.deleteItemsForOrder(order.id)
        orderDao.insertItems(order.items.map { it.toEntity(order.id) })
    }

    private suspend fun syncRemoteOrdersToLocal(orders: List<Order>) {
        orders.forEach { syncRemoteOrderToLocal(it) }
    }
}
