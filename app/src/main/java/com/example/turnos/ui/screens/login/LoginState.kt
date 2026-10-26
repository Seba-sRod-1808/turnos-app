package com.example.turnos.ui.screens.login

import com.example.turnos.data.model.UserRole

/** Estado completo de la pantalla de login en un momento dado. */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val errorMessage: String? = null,
    /** Mensaje temporal (snackbar). */
    val userMessage: String? = null,
    /** Cuando no es null, la vista navega al inicio de ese rol. */
    val loggedInRole: UserRole? = null,
)
