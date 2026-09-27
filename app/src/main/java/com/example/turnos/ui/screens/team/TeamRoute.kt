package com.example.turnos.ui.screens.team

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.launch

@Composable
fun TeamRoute(onBack: () -> Unit = {}) {
    val state = remember { TeamUiState(barbers = FakeDataSource.barbers, dateLabel = "Sábado, 15 de junio") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notify: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }

    TeamScreen(
        state = state,
        snackbarHostState = snackbar,
        onBack = onBack,
        onAddBarber = { notify("Invitar nuevo barbero") },
        onManage = { notify("Gestionar a ${it.name}") },
        onViewAgenda = { notify("Agenda de ${it.name}") },
    )
}
