package com.example.turnos.ui.screens.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class MyBusinessViewModel(
    appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    session: SessionRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        MyBusinessState(profile = settings.businessProfile, ownerName = session.currentUser.value?.name.orEmpty())
    )
    val state: StateFlow<MyBusinessState> = _state.asStateFlow()

    init {
        // Las estadísticas se recalculan solas cada vez que cambia una cita.
        appointmentRepository.observeAll()
            .onEach { all ->
                val today = all.filter { it.date == LocalDate.now() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        appointmentsToday = today.count { a -> a.isActive || a.status == AppointmentStatus.COMPLETED },
                        pending = all.count { a -> a.status == AppointmentStatus.PENDING },
                        revenueToday = today
                            .filter { a -> a.status == AppointmentStatus.COMPLETED || a.status == AppointmentStatus.CONFIRMED }
                            .sumOf { a -> a.price },
                    )
                }
            }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudieron cargar las estadísticas.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: MyBusinessEvent) {
        when (event) {
            MyBusinessEvent.OnEditProfileClick ->
                _state.update { it.copy(userMessage = "La edición del perfil llegará en la siguiente fase.") }
            MyBusinessEvent.OnLogoutClick -> {
                authRepository.logout()
                _state.update { it.copy(isLoggedOut = true) }
            }
            MyBusinessEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            is MyBusinessEvent.OnSectionClick -> Unit // navegación
        }
    }
}
