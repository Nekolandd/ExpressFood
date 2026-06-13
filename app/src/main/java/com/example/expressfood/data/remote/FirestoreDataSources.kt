package com.example.expressfood.data.remote

import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderItem
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.domain.model.Product
import com.example.expressfood.domain.model.User
import com.example.expressfood.domain.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection get() = firestore.collection("users")

    suspend fun getUser(userId: String): User? {
        getUserFromSource(userId, Source.CACHE)?.let { return it }
        return try {
            getUserFromSource(userId, Source.SERVER)
        } catch (_: Exception) {
            getUserFromSource(userId, Source.CACHE)
        }
    }

    suspend fun createUser(user: User) {
        usersCollection.document(user.id).set(
            mapOf(
                "name" to user.name,
                "email" to user.email,
                "role" to UserRole.toFirestoreValue(user.role)
            )
        ).await()
    }

    suspend fun ensureUserExists(
        userId: String,
        name: String,
        email: String
    ): User {
        val existing = try {
            getUserFromSource(userId, Source.SERVER)
        } catch (_: Exception) {
            getUserFromSource(userId, Source.CACHE)
        }
        if (existing != null) return existing

        val newUser = User(
            id = userId,
            name = name,
            email = email,
            role = UserRole.CLIENT
        )
        try {
            createUser(newUser)
        } catch (_: Exception) {
            // Firestore encola escrituras offline; si falla, el repositorio usa caché local.
        }
        return newUser
    }

    private suspend fun getUserFromSource(userId: String, source: Source): User? {
        val snapshot = usersCollection.document(userId).get(source).await()
        if (!snapshot.exists()) return null
        return snapshot.toUser()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        if (!exists()) return null
        return User(
            id = id,
            name = getString("name").orEmpty(),
            email = getString("email").orEmpty(),
            role = UserRole.fromString(getString("role") ?: UserRole.CLIENT_VALUE)
        )
    }
}

class FirestoreProductDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val productsCollection get() = firestore.collection("products")

    suspend fun fetchProducts(): List<Product> {
        val snapshot = try {
            productsCollection.get(Source.SERVER).await()
        } catch (_: Exception) {
            productsCollection.get(Source.CACHE).await()
        }
        return snapshot.documents.mapNotNull { it.toProduct() }
    }

    suspend fun seedProductsIfEmpty(): List<Product> {
        val existing = fetchProducts()
        if (existing.isNotEmpty()) return existing

        ProductSeedData.defaultProducts.forEach { product ->
            saveProduct(product)
        }
        return ProductSeedData.defaultProducts
    }

    suspend fun saveProduct(product: Product) {
        productsCollection.document(product.id).set(product.toMap()).await()
    }

    suspend fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(): Product? {
        if (!exists()) return null
        return Product(
            id = id,
            name = getString("name").orEmpty(),
            price = getDouble("price") ?: 0.0,
            ingredients = getString("ingredients").orEmpty(),
            estimatedTime = getLong("estimatedTime")?.toInt() ?: 0,
            rating = getDouble("rating") ?: 0.0,
            imageUrl = getString("imageUrl").orEmpty(),
            enabled = getBoolean("enabled") ?: true
        )
    }

    private fun Product.toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "price" to price,
        "ingredients" to ingredients,
        "estimatedTime" to estimatedTime,
        "rating" to rating,
        "imageUrl" to imageUrl,
        "enabled" to enabled
    )
}

class FirestoreOrderDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ordersCollection get() = firestore.collection("orders")

    suspend fun uploadOrder(order: Order) {
        val data = mutableMapOf<String, Any>(
            "userId" to order.userId,
            "userName" to order.userName,
            "createdAt" to order.createdAt,
            "status" to order.status.name,
            "subtotal" to order.subtotal,
            "tax" to order.tax,
            "total" to order.total,
            "items" to order.items.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitPrice
                )
            }
        )
        ordersCollection.document(order.id).set(data).await()
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        ordersCollection.document(orderId)
            .update("status", status.name)
            .await()
    }

    fun observeAllOrders(): Flow<List<Order>> = callbackFlow {
        val registration: ListenerRegistration = ordersCollection
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toOrder() }.orEmpty()
                trySend(orders)
            }
        awaitClose { registration.remove() }
    }

    fun observeOrdersForUser(userId: String): Flow<List<Order>> = callbackFlow {
        val registration: ListenerRegistration = ordersCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents
                    ?.mapNotNull { it.toOrder() }
                    ?.sortedByDescending { it.createdAt }
                    .orEmpty()
                trySend(orders)
            }
        awaitClose { registration.remove() }
    }

    suspend fun fetchOrdersForUser(userId: String): List<Order> {
        val snapshot = try {
            ordersCollection.whereEqualTo("userId", userId).get(Source.SERVER).await()
        } catch (_: Exception) {
            ordersCollection.whereEqualTo("userId", userId).get(Source.CACHE).await()
        }
        return snapshot.documents
            .mapNotNull { it.toOrder() }
            .sortedByDescending { it.createdAt }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrder(): Order? {
        if (!exists()) return null
        @Suppress("UNCHECKED_CAST")
        val itemsData = get("items") as? List<Map<String, Any>> ?: emptyList()
        val items = itemsData.map { map ->
            OrderItem(
                productId = map["productId"] as? String ?: "",
                productName = map["productName"] as? String ?: "",
                quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                unitPrice = map["unitPrice"] as? Double ?: 0.0
            )
        }
        return Order(
            id = id,
            userId = getString("userId").orEmpty(),
            userName = getString("userName").orEmpty(),
            createdAt = getLong("createdAt") ?: 0L,
            status = OrderStatus.fromString(getString("status") ?: OrderStatus.PENDING.name),
            subtotal = getDouble("subtotal") ?: 0.0,
            tax = getDouble("tax") ?: 0.0,
            total = getDouble("total") ?: 0.0,
            synced = true,
            items = items
        )
    }
}
