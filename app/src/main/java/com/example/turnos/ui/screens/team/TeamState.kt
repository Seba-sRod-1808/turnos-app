package com.example.turnos.ui.screens.team

import com.example.turnos.data.model.Barber
import com.example.turnos.data.model.BarberStatus

data class BarberForm(
    val name: String = "",
    val role: String = "Barbero",
) {
    val isValid: Boolean get() = name.isNotBlank() && role.isNotBlank()
}

data class TeamState(
    val barbers: List<Barber> = emptyList(),
    val dateLabel: String = "",
    val isLoading: Boolean = true,
    /** Formulario para agregar barbero; null = cerrado. */
    val form: BarberForm? = null,
    val userMessage: String? = null,
) {
    val availableCount: Int get() = barbers.count { it.status == BarberStatus.AVAILABLE }
    val appointmentsToday: Int get() = barbers.sumOf { it.appointmentsToday }
}
