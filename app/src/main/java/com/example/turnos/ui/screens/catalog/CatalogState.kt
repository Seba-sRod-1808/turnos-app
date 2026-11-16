package com.example.turnos.ui.screens.catalog

import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.Service

data class CatalogState(
    val businessName: String = "",
    val userName: String = "",
    val services: List<Service> = emptyList(),
    /** Se lee de Room, así que sigue visible sin conexión (modo de consulta offline). */
    val nextAppointment: Appointment? = null,
    val isOffline: Boolean = false,
    val isSearching: Boolean = false,
    val query: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false,
) {
    val visibleServices: List<Service>
        get() = if (query.isBlank()) services else services.filter { it.name.contains(query.trim(), ignoreCase = true) }
}
