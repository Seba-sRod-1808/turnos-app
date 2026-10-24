package com.example.turnos.ui.screens.services

import com.example.turnos.data.model.Service

sealed interface ServicesEvent {
    data object OnToggleSearch : ServicesEvent
    data class OnQueryChange(val query: String) : ServicesEvent
    data class OnToggleActive(val service: Service, val active: Boolean) : ServicesEvent
    data class OnEditClick(val service: Service) : ServicesEvent
    data object OnAddClick : ServicesEvent
    data class OnFormChange(val form: ServiceForm) : ServicesEvent
    data object OnSaveForm : ServicesEvent
    data object OnDeleteFromForm : ServicesEvent
    data object OnDismissForm : ServicesEvent
    data object OnMessageShown : ServicesEvent

    /** Solo navegación: lo resuelve la Route. */
    data object OnBackClick : ServicesEvent
}
