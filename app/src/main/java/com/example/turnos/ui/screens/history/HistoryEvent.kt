package com.example.turnos.ui.screens.history

import com.example.turnos.data.model.Appointment

sealed interface HistoryEvent {
    data class OnTabSelected(val tab: HistoryTab) : HistoryEvent

    data class OnCancelClick(val appointment: Appointment) : HistoryEvent
    data object OnConfirmCancel : HistoryEvent
    data object OnDismissCancel : HistoryEvent
    data class OnRescheduleClick(val appointment: Appointment) : HistoryEvent
    data object OnNavigationHandled : HistoryEvent

    data class OnConfirmAppointment(val appointment: Appointment) : HistoryEvent
    data class OnMarkCompleted(val appointment: Appointment) : HistoryEvent
    data class OnMarkNoShow(val appointment: Appointment) : HistoryEvent

    data object OnMessageShown : HistoryEvent
    data object OnLogoutClick : HistoryEvent
    data object OnBackClick : HistoryEvent
    data object OnGoToCatalog : HistoryEvent
}
