package com.example.turnos.ui.screens.services

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.Service
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/** Gestión del catálogo: agregar, editar, pausar y eliminar servicios. */
@Composable
fun ServicesScreen(onBack: () -> Unit) {
    val services = remember { mutableStateListOf(*MockData.services.toTypedArray()) }
    // null = cerrado; Service con id vacío = nuevo servicio
    var editing by remember { mutableStateOf<Service?>(null) }

    val activeCount = services.count { it.active }
    val pausedCount = services.size - activeCount

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
            item { SectionHeader("Tus servicios", "$activeCount activos · $pausedCount pausado${if (pausedCount == 1) "" else "s"}") }
            items(services, key = { it.id }) { s ->
                ServiceAdminCard(
                    service = s,
                    onToggle = { on ->
                        val i = services.indexOfFirst { it.id == s.id }
                        services[i] = s.copy(active = on, paused = !on)
                    },
                    onEdit = { editing = s },
                )
            }
            item {
                SecondaryButton(
                    "AGREGAR SERVICIO",
                    icon = Icons.Filled.Add,
                    onClick = { editing = Service("", "", "", 30, 0) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    editing?.let { current ->
        ServiceEditorDialog(
            initial = current,
            onDismiss = { editing = null },
            onDelete = if (current.id.isNotEmpty()) {
                { services.removeAll { it.id == current.id }; editing = null }
            } else null,
            onSave = { updated ->
                if (current.id.isEmpty()) {
                    services.add(updated.copy(id = "s${System.currentTimeMillis()}"))
                } else {
                    val i = services.indexOfFirst { it.id == current.id }
                    if (i >= 0) services[i] = updated
                }
                editing = null
            },
        )
    }
}

@Composable
private fun ServiceAdminCard(service: Service, onToggle: (Boolean) -> Unit, onEdit: () -> Unit) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(service.name, style = MaterialTheme.typography.titleSmall, color = Ink)
            if (service.paused) {
                Spacer(Modifier.width(8.dp))
                StatusChip("Pausado", InkSecondary, NeutralSoft)
            }
            Spacer(Modifier.weight(1f))
            Text(quetzales(service.price), style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        Text(
            "${service.durationMin} min · ${service.description}",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary,
        )
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
            Row(
                Modifier.clickable(onClick = onEdit).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Edit, null, tint = TurnosOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Editar", style = MaterialTheme.typography.labelMedium, color = TurnosOrange)
            }
        }
    }
}

@Composable
private fun ServiceEditorDialog(
    initial: Service,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (Service) -> Unit,
) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var duration by remember { mutableStateOf(initial.durationMin.toString()) }
    var price by remember { mutableStateOf(if (initial.price == 0) "" else initial.price.toString()) }
    val valid = name.isNotBlank() && duration.toIntOrNull() != null && price.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text(if (initial.id.isEmpty()) "Nuevo servicio" else "Editar servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorField("Nombre", name) { name = it }
                EditorField("Descripción", description) { description = it }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorField("Duración (min)", duration, Modifier.weight(1f), numeric = true) { duration = it.filter(Char::isDigit) }
                    EditorField("Precio (Q)", price, Modifier.weight(1f), numeric = true) { price = it.filter(Char::isDigit) }
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Eliminar servicio", color = DangerRed) }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            description = description.trim(),
                            durationMin = duration.toInt(),
                            price = price.toInt(),
                        )
                    )
                },
            ) { Text("Guardar", color = if (valid) TurnosOrange else InkMuted) }
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

@Preview(showBackground = true)
@Composable
private fun ServicesPreview() = TurnosTheme { ServicesScreen {} }
