package com.example.expressfood.data.local

import android.content.Context
import com.example.expressfood.domain.model.User
import com.example.expressfood.domain.model.UserRole

class UserSessionStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit()
            .putString(key(user.id, "name"), user.name)
            .putString(key(user.id, "email"), user.email)
            .putString(key(user.id, "role"), UserRole.toFirestoreValue(user.role))
            .apply()
    }

    fun getUser(userId: String): User? {
        val name = prefs.getString(key(userId, "name"), null) ?: return null
        val email = prefs.getString(key(userId, "email"), null) ?: return null
        val role = UserRole.fromString(
            prefs.getString(key(userId, "role"), UserRole.CLIENT_VALUE) ?: UserRole.CLIENT_VALUE
        )
        return User(id = userId, name = name, email = email, role = role)
    }

    fun clearUser(userId: String) {
        prefs.edit()
            .remove(key(userId, "name"))
            .remove(key(userId, "email"))
            .remove(key(userId, "role"))
            .apply()
    }

    private fun key(userId: String, field: String) = "user_${userId}_$field"

    companion object {
        private const val PREFS_NAME = "expressfood_user_session"
    }
}
