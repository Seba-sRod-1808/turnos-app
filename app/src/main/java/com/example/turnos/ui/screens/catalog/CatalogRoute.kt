package com.example.turnos.ui.screens.catalog

import androidx.compose.runtime.*
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.delay

@Composable
fun CatalogRoute(
    onReserve: (serviceId: String) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var state by remember { mutableStateOf(CatalogUiState(isLoading = true)) }
    var reloadKey by remember { mutableIntStateOf(0) }

    // Carga simulada desde la fuente de datos fake.
    LaunchedEffect(reloadKey) {
        state = state.copy(isLoading = true, errorMessage = null)
        delay(600)
        state = state.copy(
            isLoading = false,
            businessName = FakeDataSource.business.name,
            services = filterServices(state.query),
        )
    }

    CatalogScreen(
        state = state,
        onToggleSearch = {
            val searching = !state.isSearching
            state = state.copy(
                isSearching = searching,
                query = if (searching) state.query else "",
                services = if (searching) state.services else filterServices(""),
            )
        },
        onQueryChange = { q -> state = state.copy(query = q, services = filterServices(q)) },
        onReserve = onReserve,
        onRetry = { reloadKey++ },
        onOpenHistory = onOpenHistory,
        onLogout = onLogout,
    )
}

/** Solo servicios activos, filtrados por nombre. */
private fun filterServices(query: String) = FakeDataSource.services
    .filter { it.active }
    .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
