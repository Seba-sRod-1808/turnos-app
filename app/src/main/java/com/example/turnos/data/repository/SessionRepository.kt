package com.example.turnos.data.repository

import android.content.Context
import com.example.turnos.data.local.dao.UserDao
import com.example.turnos.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Usuario con sesión activa. El id se guarda en SharedPreferences para que la sesión
 * sobreviva al cierre de la app y a que Android mate el proceso en segundo plano.
 */

class SessionRepository(context: Context, private val userDao: UserDao) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun start(user: User) {
        prefs.edit().putLong(KEY_USER_ID, user.id).apply()
        _currentUser.value = user
    }

    suspend fun restore(): User? {
        val id = prefs.getLong(KEY_USER_ID, NO_USER)
        if (id == NO_USER) return null
        val user = userDao.getById(id)?.toModel()?.takeUnless { it.mustChangePassword }
        if (user == null) end() else _currentUser.value = user
        return user
    }

    fun end() {
        prefs.edit().remove(KEY_USER_ID).apply()
        _currentUser.value = null
    }

    private companion object {
        const val PREFS_NAME = "turnos_session"
        const val KEY_USER_ID = "user_id"
        const val NO_USER = -1L
    }
}
