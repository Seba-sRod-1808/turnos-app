package com.example.turnos.ui.screens.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.BarberStatus
import com.example.turnos.data.repository.BarberRepository
import com.example.turnos.util.formatDateFull
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TeamViewModel(private val repository: BarberRepository) : ViewModel() {

    private val _state = MutableStateFlow(TeamState(dateLabel = formatDateFull(LocalDate.now())))
    val state: StateFlow<TeamState> = _state.asStateFlow()

    init {
        repository.observeTeam()
            .onEach { barbers -> _state.update { it.copy(isLoading = false, barbers = barbers) } }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudo cargar el equipo.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: TeamEvent) {
        when (event) {
            TeamEvent.OnAddBarberClick -> _state.update { it.copy(form = BarberForm()) }
            is TeamEvent.OnFormChange -> _state.update { it.copy(form = event.form) }
            TeamEvent.OnDismissForm -> _state.update { it.copy(form = null) }
            TeamEvent.OnSaveBarber -> saveBarber()
            is TeamEvent.OnManageClick -> viewModelScope.launch {
                val next = when (event.barber.status) {
                    BarberStatus.AVAILABLE -> BarberStatus.IN_SERVICE
                    BarberStatus.IN_SERVICE -> BarberStatus.ABSENT
                    BarberStatus.ABSENT -> BarberStatus.AVAILABLE
                }
                repository.setStatus(event.barber.id, next)
            }
            TeamEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            is TeamEvent.OnViewAgendaClick, TeamEvent.OnBackClick -> Unit // navegación
        }
    }

    private fun saveBarber() {
        val form = _state.value.form ?: return
        if (!form.isValid) return
        viewModelScope.launch {
            repository.add(form.name, form.role)
            _state.update { it.copy(form = null, userMessage = "${form.name.trim()} se unió al equipo.") }
        }
    }
}
