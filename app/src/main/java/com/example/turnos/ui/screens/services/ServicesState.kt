package com.example.turnos.ui.screens.services

import com.example.turnos.data.model.Service

/** Campos del formulario para agregar / editar un servicio. */
data class ServiceForm(
    val id: Long = 0, // 0 = servicio nuevo
    val name: String = "",
    val description: String = "",
    val duration: String = "30",
    val price: String = "",
    val active: Boolean = true,
) {
    val isNew: Boolean get() = id == 0L
    val isValid: Boolean
        get() = name.isNotBlank() && (duration.toIntOrNull() ?: 0) in 5..480 && price.toIntOrNull() != null
}

data class ServicesState(
    val services: List<Service> = emptyList(),
    val isSearching: Boolean = false,
    val query: String = "",
    /** Formulario abierto; null = diálogo cerrado. */
    val form: ServiceForm? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
) {
    val visibleServices: List<Service>
        get() = if (query.isBlank()) services else services.filter { it.name.contains(query.trim(), ignoreCase = true) }
    val activeCount: Int get() = services.count { it.active }
    val pausedCount: Int get() = services.size - activeCount
}
