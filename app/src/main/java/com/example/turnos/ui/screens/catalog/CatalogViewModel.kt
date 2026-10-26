package com.example.turnos.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.ServiceRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    session: SessionRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CatalogState(
            businessName = settings.businessProfile.name,
            userName = session.currentUser.value?.name.orEmpty(),
        )
    )
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadServices()
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
            is CatalogEvent.OnReserveClick, CatalogEvent.OnMyAppointmentsClick -> Unit // navegación
        }
    }

    /** Escucha la tabla de servicios: cualquier cambio del negocio se refleja aquí al instante. */
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
