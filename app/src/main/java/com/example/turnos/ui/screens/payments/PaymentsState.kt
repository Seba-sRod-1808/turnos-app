package com.example.turnos.ui.screens.payments

import com.example.turnos.data.model.PaymentMethod

/**
 * Resumen de cobros del mes (tarjeta oscura del diseño), calculado de las citas completadas.
 * La variación contra el mes anterior es null si el mes anterior no tuvo cobros.
 */

data class RevenueSummary(
    val monthLabel: String,
    val total: Int,
    val deltaPct: Int?,
    val count: Int,
    val nextDeposit: Int,
)

data class PaymentsState(
    val summary: RevenueSummary? = null,
    val methods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null,
) {
    val enabledCount: Int get() = methods.count { it.enabled }
}
