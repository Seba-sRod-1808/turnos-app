package com.example.turnos.ui.screens.register

import com.example.turnos.data.model.UserRole

sealed interface RegisterEvent {
    data class OnNameChange(val value: String) : RegisterEvent
    data class OnEmailChange(val value: String) : RegisterEvent
    data class OnPasswordChange(val value: String) : RegisterEvent
    data class OnConfirmPasswordChange(val value: String) : RegisterEvent
    data object OnTogglePasswordVisibility : RegisterEvent
    data class OnRoleChange(val role: UserRole) : RegisterEvent
    data object OnRegisterClick : RegisterEvent
    data object OnNavigationHandled : RegisterEvent

    /** Solo navegación: lo resuelve la Route. */
    data object OnBackClick : RegisterEvent
}
