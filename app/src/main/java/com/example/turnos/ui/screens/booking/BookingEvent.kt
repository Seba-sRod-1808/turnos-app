package com.example.turnos.ui.screens.booking

import java.time.LocalDate

sealed interface BookingEvent {
    data class OnSelectDay(val date: LocalDate) : BookingEvent
    data class OnSelectSlot(val startMinutes: Int) : BookingEvent
    data object OnConfirmClick : BookingEvent
    data object OnDismissError : BookingEvent

    // Solo navegación o acciones del sistema: los resuelve la Route.
    data object OnOpenMapsClick : BookingEvent
    data object OnBackClick : BookingEvent
    data object OnViewAgendaClick : BookingEvent
}
