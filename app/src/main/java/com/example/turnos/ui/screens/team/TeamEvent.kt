package com.example.turnos.ui.screens.team

import com.example.turnos.data.model.Barber

sealed interface TeamEvent {
    data object OnAddBarberClick : TeamEvent
    data class OnFormChange(val form: BarberForm) : TeamEvent
    data object OnSaveBarber : TeamEvent
    data object OnDismissForm : TeamEvent
    /** "Gestionar": rota el estado Disponible → En servicio → Ausente. */
    data class OnManageClick(val barber: Barber) : TeamEvent
    data object OnMessageShown : TeamEvent

    // Solo navegación: los resuelve la Route.
    data class OnViewAgendaClick(val barber: Barber) : TeamEvent
    data object OnBackClick : TeamEvent
}
