package com.example.turnos.ui.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.Service
import com.example.turnos.data.repository.ServiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Gestión del catálogo: agregar, editar, pausar y eliminar servicios. */
class ServicesViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _state = MutableStateFlow(ServicesState())
    val state: StateFlow<ServicesState> = _state.asStateFlow()

    init {
        repository.observeAll()
            .onEach { list -> _state.update { it.copy(isLoading = false, services = list) } }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudieron cargar los servicios.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ServicesEvent) {
        when (event) {
            ServicesEvent.OnToggleSearch -> _state.update { it.copy(isSearching = !it.isSearching, query = "") }
            is ServicesEvent.OnQueryChange -> _state.update { it.copy(query = event.query) }
            is ServicesEvent.OnToggleActive -> viewModelScope.launch {
                repository.save(event.service.copy(active = event.active))
            }
            is ServicesEvent.OnEditClick -> _state.update { it.copy(form = event.service.toForm()) }
            ServicesEvent.OnAddClick -> _state.update { it.copy(form = ServiceForm()) }
            is ServicesEvent.OnFormChange -> _state.update { it.copy(form = event.form) }
            ServicesEvent.OnSaveForm -> saveForm()
            ServicesEvent.OnDeleteFromForm -> deleteFromForm()
            ServicesEvent.OnDismissForm -> _state.update { it.copy(form = null) }
            ServicesEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            ServicesEvent.OnBackClick -> Unit // navegación
        }
    }

    private fun saveForm() {
        val form = _state.value.form ?: return
        if (!form.isValid) {
            _state.update { it.copy(userMessage = "Revisa nombre, duración (5–480 min) y precio.") }
            return
        }
        viewModelScope.launch {
            repository.save(
                Service(
                    id = form.id,
                    name = form.name.trim(),
                    description = form.description.trim(),
                    durationMin = form.duration.toInt(),
                    price = form.price.toInt(),
                    active = form.active,
                )
            )
            _state.update {
                it.copy(form = null, userMessage = if (form.isNew) "Servicio agregado." else "Servicio actualizado.")
            }
        }
    }

    private fun deleteFromForm() {
        val form = _state.value.form ?: return
        if (form.isNew) return
        viewModelScope.launch {
            repository.delete(form.id)
            _state.update { it.copy(form = null, userMessage = "Servicio eliminado.") }
        }
    }

    private fun Service.toForm() = ServiceForm(id, name, description, durationMin.toString(), price.toString(), active)
}
