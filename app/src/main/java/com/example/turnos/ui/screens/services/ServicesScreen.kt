package com.example.turnos.ui.screens.services

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.Service
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/** Campos del formulario de agregar / editar servicio. */
data class ServiceForm(
    val id: String? = null, // null = servicio nuevo
    val name: String = "",
    val description: String = "",
    val duration: String = "30",
    val price: String = "",
) {
    val isNew: Boolean get() = id == null
    val isValid: Boolean
        get() = name.isNotBlank() && (duration.toIntOrNull() ?: 0) > 0 && price.toIntOrNull() != null
}

data class ServicesUiState(
    val services: List<Service> = emptyList(),
    /** Formulario abierto; null = diálogo cerrado. */
    val form: ServiceForm? = null,
) {
    val activeCount: Int get() = services.count { it.active }
    val pausedCount: Int get() = services.size - activeCount
}

/** Gestión del catálogo: agregar, editar, pausar y eliminar servicios. */
@Composable
fun ServicesScreen(
    state: ServicesUiState,
    onBack: () -> Unit,
    onToggleActive: (Service, Boolean) -> Unit,
    onEditClick: (Service) -> Unit,
    onAddClick: () -> Unit,
    onFormChange: (ServiceForm) -> Unit,
    onFormSave: () -> Unit,
    onFormDelete: () -> Unit,
    onFormDismiss: () -> Unit,
) {
    TurnosScaffold(
        title = "Servicios y Precios",
        onBack = onBack,
        actionIcon = Icons.Filled.Search,
        actionDescription = "Buscar",
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                InfoBanner(
                    "Los cambios de precio y estado se reflejan de inmediato en el catálogo de reservas.",
                    icon = Icons.Outlined.Sell,
                )
            }
            item {
                val paused = state.pausedCount
                SectionHeader("Tus servicios", "${state.activeCount} activos · $paused pausado${if (paused == 1) "" else "s"}")
            }
            if (state.services.isEmpty()) {
                item { EmptyContent("Aún no tienes servicios. Agrega el primero.", icon = Icons.Outlined.ContentCut) }
            }
            items(state.services, key = { it.id }) { s ->
                ServiceAdminCard(s, onToggle = { onToggleActive(s, it) }, onEdit = { onEditClick(s) })
            }
            item {
                SecondaryButton("AGREGAR SERVICIO", icon = Icons.Filled.Add, onClick = onAddClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    state.form?.let { form ->
        ServiceEditorDialog(form, onFormChange, onFormSave, onFormDelete, onFormDismiss)
    }
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
private fun ServiceEditorDialog(
    form: ServiceForm,
    onChange: (ServiceForm) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text(if (form.isNew) "Nuevo servicio" else "Editar servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorField("Nombre", form.name) { onChange(form.copy(name = it)) }
                EditorField("Descripción", form.description) { onChange(form.copy(description = it)) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorField("Duración (min)", form.duration, Modifier.weight(1f), numeric = true) {
                        onChange(form.copy(duration = it.filter(Char::isDigit)))
                    }
                    EditorField("Precio (Q)", form.price, Modifier.weight(1f), numeric = true) {
                        onChange(form.copy(price = it.filter(Char::isDigit)))
                    }
                }
                if (!form.isNew) {
                    TextButton(onClick = onDelete) { Text("Eliminar servicio", color = DangerRed) }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = form.isValid, onClick = onSave) {
                Text("Guardar", color = if (form.isValid) TurnosOrange else InkMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = InkSecondary) } },
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TurnosOrange,
            focusedLabelColor = TurnosOrange,
            cursorColor = TurnosOrange,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewState = ServicesUiState(services = FakeDataSource.services)

@Composable
private fun ServicesPreviewHost(state: ServicesUiState) = TurnosTheme {
    ServicesScreen(state, {}, { _, _ -> }, {}, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, heightDp = 900, name = "Servicios - lista")
@Composable
private fun ServicesPreview() = ServicesPreviewHost(previewState)

@Preview(showBackground = true, name = "Servicios - vacío")
@Composable
private fun ServicesEmptyPreview() = ServicesPreviewHost(ServicesUiState())

@Preview(showBackground = true, name = "Servicios - nuevo servicio")
@Composable
private fun ServicesNewPreview() = ServicesPreviewHost(previewState.copy(form = ServiceForm()))

@Preview(showBackground = true, name = "Servicios - editar servicio")
@Composable
private fun ServicesEditPreview() = ServicesPreviewHost(
    previewState.copy(form = ServiceForm("s1", "Corte Clásico", "Corte tradicional y lavado", "45", "150")),
)
