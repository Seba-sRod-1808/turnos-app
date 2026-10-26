package com.example.turnos.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.domain.BusinessRules
import com.example.turnos.navigation.HistoryDestination
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    session: SessionRepository,
) : ViewModel() {

    private val isBusiness = savedStateHandle.toRoute<HistoryDestination>().asBusiness

    private val _state = MutableStateFlow(HistoryState(isBusiness = isBusiness))
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        // El negocio ve todas las citas; el cliente solo las suyas.
        val source = if (isBusiness) appointmentRepository.observeAll()
        else appointmentRepository.observeForClient(session.currentUser.value?.id ?: -1L)

        source
            .onEach { all ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        upcoming = all.filter(Appointment::isActive).sortedBy { a -> a.startDateTime },
                        past = all.filterNot(Appointment::isActive).sortedByDescending { a -> a.startDateTime },
                    )
                }
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
            is HistoryEvent.OnConfirmAppointment ->
                changeStatus(event.appointment, AppointmentStatus.CONFIRMED, "Cita confirmada.")
            is HistoryEvent.OnMarkCompleted ->
                changeStatus(event.appointment, AppointmentStatus.COMPLETED, "Cita marcada como completada.")
            is HistoryEvent.OnMarkNoShow ->
                changeStatus(event.appointment, AppointmentStatus.NO_SHOW, "Cita marcada como no presente.")
            HistoryEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            HistoryEvent.OnLogoutClick -> {
                authRepository.logout()
                _state.update { it.copy(isLoggedOut = true) }
            }
            is HistoryEvent.OnRescheduleClick, HistoryEvent.OnBackClick, HistoryEvent.OnGoToCatalog -> Unit // navegación
        }
    }

    /** Regla: cancelar con menos de 2 h de anticipación queda marcado como cancelación tardía. */
    private fun cancelSelected() {
        val appointment = _state.value.appointmentToCancel ?: return
        viewModelScope.launch {
            val late = BusinessRules.isLateCancellation(appointment)
            appointmentRepository.updateStatus(appointment.id, AppointmentStatus.CANCELLED, lateCancellation = late)
            _state.update {
                it.copy(
                    appointmentToCancel = null,
                    userMessage = if (late) "Cita cancelada (tardía: menos de 2 h de anticipación)." else "Cita cancelada.",
                )
            }
        }
    }

    private fun changeStatus(appointment: Appointment, status: AppointmentStatus, message: String) {
        viewModelScope.launch {
            appointmentRepository.updateStatus(appointment.id, status)
            _state.update { it.copy(userMessage = message) }
        }
    }
}
