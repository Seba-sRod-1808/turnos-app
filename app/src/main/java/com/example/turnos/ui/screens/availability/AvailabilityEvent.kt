package com.example.turnos.ui.screens.availability

import com.example.turnos.data.model.TimeBlock

sealed interface AvailabilityEvent {
    data class OnToggleDay(val index: Int) : AvailabilityEvent
    data class OnDeleteBlock(val block: TimeBlock) : AvailabilityEvent
    data object OnAddBlockClick : AvailabilityEvent
    data class OnBlockFormChange(val form: BlockForm) : AvailabilityEvent
    data object OnConfirmBlock : AvailabilityEvent
    data object OnDismissBlockForm : AvailabilityEvent
    data class OnToggleSlot(val startMinutes: Int) : AvailabilityEvent
    data object OnSaveClick : AvailabilityEvent
    data object OnRetryHolidays : AvailabilityEvent
    data object OnMessageShown : AvailabilityEvent

    /** Solo navegación: lo resuelve la Route. */
    data object OnBackClick : AvailabilityEvent
}
