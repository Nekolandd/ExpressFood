package com.example.expressfood.data.local

import com.example.expressfood.data.local.dao.CartItemWithProduct
import com.example.expressfood.data.local.entity.OrderEntity
import com.example.expressfood.data.local.entity.OrderItemEntity
import com.example.expressfood.data.local.entity.ProductEntity
import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderItem
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.domain.model.Product

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    price = price,
    ingredients = ingredients,
    estimatedTime = estimatedTime,
    rating = rating,
    imageUrl = imageUrl,
    enabled = enabled
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price,
    ingredients = ingredients,
    estimatedTime = estimatedTime,
    rating = rating,
    imageUrl = imageUrl,
    enabled = enabled
)

fun CartItemWithProduct.toDomain(): CartItem = CartItem(
    product = Product(
        id = id,
        name = name,
        price = price,
        ingredients = ingredients,
        estimatedTime = estimatedTime,
        rating = rating,
        imageUrl = imageUrl,
        enabled = enabled
    ),
    quantity = quantity
)

fun OrderEntity.toDomain(items: List<OrderItemEntity>): Order = Order(
    id = id,
    userId = userId,
    userName = userName,
    createdAt = createdAt,
    status = OrderStatus.fromString(status),
    subtotal = subtotal,
    tax = tax,
    total = total,
    synced = synced,
    items = items.map { it.toDomain() }
)

fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    userId = userId,
    userName = userName,
    createdAt = createdAt,
    status = status.name,
    subtotal = subtotal,
    tax = tax,
    total = total,
    synced = synced
)

fun OrderItem.toEntity(orderId: String): OrderItemEntity = OrderItemEntity(
    orderId = orderId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice
)
