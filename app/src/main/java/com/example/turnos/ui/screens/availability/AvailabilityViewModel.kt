package com.example.turnos.ui.screens.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.Holiday
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AvailabilityRepository
import com.example.turnos.data.repository.HolidayRepository
import com.example.turnos.domain.BusinessRules
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Vista previa de hoy calculada con las mismas reglas que ve el cliente.
 * El bloqueo manual de un slot de hoy se guarda al instante; los ocupados por citas no se tocan.
 * Regla: un bloque debe terminar después de empezar y no traslaparse con otro.
 */

class AvailabilityViewModel(
    private val availabilityRepository: AvailabilityRepository,
    private val appointmentRepository: AppointmentRepository,
    private val holidayRepository: HolidayRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AvailabilityState())
    val state: StateFlow<AvailabilityState> = _state.asStateFlow()

    private val today: LocalDate = LocalDate.now()

    private var todayAppointments: List<Appointment> = emptyList()
    private var todayBlocked: Set<Int> = emptySet()

    init {
        loadSchedule()
        observeToday()
        loadHolidays()
    }

    fun onEvent(event: AvailabilityEvent) {
        when (event) {
            is AvailabilityEvent.OnToggleDay -> _state.update {
                it.copy(
                    workDays = it.workDays.mapIndexed { i, on -> if (i == event.index) !on else on },
                    hasUnsavedChanges = true,
                )
            }
            is AvailabilityEvent.OnDeleteBlock -> {
                _state.update { it.copy(blocks = it.blocks - event.block, hasUnsavedChanges = true) }
                refreshPreview()
            }
            AvailabilityEvent.OnAddBlockClick -> _state.update { it.copy(blockForm = BlockForm()) }
            is AvailabilityEvent.OnBlockFormChange -> _state.update { it.copy(blockForm = event.form.copy(error = null)) }
            AvailabilityEvent.OnConfirmBlock -> confirmBlock()
            AvailabilityEvent.OnDismissBlockForm -> _state.update { it.copy(blockForm = null) }
            is AvailabilityEvent.OnToggleSlot -> toggleSlot(event.startMinutes)
            AvailabilityEvent.OnSaveClick -> save()
            AvailabilityEvent.OnRetryHolidays -> loadHolidays()
            AvailabilityEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            AvailabilityEvent.OnBackClick -> Unit
        }
    }

    private fun loadSchedule() = viewModelScope.launch {
        val days = availabilityRepository.getWorkDays()
        val blocks = availabilityRepository.getBlocks()
        _state.update { it.copy(isLoading = false, workDays = days, blocks = blocks) }
        refreshPreview()
    }

    private fun observeToday() {
        combine(
            appointmentRepository.observeAll().map { all -> all.filter { it.date == today } },
            availabilityRepository.observeBlockedStarts(today),
        ) { appointments, blocked -> appointments to blocked }
            .onEach { (appointments, blocked) ->
                todayAppointments = appointments
                todayBlocked = blocked
                refreshPreview()
            }
            .launchIn(viewModelScope)
    }

    private fun refreshPreview() {
        val slots = BusinessRules.computeSlots(
            date = today,
            durationMin = BusinessRules.SLOT_STEP_MIN,
            blocks = _state.value.blocks,
            busy = todayAppointments,
            blockedStarts = todayBlocked,
            now = today.atStartOfDay(),
        )
        _state.update { it.copy(todaySlots = slots) }
    }

    private fun confirmBlock() {
        val form = _state.value.blockForm ?: return
        val error = when {
            form.endMinutes <= form.startMinutes -> "La hora final debe ser posterior a la inicial."
            _state.value.blocks.any { form.startMinutes < it.endMinutes && it.startMinutes < form.endMinutes } ->
                "Ese bloque se traslapa con otro existente."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(blockForm = form.copy(error = error)) }
            return
        }
        _state.update {
            it.copy(
                blocks = (it.blocks + TimeBlock(id = 0, startMinutes = form.startMinutes, endMinutes = form.endMinutes))
                    .sortedBy { b -> b.startMinutes },
                blockForm = null,
                hasUnsavedChanges = true,
            )
        }
        refreshPreview()
    }

    private fun toggleSlot(start: Int) {
        val slot = _state.value.todaySlots.firstOrNull { it.startMinutes == start } ?: return
        if (slot.state == SlotState.OCCUPIED) return
        viewModelScope.launch {
            availabilityRepository.setBlocked(today, start, blocked = slot.state == SlotState.AVAILABLE)
        }
    }

    private fun save() {
        val s = _state.value
        if (s.validationError != null) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            availabilityRepository.saveSchedule(s.workDays, s.blocks)
            _state.update { it.copy(isSaving = false, hasUnsavedChanges = false, userMessage = "Configuración guardada.") }
        }
    }

    private fun loadHolidays() = viewModelScope.launch {
        _state.update { it.copy(holidays = HolidaysUi(isLoading = true)) }
        val years = listOf(today.year, today.year + 1)
        val all = mutableListOf<Holiday>()
        var fromCache = false
        var error: String? = null
        for (year in years) {
            holidayRepository.getHolidays(year)
                .onSuccess { result ->
                    all += result.holidays
                    fromCache = fromCache || result.fromCache
                }
                .onFailure { error = "No se pudieron consultar los feriados. Revisa tu conexión." }
        }
        val upcoming = all.filter { !it.date.isBefore(today) }.sortedBy { it.date }.take(5)
        _state.update {
            it.copy(
                holidays = HolidaysUi(
                    isLoading = false,
                    upcoming = upcoming,
                    fromCache = fromCache,
                    error = if (upcoming.isEmpty()) error else null,
                )
            )
        }
    }
}
