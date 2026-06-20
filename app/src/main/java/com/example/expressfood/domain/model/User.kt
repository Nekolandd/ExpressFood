package com.example.expressfood.domain.model

// roles que determinan si el usuario entra como cliente o administrador.
enum class UserRole {
    CLIENT,
    ADMIN;

    companion object {
        const val CLIENT_VALUE = "client"
        const val ADMIN_VALUE = "admin"

        fun fromString(value: String): UserRole =
            if (value == ADMIN_VALUE) ADMIN else CLIENT

        fun toFirestoreValue(role: UserRole): String =
            if (role == ADMIN) ADMIN_VALUE else CLIENT_VALUE
    }
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.CLIENT
)
