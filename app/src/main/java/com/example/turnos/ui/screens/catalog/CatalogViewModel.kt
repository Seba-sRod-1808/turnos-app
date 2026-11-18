package com.example.turnos.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.ServiceRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel del catálogo de servicios del cliente.
 *
 * Observa los servicios activos, el perfil del negocio, el estado de red y la próxima cita
 * activa del usuario en sesión para mantener actualizada la vista incluso sin conexión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    appointmentRepository: AppointmentRepository,
    session: SessionRepository,
    settings: SettingsRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadServices()

        settings.observeBusinessProfile()
            .onEach { profile -> _state.update { it.copy(businessName = profile.name) } }
            .launchIn(viewModelScope)

        networkMonitor.isOnline
            .onEach { online -> _state.update { it.copy(isOffline = !online) } }
            .launchIn(viewModelScope)

        session.currentUser
            .filterNotNull()
            .onEach { user -> _state.update { it.copy(userName = user.name) } }
            .flatMapLatest { user -> appointmentRepository.observeForClient(user.id) }
            .onEach { appointments ->
                val now = LocalDateTime.now()
                val next = appointments.filter { it.isActive && !it.hasStarted(now) }.minByOrNull { it.startDateTime }
                _state.update { it.copy(nextAppointment = next) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CatalogEvent) {
        when (event) {
            CatalogEvent.OnToggleSearch -> _state.update { it.copy(isSearching = !it.isSearching, query = "") }
            is CatalogEvent.OnQueryChange -> _state.update { it.copy(query = event.query) }
            CatalogEvent.OnRetry -> loadServices()
            CatalogEvent.OnLogoutClick -> {
                authRepository.logout()
                _state.update { it.copy(isLoggedOut = true) }
            }
            is CatalogEvent.OnReserveClick, CatalogEvent.OnMyAppointmentsClick -> Unit
        }
    }

    private fun loadServices() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            serviceRepository.observeActive()
                .catch { _state.update { it.copy(isLoading = false, errorMessage = "No se pudieron cargar los servicios.") } }
                .collect { services -> _state.update { it.copy(isLoading = false, services = services) } }
        }
    }
}
