package com.example.turnos.ui.screens.payments

import com.example.turnos.data.model.PaymentMethod

sealed interface PaymentsEvent {
    data class OnToggleMethod(val method: PaymentMethod, val enabled: Boolean) : PaymentsEvent
    data object OnConfigureDepositClick : PaymentsEvent
    data object OnMessageShown : PaymentsEvent

    /** Solo navegación: lo resuelve la Route. */
    data object OnBackClick : PaymentsEvent
}
