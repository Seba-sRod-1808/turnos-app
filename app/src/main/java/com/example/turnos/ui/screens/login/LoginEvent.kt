package com.example.turnos.ui.screens.login

import com.example.turnos.data.model.UserRole

sealed interface LoginEvent {
    data class OnUsernameChange(val value: String) : LoginEvent
    data class OnPasswordChange(val value: String) : LoginEvent
    data object OnTogglePasswordVisibility : LoginEvent
    data object OnLoginClick : LoginEvent
    data class OnUseDemoAccount(val role: UserRole) : LoginEvent
    data object OnNavigationHandled : LoginEvent
    data object OnForgotPasswordClick : LoginEvent
}
