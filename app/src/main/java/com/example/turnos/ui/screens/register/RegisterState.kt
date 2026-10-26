package com.example.turnos.ui.screens.register

import com.example.turnos.data.model.UserRole

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val role: UserRole = UserRole.CLIENT,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    /** Cuando no es null, la cuenta se creó y la vista navega al inicio de ese rol. */
    val registeredRole: UserRole? = null,
)
