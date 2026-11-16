package com.example.turnos.ui.screens.setupaccount

import com.example.turnos.data.model.UserRole
import com.example.turnos.data.repository.AuthRepository

data class SetupAccountState(
    val userName: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val questions: List<String> = AuthRepository.SECURITY_QUESTIONS,
    val selectedQuestion: String = AuthRepository.SECURITY_QUESTIONS.first(),
    val answer: String = "",
    val passwordError: String? = null,
    val confirmError: String? = null,
    val answerError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    /** Cuando no es null, la cuenta quedó lista y la vista navega al inicio de ese rol. */
    val completedRole: UserRole? = null,
)
