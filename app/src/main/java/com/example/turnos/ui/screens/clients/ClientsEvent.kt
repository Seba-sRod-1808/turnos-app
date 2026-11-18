package com.example.turnos.ui.screens.clients

import com.example.turnos.data.model.User

sealed interface ClientsEvent {
    data class OnQueryChange(val query: String) : ClientsEvent
    data object OnAddClick : ClientsEvent
    data class OnNameChange(val value: String) : ClientsEvent
    data class OnUsernameChange(val value: String) : ClientsEvent
    data class OnPhoneChange(val value: String) : ClientsEvent
    data object OnSaveClient : ClientsEvent
    data object OnDismissForm : ClientsEvent
    data class OnResetPasswordClick(val client: User) : ClientsEvent
    data object OnConfirmReset : ClientsEvent
    data object OnDismissReset : ClientsEvent
    data object OnCredentialsDismissed : ClientsEvent
    data object OnMessageShown : ClientsEvent
    data object OnBackClick : ClientsEvent
}
