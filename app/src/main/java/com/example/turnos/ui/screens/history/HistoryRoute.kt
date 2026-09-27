package com.example.turnos.ui.screens.history

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.launch

@Composable
fun HistoryRoute(
    isBusiness: Boolean = false,
    onBack: () -> Unit = {},
    onGoToCatalog: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var state by remember(isBusiness) {
        mutableStateOf(
            HistoryUiState(
                isBusiness = isBusiness,
                upcoming = FakeDataSource.upcoming,
                past = FakeDataSource.past,
            )
        )
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /** Mueve una cita de "próximas" a "pasadas" con el nuevo estado. */
    fun close(a: Appointment, status: AppointmentStatus) {
        state = state.copy(
            upcoming = state.upcoming - a,
            past = listOf(a.copy(status = status)) + state.past,
            appointmentToCancel = null,
        )
    }

    HistoryScreen(
        state = state,
        snackbarHostState = snackbar,
        onBack = onBack,
        onTabSelected = { state = state.copy(selectedTab = it) },
        onCancelClick = { state = state.copy(appointmentToCancel = it) },
        onConfirmCancel = { state.appointmentToCancel?.let { close(it, AppointmentStatus.CANCELLED) } },
        onDismissCancel = { state = state.copy(appointmentToCancel = null) },
        onRescheduleClick = { a -> scope.launch { snackbar.showSnackbar("Elige un nuevo horario para ${a.service}") } },
        onMarkCompleted = { close(it, AppointmentStatus.COMPLETED) },
        onMarkNoShow = { close(it, AppointmentStatus.NO_SHOW) },
        onGoToCatalog = onGoToCatalog,
        onLogout = onLogout,
    )
}
