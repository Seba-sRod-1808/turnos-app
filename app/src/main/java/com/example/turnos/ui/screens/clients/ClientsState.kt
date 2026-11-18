package com.example.turnos.ui.screens.clients

import com.example.turnos.data.model.User
import com.example.turnos.data.repository.ClientCredentials

data class NewClientForm(
    val name: String = "",
    val username: String = "",
    val phone: String = "",
    val usernameEdited: Boolean = false,
    val nameError: String? = null,
    val usernameError: String? = null,
    val phoneError: String? = null,
)

data class ClientsState(
    val clients: List<User> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val form: NewClientForm? = null,
    val isSaving: Boolean = false,
    val clientToReset: User? = null,
    val credentials: ClientCredentials? = null,
    val userMessage: String? = null,
) {
    val visibleClients: List<User>
        get() = if (query.isBlank()) clients
        else clients.filter { it.name.contains(query.trim(), ignoreCase = true) || it.username.contains(query.trim(), ignoreCase = true) }

    val pendingFirstLogin: Int get() = clients.count { it.mustChangePassword }
}
