package com.example.turnos.ui.screens.availability

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AvailabilityRoute(onBack: () -> Unit = {}) {
    var state by remember {
        mutableStateOf(
            AvailabilityUiState(
                dayLabels = FakeDataSource.workDays,
                activeDays = listOf(true, true, true, true, true, false, false),
                blocks = FakeDataSource.timeBlocks,
                todaySlots = FakeDataSource.previewSlots,
            )
        )
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AvailabilityScreen(
        state = state,
        snackbarHostState = snackbar,
        onBack = onBack,
        onToggleDay = { i ->
            state = state.copy(activeDays = state.activeDays.mapIndexed { idx, on -> if (idx == i) !on else on })
        },
        onDeleteBlock = { b -> state = state.copy(blocks = state.blocks - b) },
        onAddBlock = {
            // Bloque de ejemplo; en la siguiente fase se elegirá con un TimePicker.
            val new = TimeBlock("t${System.currentTimeMillis()}", "07:00 PM", "08:00 PM")
            state = state.copy(blocks = state.blocks + new)
        },
        onToggleSlot = { i ->
            state = state.copy(
                todaySlots = state.todaySlots.mapIndexed { idx, s ->
                    if (idx != i) s
                    else when (s.state) {
                        SlotState.AVAILABLE -> s.copy(state = SlotState.BLOCKED)
                        SlotState.BLOCKED -> s.copy(state = SlotState.AVAILABLE)
                        SlotState.OCCUPIED -> s // ocupado por una cita: no editable
                    }
                }
            )
        },
        onSave = {
            scope.launch {
                state = state.copy(isSaving = true)
                delay(700) // guardado simulado
                state = state.copy(isSaving = false)
                snackbar.showSnackbar("Configuración guardada")
            }
        },
    )
}
