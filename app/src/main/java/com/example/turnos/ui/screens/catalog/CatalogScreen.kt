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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.Service
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

data class CatalogUiState(
    val businessName: String = "",
    val services: List<Service> = emptyList(),
    val isSearching: Boolean = false,
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onToggleSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onReserve: (serviceId: String) -> Unit,
    onRetry: () -> Unit,
    onOpenHistory: () -> Unit,
    onLogout: () -> Unit,
) {
    TurnosScaffold(
        title = "Catálogo de Servicios",
        actionIcon = if (state.isSearching) Icons.Filled.Close else Icons.Filled.Search,
        actionDescription = if (state.isSearching) "Cerrar búsqueda" else "Buscar",
        onAction = onToggleSearch,
        bottomBar = { ClientBottomBar(selected = 0, onServices = {}, onAppointments = onOpenHistory, onLogout = onLogout) },
    ) { padding ->
        val contentModifier = Modifier.padding(padding).fillMaxSize()
        when {
            state.isLoading -> LoadingContent(contentModifier, "Cargando servicios…")
            state.errorMessage != null -> ErrorContent(state.errorMessage, onRetry, contentModifier)
            else -> LazyColumn(
                contentModifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.isSearching) {
                    item {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChange,
                            placeholder = { Text("Buscar servicio") },
                            leadingIcon = { Icon(Icons.Filled.Search, null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TurnosOrange,
                                unfocusedBorderColor = DividerGray,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storefront, null, tint = TurnosOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(state.businessName, style = MaterialTheme.typography.titleSmall, color = Ink)
                    }
                }
                items(state.services, key = { it.id }) { service ->
                    ServiceCard(service, onReserve = { onReserve(service.id) })
                }
                if (state.services.isEmpty()) {
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

/** Navegación inferior del cliente (compartida con Historial). */
@Composable
fun ClientBottomBar(
    selected: Int,
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
            selected = selected == 0, onClick = onServices, colors = colors,
            icon = { Icon(Icons.Filled.ContentCut, null) }, label = { Text("Servicios") },
        )
        NavigationBarItem(
            selected = selected == 1, onClick = onAppointments, colors = colors,
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

private val previewState = CatalogUiState(
    businessName = FakeDataSource.business.name,
    services = FakeDataSource.services.filter { it.active },
)

@Composable
private fun CatalogPreviewHost(state: CatalogUiState) = TurnosTheme {
    CatalogScreen(state, {}, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, name = "Catálogo - con servicios")
@Composable
private fun CatalogContentPreview() = CatalogPreviewHost(previewState)

@Preview(showBackground = true, name = "Catálogo - buscando")
@Composable
private fun CatalogSearchPreview() = CatalogPreviewHost(
    previewState.copy(isSearching = true, query = "corte", services = previewState.services.filter { it.name.contains("Corte") }),
)

@Preview(showBackground = true, name = "Catálogo - búsqueda sin resultados")
@Composable
private fun CatalogNoResultsPreview() = CatalogPreviewHost(
    previewState.copy(isSearching = true, query = "tinte", services = emptyList()),
)

@Preview(showBackground = true, name = "Catálogo - cargando")
@Composable
private fun CatalogLoadingPreview() = CatalogPreviewHost(CatalogUiState(isLoading = true))

@Preview(showBackground = true, name = "Catálogo - error")
@Composable
private fun CatalogErrorPreview() = CatalogPreviewHost(
    CatalogUiState(errorMessage = "No se pudieron cargar los servicios. Revisa tu conexión."),
)
