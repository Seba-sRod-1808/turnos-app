package com.example.turnos.ui.screens.forgotpassword

import com.example.turnos.data.repository.AuthRepository

enum class RecoveryStep { USERNAME, SECURITY_QUESTION, NEW_PASSWORD, DONE }

data class ForgotPasswordState(
    val step: RecoveryStep = RecoveryStep.USERNAME,
    val username: String = "",
    val securityQuestion: String = "",
    val answer: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val attemptsLeft: Int = AuthRepository.MAX_RECOVERY_ATTEMPTS,
    val isLoading: Boolean = false,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val errorMessage: String? = null,
) {
    val isLocked: Boolean get() = attemptsLeft <= 0
}
