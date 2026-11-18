package com.example.turnos.ui.screens.booking

import com.example.turnos.data.model.Barber
import com.example.turnos.data.model.Service
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeSlot
import com.example.turnos.util.formatDateLong
import com.example.turnos.util.formatTime
import java.time.LocalDate

data class DayOption(
    val date: LocalDate,
    val label: String,
    val isWorkDay: Boolean,
    val holidayName: String? = null,
) {
    val isOpen: Boolean get() = isWorkDay && holidayName == null
    val closedReason: String? get() = when {
        holidayName != null -> "Feriado"
        !isWorkDay -> "Cerrado"
        else -> null
    }
}

data class BookingState(
    val isLoading: Boolean = true,
    val service: Service? = null,
    val barber: Barber? = null,
    val businessName: String = "",
    val businessAddress: String = "",
    val days: List<DayOption> = emptyList(),
    val selectedDate: LocalDate? = null,
    val slots: List<TimeSlot> = emptyList(),
    val isLoadingSlots: Boolean = false,
    val selectedSlot: Int? = null,
    val isReschedule: Boolean = false,
    val isConfirming: Boolean = false,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null,
    val holidaysNotice: String? = null,
) {
    val barberName: String get() = barber?.name ?: "Por asignar"
    val canConfirm: Boolean
        get() = selectedSlot != null &&
            selectedDay?.isOpen == true &&
            slots.any { it.startMinutes == selectedSlot && it.state == SlotState.AVAILABLE } &&
            !isConfirming &&
            !isConfirmed
    val isEditable: Boolean get() = !isConfirming && !isConfirmed

    val dateLabel: String
        get() = if (selectedDate != null && selectedSlot != null) "${formatDateLong(selectedDate)}, ${formatTime(selectedSlot)}"
        else "Selecciona un horario"

    val selectedDay: DayOption? get() = days.firstOrNull { it.date == selectedDate }
}
