package com.example.expressfood.domain.model

enum class OrderStatus {
    CREATED,
    PENDING,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED;

    companion object {
        fun fromString(value: String): OrderStatus =
            entries.find { it.name == value } ?: PENDING
    }
}
