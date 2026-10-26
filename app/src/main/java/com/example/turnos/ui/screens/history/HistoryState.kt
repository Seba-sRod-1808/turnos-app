package com.example.turnos.ui.screens.history

import com.example.turnos.data.model.Appointment

enum class HistoryTab { UPCOMING, PAST }

data class HistoryState(
    val isBusiness: Boolean = false,
    val selectedTab: HistoryTab = HistoryTab.UPCOMING,
    /** Citas activas (pendientes o confirmadas), ordenadas por fecha. */
    val upcoming: List<Appointment> = emptyList(),
    /** Citas cerradas (completadas, canceladas o no presentes), más recientes primero. */
    val past: List<Appointment> = emptyList(),
    /** Cita que espera confirmación para cancelarse (muestra el diálogo). */
    val appointmentToCancel: Appointment? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val isLoggedOut: Boolean = false,
)
