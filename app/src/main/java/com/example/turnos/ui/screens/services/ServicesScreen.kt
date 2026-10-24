package com.example.turnos.ui.screens.services

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
fun ServicesRoute(
    onBack: () -> Unit,
    viewModel: ServicesViewModel = turnosViewModel { ServicesViewModel(serviceRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(ServicesEvent.OnMessageShown) }

    ServicesScreen(state, snackbar) { event ->
        when (event) {
            ServicesEvent.OnBackClick -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun ServicesScreen(
    state: ServicesState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (ServicesEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Servicios y Precios",
        onBack = { onEvent(ServicesEvent.OnBackClick) },
        actionIcon = if (state.isSearching) Icons.Filled.Close else Icons.Filled.Search,
        actionDescription = if (state.isSearching) "Cerrar búsqueda" else "Buscar",
        onAction = { onEvent(ServicesEvent.OnToggleSearch) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        if (state.isLoading) {
            LoadingContent(Modifier.padding(padding))
            return@TurnosScaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                InfoBanner("Los cambios de precio y estado se reflejan de inmediato en el catálogo de reservas.", icon = Icons.Outlined.Sell)
            }
            if (state.isSearching) {
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { onEvent(ServicesEvent.OnQueryChange(it)) },
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
                val paused = state.pausedCount
                SectionHeader("Tus servicios", "${state.activeCount} activos · $paused pausado${if (paused == 1) "" else "s"}")
            }
            if (state.visibleServices.isEmpty()) {
                item { EmptyContent("No hay servicios que mostrar. Agrega el primero.", icon = Icons.Outlined.ContentCut) }
            }
            items(state.visibleServices, key = { it.id }) { s ->
                ServiceAdminCard(
                    service = s,
                    onToggle = { onEvent(ServicesEvent.OnToggleActive(s, it)) },
                    onEdit = { onEvent(ServicesEvent.OnEditClick(s)) },
                )
            }
            item {
                SecondaryButton("AGREGAR SERVICIO", icon = Icons.Filled.Add, onClick = { onEvent(ServicesEvent.OnAddClick) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    state.form?.let { form -> ServiceEditorDialog(form, onEvent) }
}

@Composable
private fun ServiceAdminCard(service: Service, onToggle: (Boolean) -> Unit, onEdit: () -> Unit) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(service.name, style = MaterialTheme.typography.titleSmall, color = Ink)
            if (!service.active) {
                Spacer(Modifier.width(8.dp))
                StatusChip("Pausado", InkSecondary, NeutralSoft)
            }
            Spacer(Modifier.weight(1f))
            Text(quetzales(service.price), style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        Text("${service.durationMin} min · ${service.description}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TurnosSwitch(checked = service.active, onCheckedChange = onToggle)
            Spacer(Modifier.width(8.dp))
            Text(
                if (service.active) "Activo en reservas" else "No disponible",
                style = MaterialTheme.typography.bodySmall,
                color = if (service.active) SuccessGreen else InkMuted,
                modifier = Modifier.weight(1f),
            )
            Row(Modifier.clickable(onClick = onEdit).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Edit, null, tint = TurnosOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Editar", style = MaterialTheme.typography.labelMedium, color = TurnosOrange)
            }
        }
    }
}

@Composable
private fun ServiceEditorDialog(form: ServiceForm, onEvent: (ServicesEvent) -> Unit) {
    val change: (ServiceForm) -> Unit = { onEvent(ServicesEvent.OnFormChange(it)) }
    AlertDialog(
        onDismissRequest = { onEvent(ServicesEvent.OnDismissForm) },
        containerColor = SurfaceWhite,
        title = { Text(if (form.isNew) "Nuevo servicio" else "Editar servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorField("Nombre", form.name) { change(form.copy(name = it)) }
                EditorField("Descripción", form.description) { change(form.copy(description = it)) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorField("Duración (min)", form.duration, Modifier.weight(1f), numeric = true) {
                        change(form.copy(duration = it.filter(Char::isDigit)))
                    }
                    EditorField("Precio (Q)", form.price, Modifier.weight(1f), numeric = true) {
                        change(form.copy(price = it.filter(Char::isDigit)))
                    }
                }
                if (!form.isNew) {
                    TextButton(onClick = { onEvent(ServicesEvent.OnDeleteFromForm) }) { Text("Eliminar servicio", color = DangerRed) }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = form.isValid, onClick = { onEvent(ServicesEvent.OnSaveForm) }) {
                Text("Guardar", color = if (form.isValid) TurnosOrange else InkMuted)
            }
        },
        dismissButton = { TextButton(onClick = { onEvent(ServicesEvent.OnDismissForm) }) { Text("Cancelar", color = InkSecondary) } },
    )
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
        shape = MaterialTheme.shapes.small,
        colors = turnosFieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val sample = ServicesState(services = PreviewData.services, isLoading = false)

@Composable
private fun Host(state: ServicesState) = TurnosTheme { ServicesScreen(state) {} }

@Preview(showBackground = true, heightDp = 900, name = "Servicios - lista")
@Composable
private fun ServicesPreview() = Host(sample)

@Preview(showBackground = true, name = "Servicios - vacío")
@Composable
private fun ServicesEmptyPreview() = Host(ServicesState(isLoading = false))

@Preview(showBackground = true, name = "Servicios - nuevo")
@Composable
private fun ServicesNewPreview() = Host(sample.copy(form = ServiceForm()))

@Preview(showBackground = true, name = "Servicios - editar")
@Composable
private fun ServicesEditPreview() = Host(sample.copy(form = ServiceForm(1, "Corte Clásico", "Corte tradicional y lavado", "45", "150")))
