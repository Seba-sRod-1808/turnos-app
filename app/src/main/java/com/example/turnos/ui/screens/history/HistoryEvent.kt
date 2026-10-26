package com.example.turnos.ui.screens.history

import com.example.turnos.data.model.Appointment

sealed interface HistoryEvent {
    data class OnTabSelected(val tab: HistoryTab) : HistoryEvent

    // Cliente
    data class OnCancelClick(val appointment: Appointment) : HistoryEvent
    data object OnConfirmCancel : HistoryEvent
    data object OnDismissCancel : HistoryEvent

    // Negocio
    data class OnConfirmAppointment(val appointment: Appointment) : HistoryEvent
    data class OnMarkCompleted(val appointment: Appointment) : HistoryEvent
    data class OnMarkNoShow(val appointment: Appointment) : HistoryEvent

    data object OnMessageShown : HistoryEvent
    data object OnLogoutClick : HistoryEvent

    // Solo navegación: los resuelve la Route.
    data class OnRescheduleClick(val appointment: Appointment) : HistoryEvent
    data object OnBackClick : HistoryEvent
    data object OnGoToCatalog : HistoryEvent
}
