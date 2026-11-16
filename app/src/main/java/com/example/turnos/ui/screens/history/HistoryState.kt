package com.example.turnos.ui.screens.history

import com.example.turnos.data.model.Appointment

enum class HistoryTab { UPCOMING, PAST }

data class HistoryState(
    val isBusiness: Boolean = false,
    /** Nombre del barbero cuando el negocio abre la agenda de una sola persona. */
    val barberName: String? = null,
    val selectedTab: HistoryTab = HistoryTab.UPCOMING,
    /** Citas activas que todavía no empiezan, ordenadas por fecha. */
    val upcoming: List<Appointment> = emptyList(),
    /** Citas activas cuya hora ya pasó: el negocio debe marcarlas como completadas o no presentes. */
    val awaitingClose: List<Appointment> = emptyList(),
    /** Citas cerradas, más recientes primero. */
    val past: List<Appointment> = emptyList(),
    val appointmentToCancel: Appointment? = null,
    /** Cuando no es null, la vista abre la reserva para reprogramar esta cita. */
    val rescheduleTarget: Appointment? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val isLoggedOut: Boolean = false,
) {
    val title: String get() = barberName?.let { "Agenda de $it" } ?: "Historial de Citas"

    /** El cliente ve sus citas vencidas junto al historial; el negocio las tiene aparte para cerrarlas. */
    val pastForDisplay: List<Appointment>
        get() = if (isBusiness) past else (awaitingClose + past).sortedByDescending { it.startDateTime }
}
