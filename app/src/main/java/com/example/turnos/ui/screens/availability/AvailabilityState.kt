package com.example.turnos.ui.screens.availability

import com.example.turnos.data.model.Holiday
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.data.model.TimeSlot

data class BlockForm(
    val startMinutes: Int = 19 * 60,
    val endMinutes: Int = 20 * 60,
    val error: String? = null,
)

data class HolidaysUi(
    val isLoading: Boolean = true,
    val upcoming: List<Holiday> = emptyList(),
    val fromCache: Boolean = false,
    val error: String? = null,
)

data class AvailabilityState(
    val isLoading: Boolean = true,
    val dayLabels: List<String> = listOf("L", "M", "Mi", "J", "V", "S", "D"),
    val workDays: List<Boolean> = List(7) { false },
    val blocks: List<TimeBlock> = emptyList(),
    val todaySlots: List<TimeSlot> = emptyList(),
    val holidays: HolidaysUi = HolidaysUi(),
    val blockForm: BlockForm? = null,
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false,
    val userMessage: String? = null,
) {
    val validationError: String?
        get() = when {
            workDays.none { it } -> "Selecciona al menos un día laborable."
            blocks.isEmpty() -> "Agrega al menos un bloque de horario."
            else -> null
        }
    val canSave: Boolean get() = validationError == null && hasUnsavedChanges && !isSaving
}
