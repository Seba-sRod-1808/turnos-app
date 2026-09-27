package com.example.turnos.ui.screens.payments

import androidx.compose.runtime.*
import com.example.turnos.data.source.FakeDataSource

@Composable
fun PaymentMethodsRoute(onBack: () -> Unit = {}) {
    var state by remember {
        mutableStateOf(
            PaymentsUiState(
                summary = RevenueSummary(monthLabel = "junio", total = 4860, deltaPct = 12, count = 34, nextDeposit = 1420),
                methods = FakeDataSource.paymentMethods,
            )
        )
    }

    PaymentMethodsScreen(
        state = state,
        onBack = onBack,
        onToggleMethod = { m, on ->
            state = state.copy(methods = state.methods.map { if (it.id == m.id) it.copy(enabled = on) else it })
        },
        onConfigureDeposit = { },
    )
}
