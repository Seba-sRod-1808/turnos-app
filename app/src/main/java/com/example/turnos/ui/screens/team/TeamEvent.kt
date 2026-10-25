package com.example.turnos.ui.screens.team

import com.example.turnos.data.model.Barber

sealed interface TeamEvent {
    data object OnAddBarberClick : TeamEvent
    data class OnFormChange(val form: BarberForm) : TeamEvent
    data object OnSaveBarber : TeamEvent
    data object OnDismissForm : TeamEvent
    data class OnManageClick(val barber: Barber) : TeamEvent
    data object OnMessageShown : TeamEvent

    data class OnViewAgendaClick(val barber: Barber) : TeamEvent
    data object OnBackClick : TeamEvent
}
