package com.example.turnos.ui.screens.business

sealed interface MyBusinessEvent {
    data object OnEditProfileClick : MyBusinessEvent
    data class OnProfileFormChange(val form: ProfileForm) : MyBusinessEvent
    data object OnSaveProfile : MyBusinessEvent
    data object OnDismissProfile : MyBusinessEvent
    data object OnLogoutClick : MyBusinessEvent
    data object OnMessageShown : MyBusinessEvent

    data class OnSectionClick(val section: AdminSection) : MyBusinessEvent
}
