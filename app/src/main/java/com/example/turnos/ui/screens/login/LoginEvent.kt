package com.example.turnos.ui.screens.login

import com.example.turnos.data.model.UserRole

/** Acciones que el usuario puede ejecutar en el login. */
sealed interface LoginEvent {
    data class OnEmailChange(val value: String) : LoginEvent
    data class OnPasswordChange(val value: String) : LoginEvent
    data object OnTogglePasswordVisibility : LoginEvent
    data object OnLoginClick : LoginEvent
    data class OnUseDemoAccount(val role: UserRole) : LoginEvent
    data object OnForgotPasswordClick : LoginEvent
    data object OnGoogleClick : LoginEvent
    data object OnMessageShown : LoginEvent
    data object OnNavigationHandled : LoginEvent

    /** Solo navegación: lo resuelve la Route, no el ViewModel. */
    data object OnCreateAccountClick : LoginEvent
}
