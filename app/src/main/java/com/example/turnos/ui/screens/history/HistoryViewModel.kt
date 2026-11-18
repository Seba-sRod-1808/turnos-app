package com.example.turnos.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.BarberRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.domain.BusinessRules
import com.example.turnos.navigation.HistoryDestination
import com.example.turnos.notifications.AppointmentAlerts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Las citas se clasifican contra la hora actual, que se vuelve a leer cada minuto:
 * así una cita pasa sola de "Próximas" a "Pendientes de cerrar" cuando llega su hora.
 */

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    private val alerts: AppointmentAlerts,
    barberRepository: BarberRepository,
    session: SessionRepository,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<HistoryDestination>()
    private val barberId: Long? = args.barberId.takeIf { it > 0 }

    private val _state = MutableStateFlow(HistoryState(isBusiness = args.asBusiness))
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        barberId?.let { id ->
            viewModelScope.launch {
                val name = barberRepository.getById(id)?.name
                _state.update { it.copy(barberName = name) }
            }
        }

        val source: Flow<List<Appointment>> = when {
            barberId != null -> appointmentRepository.observeForBarber(barberId)
            args.asBusiness -> appointmentRepository.observeAll()
            else -> session.currentUser.filterNotNull().flatMapLatest { appointmentRepository.observeForClient(it.id) }
        }

        combine(source, everyMinute()) { all, now -> classify(all, now) }
            .onEach { (upcoming, awaiting, past) ->
                _state.update { it.copy(isLoading = false, upcoming = upcoming, awaitingClose = awaiting, past = past) }
            }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudo cargar el historial.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.OnTabSelected -> _state.update { it.copy(selectedTab = event.tab) }
            is HistoryEvent.OnCancelClick -> _state.update { it.copy(appointmentToCancel = event.appointment) }
            HistoryEvent.OnDismissCancel -> _state.update { it.copy(appointmentToCancel = null) }
            HistoryEvent.OnConfirmCancel -> cancelSelected()
            is HistoryEvent.OnRescheduleClick -> requestReschedule(event.appointment)
            HistoryEvent.OnNavigationHandled -> _state.update { it.copy(rescheduleTarget = null) }
            is HistoryEvent.OnConfirmAppointment -> confirm(event.appointment)
            is HistoryEvent.OnMarkCompleted -> close(event.appointment, AppointmentStatus.COMPLETED, "Cita marcada como completada.")
            is HistoryEvent.OnMarkNoShow -> close(event.appointment, AppointmentStatus.NO_SHOW, "Cita marcada como no presente.")
            HistoryEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            HistoryEvent.OnLogoutClick -> {
                authRepository.logout()
                _state.update { it.copy(isLoggedOut = true) }
            }
            HistoryEvent.OnBackClick, HistoryEvent.OnGoToCatalog -> Unit
        }
    }

    private fun classify(all: List<Appointment>, now: LocalDateTime): Triple<List<Appointment>, List<Appointment>, List<Appointment>> {
        val (active, closed) = all.partition(Appointment::isActive)
        val (started, notStarted) = active.partition { it.hasStarted(now) }
        return Triple(
            notStarted.sortedBy { it.startDateTime },
            started.sortedBy { it.startDateTime },
            closed.sortedByDescending { it.startDateTime },
        )
    }

    private fun everyMinute(): Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(MINUTE_MS)
        }
    }

    private fun requestReschedule(appointment: Appointment) {
        if (BusinessRules.canReschedule(appointment)) {
            _state.update { it.copy(rescheduleTarget = appointment) }
        } else {
            _state.update {
                it.copy(userMessage = "Faltan menos de ${BusinessRules.LATE_CANCELLATION_HOURS} h: ya no se puede reprogramar, solo cancelar.")
            }
        }
    }

    private fun cancelSelected() {
        val appointment = _state.value.appointmentToCancel ?: return
        viewModelScope.launch {
            val late = BusinessRules.isLateCancellation(appointment)
            appointmentRepository.updateStatus(appointment.id, AppointmentStatus.CANCELLED, lateCancellation = late)
            alerts.onClosed(appointment.id)
            _state.update {
                it.copy(
                    appointmentToCancel = null,
                    userMessage = if (late) "Cita cancelada (tardía: menos de 2 h de anticipación)." else "Cita cancelada.",
                )
            }
        }
    }

    private fun confirm(appointment: Appointment) {
        if (appointment.status != AppointmentStatus.PENDING) return
        viewModelScope.launch {
            appointmentRepository.updateStatus(appointment.id, AppointmentStatus.CONFIRMED)
            _state.update { it.copy(userMessage = "Cita confirmada.") }
        }
    }

    private fun close(appointment: Appointment, status: AppointmentStatus, message: String) {
        if (!BusinessRules.canClose(appointment)) {
            _state.update { it.copy(userMessage = "Solo puedes cerrar una cita cuando ya llegó su hora.") }
            return
        }
        viewModelScope.launch {
            appointmentRepository.updateStatus(appointment.id, status)
            alerts.onClosed(appointment.id)
            _state.update { it.copy(userMessage = message) }
        }
    }

    private companion object {
        const val MINUTE_MS = 60_000L
    }
}
