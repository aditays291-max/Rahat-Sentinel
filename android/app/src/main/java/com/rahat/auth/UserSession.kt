package com.rahat.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class User(
    val id: String,
    val name: String,
    val rId: String,
    val isGuest: Boolean
)

class UserSession(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val id = prefs.getString("user_id", null)
        val name = prefs.getString("user_name", null)
        val rId = prefs.getString("r_id", "") ?: ""
        val isGuest = prefs.getBoolean("is_guest", false)

        if (id != null && name != null) {
            _currentUser.value = User(id, name, rId, isGuest)
        }
    }

    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    fun loginGuest() {
        val guestUser = User(
            id = "guest_${System.currentTimeMillis()}",
            name = "Guest User",
            rId = "GUEST",
            isGuest = true
        )
        saveSession(guestUser)
    }
    fun loginUser(id: String, name: String, rId: String = "") {
        val user = User(id = id, name = name, rId = rId, isGuest = false)
        saveSession(user)
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null  // Clear current user without auto-login
    }
    
    fun getUserName(): String? {
        return _currentUser.value?.name
    }

    fun getUserId(): String? {
        return _currentUser.value?.id
    }

    fun updateName(newName: String) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(name = newName)
            saveSession(updatedUser)
        }
    }

    private fun saveSession(user: User) {
        _currentUser.value = user
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_name", user.name)
            putString("r_id", user.rId)
            putBoolean("is_guest", user.isGuest)
            apply()
        }
    }
}
