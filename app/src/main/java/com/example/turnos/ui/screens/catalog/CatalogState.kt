package com.example.turnos.ui.screens.catalog

import com.example.turnos.data.model.Service

data class CatalogState(
    val businessName: String = "",
    val userName: String = "",
    val services: List<Service> = emptyList(),
    val isSearching: Boolean = false,
    val query: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** true cuando se cerró sesión y hay que volver al login. */
    val isLoggedOut: Boolean = false,
) {
    /** Servicios filtrados por la búsqueda (derivado, no se guarda aparte). */
    val visibleServices: List<Service>
        get() = if (query.isBlank()) services else services.filter { it.name.contains(query.trim(), ignoreCase = true) }
}
