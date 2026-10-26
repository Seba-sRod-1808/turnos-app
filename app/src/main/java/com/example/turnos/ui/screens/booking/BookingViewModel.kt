package com.example.turnos.ui.screens.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.Service
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.repository.*
import com.example.turnos.domain.BusinessRules
import com.example.turnos.navigation.BookingDestination
import com.example.turnos.util.formatDayChip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class BookingViewModel(
    savedStateHandle: SavedStateHandle,
    private val serviceRepository: ServiceRepository,
    private val appointmentRepository: AppointmentRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val barberRepository: BarberRepository,
    private val holidayRepository: HolidayRepository,
    private val session: SessionRepository,
    settings: SettingsRepository,
) : ViewModel() {

    /** Argumentos type-safe de la navegación: solo llegan IDs. */
    private val args = savedStateHandle.toRoute<BookingDestination>()
    private val rescheduleId: Long? = args.rescheduleAppointmentId.takeIf { it > 0 }

    private val _state = MutableStateFlow(
        BookingState(
            businessName = settings.businessProfile.name,
            businessAddress = settings.businessProfile.address,
            isReschedule = rescheduleId != null,
        )
    )
    val state: StateFlow<BookingState> = _state.asStateFlow()

    private var service: Service? = null

    init {
        load()
    }

    fun onEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.OnSelectDay -> selectDay(event.date)
            is BookingEvent.OnSelectSlot -> _state.update { it.copy(selectedSlot = event.startMinutes, errorMessage = null) }
            BookingEvent.OnConfirmClick -> confirm()
            BookingEvent.OnDismissError -> _state.update { it.copy(errorMessage = null) }
            BookingEvent.OnBackClick, BookingEvent.OnViewAgendaClick -> Unit // navegación
        }
    }

    private fun load() = viewModelScope.launch {
        val loaded = serviceRepository.getById(args.serviceId)
        if (loaded == null) {
            _state.update { it.copy(isLoading = false, errorMessage = "El servicio ya no está disponible.") }
            return@launch
        }
        service = loaded

        // 1) Feriados desde internet (o desde Room si no hay conexión).
        val today = LocalDate.now()
        val lastDay = today.plusDays(BusinessRules.BOOKING_WINDOW_DAYS.toLong() - 1)
        val holidays = mutableMapOf<LocalDate, String>()
        var notice: String? = null
        for (year in setOf(today.year, lastDay.year)) {
            holidayRepository.getHolidays(year)
                .onSuccess { result ->
                    result.holidays.forEach { holidays[it.date] = it.name }
                    if (result.fromCache) notice = "Sin conexión: se usan los feriados guardados."
                }
                .onFailure { notice = "No se pudieron consultar los feriados (sin conexión)." }
        }

        // 2) Días laborables configurados por el negocio.
        val workDays = availabilityRepository.getWorkDays()
        val days = (0 until BusinessRules.BOOKING_WINDOW_DAYS).map { offset ->
            val date = today.plusDays(offset.toLong())
            DayOption(
                date = date,
                label = formatDayChip(date),
                isWorkDay = workDays[date.dayOfWeek.value - 1],
                holidayName = holidays[date],
            )
        }

        _state.update {
            it.copy(
                isLoading = false,
                service = loaded,
                barberName = barberRepository.firstAvailableName(),
                days = days,
                holidaysNotice = notice,
            )
        }
        days.firstOrNull { it.isOpen }?.let { selectDay(it.date) }
    }

    private fun selectDay(date: LocalDate) {
        val day = _state.value.days.firstOrNull { it.date == date } ?: return
        _state.update { it.copy(selectedDate = date, selectedSlot = null, slots = emptyList(), errorMessage = null) }
        if (!day.isOpen) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingSlots = true) }
            val slots = computeSlots(date)
            _state.update { it.copy(isLoadingSlots = false, slots = slots) }
        }
    }

    private suspend fun computeSlots(date: LocalDate) = BusinessRules.computeSlots(
        date = date,
        durationMin = service?.durationMin ?: BusinessRules.SLOT_STEP_MIN,
        blocks = availabilityRepository.getBlocks(),
        busy = appointmentRepository.getActiveOn(date),
        blockedStarts = availabilityRepository.getBlockedStarts(date),
        ignoreAppointmentId = rescheduleId,
    )

    private fun confirm() {
        val s = _state.value
        val date = s.selectedDate ?: return
        val start = s.selectedSlot ?: return
        val service = service ?: return
        val user = session.currentUser.value ?: run {
            _state.update { it.copy(errorMessage = "Tu sesión expiró. Vuelve a iniciar sesión.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isConfirming = true, errorMessage = null) }

            // Regla: el horario sigue libre (alguien pudo reservarlo mientras tanto).
            val fresh = computeSlots(date)
            if (fresh.none { it.startMinutes == start && it.state == SlotState.AVAILABLE }) {
                _state.update {
                    it.copy(isConfirming = false, slots = fresh, selectedSlot = null, errorMessage = "Ese horario acaba de ser reservado. Elige otro.")
                }
                return@launch
            }

            // Regla: el cliente no puede tener dos citas que se traslapen.
            val mine = appointmentRepository.observeForClient(user.id).first()
            if (BusinessRules.clientHasOverlap(mine, date, start, service.durationMin, ignoreAppointmentId = rescheduleId)) {
                _state.update { it.copy(isConfirming = false, errorMessage = "Ya tienes otra cita a esa hora.") }
                return@launch
            }

            if (rescheduleId != null) {
                appointmentRepository.reschedule(rescheduleId, date, start)
            } else {
                appointmentRepository.create(
                    Appointment(
                        id = 0,
                        serviceId = service.id,
                        serviceName = service.name,
                        barberName = s.barberName,
                        clientId = user.id,
                        clientName = user.name,
                        date = date,
                        startMinutes = start,
                        durationMin = service.durationMin,
                        price = service.price,
                        status = AppointmentStatus.CONFIRMED,
                    )
                )
            }
            _state.update { it.copy(isConfirming = false, isConfirmed = true) }
        }
    }
}
