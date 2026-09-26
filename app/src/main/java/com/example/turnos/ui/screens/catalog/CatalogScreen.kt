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
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.Service
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun CatalogScreen(
    onReserve: (serviceId: String) -> Unit,
    onOpenHistory: () -> Unit,
    onLogout: () -> Unit,
) {
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val services = remember(query) {
        MockData.services
            .filter { it.active }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    }

    TurnosScaffold(
        title = "Catálogo de Servicios",
        actionIcon = if (searching) Icons.Filled.Close else Icons.Filled.Search,
        actionDescription = if (searching) "Cerrar búsqueda" else "Buscar",
        onAction = { searching = !searching; if (!searching) query = "" },
        bottomBar = {
            ClientBottomBar(
                selected = 0,
                onServices = {},
                onAppointments = onOpenHistory,
                onLogout = onLogout,
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (searching) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
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
                    Text(MockData.business.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                }
            }
            items(services, key = { it.id }) { service ->
                ServiceCard(service, onReserve = { onReserve(service.id) })
            }
            if (services.isEmpty()) {
                item {
                    Text(
                        "No encontramos servicios con “$query”.",
                        color = InkSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp),
                    )
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

/** Navegación inferior del cliente. */
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

@Preview(showBackground = true)
@Composable
private fun CatalogPreview() = TurnosTheme { CatalogScreen({}, {}, {}) }
