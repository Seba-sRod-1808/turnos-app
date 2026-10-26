package com.example.turnos.ui.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.Service
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*
import com.example.turnos.util.quetzales

@Composable
fun CatalogRoute(
    onReserve: (serviceId: Long) -> Unit,
    onMyAppointments: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: CatalogViewModel = turnosViewModel {
        CatalogViewModel(serviceRepository, authRepository, sessionRepository, settingsRepository)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedOut) { if (state.isLoggedOut) onLoggedOut() }

    CatalogScreen(state) { event ->
        when (event) {
            is CatalogEvent.OnReserveClick -> onReserve(event.serviceId)
            CatalogEvent.OnMyAppointmentsClick -> onMyAppointments()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun CatalogScreen(state: CatalogState, onEvent: (CatalogEvent) -> Unit) {
    TurnosScaffold(
        title = "Catálogo de Servicios",
        actionIcon = if (state.isSearching) Icons.Filled.Close else Icons.Filled.Search,
        actionDescription = if (state.isSearching) "Cerrar búsqueda" else "Buscar",
        onAction = { onEvent(CatalogEvent.OnToggleSearch) },
        bottomBar = {
            ClientBottomBar(
                selected = ClientTab.SERVICES,
                onServices = {},
                onAppointments = { onEvent(CatalogEvent.OnMyAppointmentsClick) },
                onLogout = { onEvent(CatalogEvent.OnLogoutClick) },
            )
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding).fillMaxSize()
        when {
            state.isLoading -> LoadingContent(contentModifier, "Cargando servicios…")
            state.errorMessage != null -> ErrorContent(state.errorMessage, { onEvent(CatalogEvent.OnRetry) }, contentModifier)
            else -> LazyColumn(
                contentModifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.isSearching) {
                    item {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = { onEvent(CatalogEvent.OnQueryChange(it)) },
                            placeholder = { Text("Buscar servicio") },
                            leadingIcon = { Icon(Icons.Filled.Search, null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = turnosFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item {
                    Column {
                        if (state.userName.isNotBlank()) {
                            Text("Hola, ${state.userName.substringBefore(" ")} 👋", style = MaterialTheme.typography.titleMedium, color = Ink)
                            Spacer(Modifier.height(4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Storefront, null, tint = TurnosOrange, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(state.businessName, style = MaterialTheme.typography.titleSmall, color = InkSecondary)
                        }
                    }
                }
                val visible = state.visibleServices
                items(visible, key = { it.id }) { service ->
                    ServiceCard(service) { onEvent(CatalogEvent.OnReserveClick(service.id)) }
                }
                if (visible.isEmpty()) {
                    item {
                        if (state.query.isNotBlank()) {
                            EmptyContent("No encontramos servicios con “${state.query}”.", icon = Icons.Outlined.SearchOff)
                        } else {
                            EmptyContent("Este negocio aún no tiene servicios disponibles.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(service: Service, onReserve: () -> Unit) {
    TurnosCard(padding = 12.dp) {
        PhotoPlaceholder(
            icon = if (service.name.contains("Afeitado") || service.name.contains("Barba")) Icons.Filled.Face else Icons.Filled.ContentCut,
            modifier = Modifier.height(150.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(service.name, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
            Text(quetzales(service.price), style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(service.description, style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Schedule, null, tint = InkMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("${service.durationMin} min", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        Spacer(Modifier.height(12.dp))
        SecondaryButton("RESERVAR", onClick = onReserve, modifier = Modifier.fillMaxWidth())
    }
}

enum class ClientTab { SERVICES, APPOINTMENTS }

/** Navegación inferior del cliente (compartida con Historial). */
@Composable
fun ClientBottomBar(
    selected: ClientTab,
    onServices: () -> Unit,
    onAppointments: () -> Unit,
    onLogout: () -> Unit,
) {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = TurnosOrange,
        selectedTextColor = TurnosOrange,
        indicatorColor = TurnosOrangeSoft,
        unselectedIconColor = InkSecondary,
        unselectedTextColor = InkSecondary,
    )
    NavigationBar(containerColor = SurfaceWhite) {
        NavigationBarItem(
            selected = selected == ClientTab.SERVICES, onClick = onServices, colors = colors,
            icon = { Icon(Icons.Filled.ContentCut, null) }, label = { Text("Servicios") },
        )
        NavigationBarItem(
            selected = selected == ClientTab.APPOINTMENTS, onClick = onAppointments, colors = colors,
            icon = { Icon(Icons.Outlined.CalendarMonth, null) }, label = { Text("Mis citas") },
        )
        NavigationBarItem(
            selected = false, onClick = onLogout, colors = colors,
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }, label = { Text("Salir") },
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val sample = CatalogState(
    businessName = PreviewData.business.name,
    userName = "Ana López",
    services = PreviewData.services.filter { it.active },
    isLoading = false,
)

@Composable
private fun Host(state: CatalogState) = TurnosTheme { CatalogScreen(state) {} }

@Preview(showBackground = true, name = "Catálogo - con servicios")
@Composable
private fun CatalogContentPreview() = Host(sample)

@Preview(showBackground = true, name = "Catálogo - buscando")
@Composable
private fun CatalogSearchPreview() = Host(sample.copy(isSearching = true, query = "corte"))

@Preview(showBackground = true, name = "Catálogo - sin resultados")
@Composable
private fun CatalogNoResultsPreview() = Host(sample.copy(isSearching = true, query = "tinte"))

@Preview(showBackground = true, name = "Catálogo - cargando")
@Composable
private fun CatalogLoadingPreview() = Host(CatalogState(isLoading = true))

@Preview(showBackground = true, name = "Catálogo - error")
@Composable
private fun CatalogErrorPreview() = Host(CatalogState(isLoading = false, errorMessage = "No se pudieron cargar los servicios."))
