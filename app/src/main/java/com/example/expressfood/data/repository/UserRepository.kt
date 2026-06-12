package com.example.expressfood.data.repository

import com.example.expressfood.data.local.UserSessionStore
import com.example.expressfood.data.remote.FirestoreUserDataSource
import com.example.expressfood.domain.model.User
import com.example.expressfood.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val sessionStore: UserSessionStore,
    private val remote: FirestoreUserDataSource = FirestoreUserDataSource()
) {
    suspend fun ensureUserExists(userId: String, name: String, email: String): User =
        withContext(Dispatchers.IO) {
            try {
                val user = remote.ensureUserExists(userId, name, email)
                sessionStore.saveUser(user)
                user
            } catch (_: Exception) {
                sessionStore.getUser(userId)
                    ?: User(
                        id = userId,
                        name = name,
                        email = email,
                        role = UserRole.CLIENT
                    ).also { fallback ->
                        sessionStore.saveUser(fallback)
                        runCatching { remote.createUser(fallback) }
                    }
            }
        }

    suspend fun getUser(userId: String): User? =
        withContext(Dispatchers.IO) {
            try {
                remote.getUser(userId)?.also { sessionStore.saveUser(it) }
            } catch (_: Exception) {
                sessionStore.getUser(userId)
            }
        }
}
