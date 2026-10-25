package com.example.turnos.data.repository

import com.example.turnos.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Usuario con sesión activa (en memoria mientras la app está abierta).
 */

class SessionRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun start(user: User) {
        _currentUser.value = user
    }

    fun end() {
        _currentUser.value = null
    }
}
