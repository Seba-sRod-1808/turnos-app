package com.example.turnos.ui.screens.business

sealed interface MyBusinessEvent {
    data object OnEditProfileClick : MyBusinessEvent
    data object OnLogoutClick : MyBusinessEvent
    data object OnMessageShown : MyBusinessEvent

    /** Solo navegación: lo resuelve la Route. */
    data class OnSectionClick(val section: AdminSection) : MyBusinessEvent
}
