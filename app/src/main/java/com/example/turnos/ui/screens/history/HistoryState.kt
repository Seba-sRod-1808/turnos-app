package com.example.turnos.ui.screens.history

import com.example.turnos.data.model.Appointment

enum class HistoryTab { UPCOMING, PAST }

data class HistoryState(
    val isBusiness: Boolean = false,
    val barberName: String? = null,
    val selectedTab: HistoryTab = HistoryTab.UPCOMING,
    val upcoming: List<Appointment> = emptyList(),
    val awaitingClose: List<Appointment> = emptyList(),
    val past: List<Appointment> = emptyList(),
    val appointmentToCancel: Appointment? = null,
    val rescheduleTarget: Appointment? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val isLoggedOut: Boolean = false,
) {
    val title: String get() = barberName?.let { "Agenda de $it" } ?: "Historial de Citas"

    val pastForDisplay: List<Appointment>
        get() = if (isBusiness) past else (awaitingClose + past).sortedByDescending { it.startDateTime }
}
