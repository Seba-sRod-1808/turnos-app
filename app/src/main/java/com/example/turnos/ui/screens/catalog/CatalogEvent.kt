package com.example.turnos.ui.screens.catalog

sealed interface CatalogEvent {
    data object OnToggleSearch : CatalogEvent
    data class OnQueryChange(val query: String) : CatalogEvent
    data object OnRetry : CatalogEvent
    data object OnLogoutClick : CatalogEvent

    // Solo navegación: los resuelve la Route.
    data class OnReserveClick(val serviceId: Long) : CatalogEvent
    data object OnMyAppointmentsClick : CatalogEvent
}
