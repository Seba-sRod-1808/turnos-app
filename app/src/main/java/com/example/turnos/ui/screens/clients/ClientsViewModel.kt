package com.example.turnos.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.repository.ClientRepository
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * El dueño crea las cuentas de sus clientes (usuario + contraseña temporal) y puede
 * restablecer la contraseña de quien la olvidó y ya no puede responder su pregunta.
 */

class ClientsViewModel(private val repository: ClientRepository) : ViewModel() {

    private val _state = MutableStateFlow(ClientsState())
    val state: StateFlow<ClientsState> = _state.asStateFlow()

    init {
        repository.observeClients()
            .onEach { clients -> _state.update { it.copy(isLoading = false, clients = clients) } }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudieron cargar los clientes.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ClientsEvent) {
        when (event) {
            is ClientsEvent.OnQueryChange -> _state.update { it.copy(query = event.query) }
            ClientsEvent.OnAddClick -> _state.update { it.copy(form = NewClientForm()) }
            is ClientsEvent.OnNameChange -> updateForm { form ->
                form.copy(
                    name = event.value,
                    username = if (form.usernameEdited) form.username else suggestUsername(event.value),
                    nameError = null,
                )
            }
            is ClientsEvent.OnUsernameChange -> updateForm {
                it.copy(username = Validators.normalizeUsername(event.value), usernameEdited = true, usernameError = null)
            }
            is ClientsEvent.OnPhoneChange -> updateForm { it.copy(phone = event.value, phoneError = null) }
            ClientsEvent.OnSaveClient -> saveClient()
            ClientsEvent.OnDismissForm -> _state.update { it.copy(form = null) }
            is ClientsEvent.OnResetPasswordClick -> _state.update { it.copy(clientToReset = event.client) }
            ClientsEvent.OnDismissReset -> _state.update { it.copy(clientToReset = null) }
            ClientsEvent.OnConfirmReset -> resetPassword()
            ClientsEvent.OnCredentialsDismissed -> _state.update { it.copy(credentials = null) }
            ClientsEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            ClientsEvent.OnBackClick -> Unit
        }
    }

    private fun updateForm(transform: (NewClientForm) -> NewClientForm) {
        _state.update { s -> s.form?.let { s.copy(form = transform(it)) } ?: s }
    }

    private fun saveClient() {
        val form = _state.value.form ?: return
        val validated = form.copy(
            nameError = if (form.name.isBlank()) "Escribe el nombre del cliente" else null,
            usernameError = if (!Validators.isValidUsername(form.username)) "3 a 20 caracteres: letras, números, punto o guion bajo" else null,
            phoneError = if (!Validators.isValidPhone(form.phone)) "Teléfono inválido" else null,
        )
        if (listOf(validated.nameError, validated.usernameError, validated.phoneError).any { it != null }) {
            _state.update { it.copy(form = validated) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repository.createClient(form.name, form.username, form.phone)
                .onSuccess { credentials -> _state.update { it.copy(isSaving = false, form = null, credentials = credentials) } }
                .onFailure { e ->
                    _state.update { s -> s.copy(isSaving = false, form = s.form?.copy(usernameError = e.message)) }
                }
        }
    }

    private fun resetPassword() {
        val client = _state.value.clientToReset ?: return
        viewModelScope.launch {
            val credentials = repository.resetPassword(client)
            _state.update { it.copy(clientToReset = null, credentials = credentials) }
        }
    }

    private fun suggestUsername(name: String): String =
        Normalizer.normalize(name.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString(".")
            .replace(Regex("[^a-z0-9._]"), "")
            .take(20)
}
