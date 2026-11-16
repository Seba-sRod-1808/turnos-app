package com.example.turnos.ui.screens.login

import com.example.turnos.data.model.UserRole

sealed interface LoginDestination {
    data class Home(val role: UserRole) : LoginDestination
    data class FirstLoginSetup(val userId: Long) : LoginDestination
}

/** Estado completo de la pantalla de login en un momento dado. */
data class LoginState(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Cuando no es null, la vista navega y luego avisa con OnNavigationHandled. */
    val destination: LoginDestination? = null,
)
