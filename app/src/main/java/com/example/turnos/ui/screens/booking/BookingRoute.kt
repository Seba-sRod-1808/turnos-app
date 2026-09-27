package com.example.turnos.ui.screens.booking

import androidx.compose.runtime.*
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BookingRoute(
    serviceId: String = FakeDataSource.services.first().id,
    onBack: () -> Unit = {},
    onViewAgenda: () -> Unit = {},
) {
    var state by remember(serviceId) {
        mutableStateOf(
            BookingUiState(
                service = FakeDataSource.services.firstOrNull { it.id == serviceId } ?: FakeDataSource.services.first(),
                barber = FakeDataSource.barbers.first(),
                businessName = FakeDataSource.business.name,
                businessAddress = FakeDataSource.business.address,
                days = FakeDataSource.bookingDays,
                slots = slotsForDay(0),
            )
        )
    }
    val scope = rememberCoroutineScope()

    BookingConfirmationScreen(
        state = state,
        onBack = onBack,
        onSelectDay = { i ->
            state = state.copy(selectedDayIndex = i, slots = slotsForDay(i), selectedSlot = null, errorMessage = null)
        },
        onSelectSlot = { state = state.copy(selectedSlot = it, errorMessage = null) },
        onConfirm = {
            scope.launch {
                state = state.copy(isConfirming = true)
                delay(800) // simula la llamada al servidor
                state = state.copy(isConfirming = false, isConfirmed = true)
            }
        },
        onViewAgenda = onViewAgenda,
    )
}

/**
 * Horarios fake por día: el sábado (índice 5) está lleno para poder
 * ver el estado "sin horarios" en la app.
 */
private fun slotsForDay(dayIndex: Int) =
    if (dayIndex == 5) FakeDataSource.previewSlots.map { it.copy(state = SlotState.OCCUPIED) }
    else FakeDataSource.previewSlots
