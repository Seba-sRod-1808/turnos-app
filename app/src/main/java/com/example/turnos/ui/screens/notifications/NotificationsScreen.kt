package com.example.turnos.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.model.NotificationPref
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/** Campos del formulario de plantilla. */
data class TemplateForm(
    val id: String? = null, // null = plantilla nueva
    val title: String = "",
    val body: String = "",
    val active: Boolean = true,
) {
    val isNew: Boolean get() = id == null
    val isValid: Boolean get() = title.isNotBlank() && body.isNotBlank()
}

data class NotificationsUiState(
    val prefs: List<NotificationPref> = emptyList(),
    val templates: List<MessageTemplate> = emptyList(),
    val lastDelivery: String? = null,
    /** Formulario abierto; null = diálogo cerrado. */
    val form: TemplateForm? = null,
) {
    val activePrefs: Int get() = prefs.count { it.enabled }
}

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onTogglePref: (NotificationPref, Boolean) -> Unit,
    onEditTemplate: (MessageTemplate) -> Unit,
    onNewTemplate: () -> Unit,
    onFormChange: (TemplateForm) -> Unit,
    onFormSave: () -> Unit,
    onFormDismiss: () -> Unit,
) {
    TurnosScaffold(
        title = "Notificaciones",
        onBack = onBack,
        actionIcon = Icons.Outlined.NotificationsActive,
        actionDescription = "Probar notificación",
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SectionHeader("Preferencias operativas", "${state.activePrefs} activas") }
            item {
                TurnosCard(padding = 4.dp) {
                    state.prefs.forEachIndexed { i, p ->
                        PrefRow(p, prefIcon(p.id)) { onTogglePref(p, it) }
                        if (i < state.prefs.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 10.dp), color = DividerGray)
                    }
                }
            }

            item { SectionHeader("Plantillas para citas", "Personalizables", Modifier.padding(top = 6.dp)) }
            if (state.templates.isEmpty()) {
                item { EmptyContent("No tienes plantillas. Crea una para avisar a tus clientes.", icon = Icons.Outlined.ChatBubbleOutline) }
            }
            items(state.templates, key = { it.id }) { t -> TemplateCard(t, onEdit = { onEditTemplate(t) }) }
            item {
                SecondaryButton("NUEVA PLANTILLA", icon = Icons.Filled.Add, onClick = onNewTemplate, modifier = Modifier.fillMaxWidth())
            }

            state.lastDelivery?.let { msg ->
                item {
                    InfoBanner(msg, icon = Icons.Outlined.MarkChatRead, fg = InfoBlue, bg = InfoSoft, border = InfoSoft)
                }
            }
        }
    }

    state.form?.let { TemplateEditorDialog(it, onFormChange, onFormSave, onFormDismiss) }
}

private fun prefIcon(id: String) = when (id) {
    "n1" -> Icons.Outlined.EventAvailable
    "n2" -> Icons.Outlined.EventBusy
    "n3" -> Icons.Outlined.WbSunny
    "n4" -> Icons.Outlined.Paid
    else -> Icons.Outlined.Notifications
}

@Composable
private fun PrefRow(p: NotificationPref, icon: androidx.compose.ui.graphics.vector.ImageVector, onToggle: (Boolean) -> Unit) {
    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(p.title, style = MaterialTheme.typography.titleSmall, color = Ink)
            Text(p.subtitle, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        TurnosSwitch(p.enabled, onToggle)
    }
}

@Composable
private fun TemplateCard(t: MessageTemplate, onEdit: () -> Unit) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t.title, style = MaterialTheme.typography.titleSmall, color = Ink)
            Spacer(Modifier.width(8.dp))
            if (t.active) StatusChip("Activa", SuccessGreen, SuccessSoft) else StatusChip("Pausada", InkSecondary, NeutralSoft)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Edit, "Editar plantilla", tint = TurnosOrange, modifier = Modifier.size(18.dp))
            }
        }
        Text(t.preview, style = MaterialTheme.typography.bodySmall, color = InkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, null, tint = InkMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(t.timing, style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.weight(1f))
            StatusChip(t.channel, InkSecondary, NeutralSoft)
        }
    }
}

@Composable
private fun TemplateEditorDialog(
    form: TemplateForm,
    onChange: (TemplateForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TurnosOrange, focusedLabelColor = TurnosOrange, cursorColor = TurnosOrange,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text(if (form.isNew) "Nueva plantilla" else "Editar plantilla") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    form.title, { onChange(form.copy(title = it)) }, label = { Text("Nombre") },
                    singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    form.body, { onChange(form.copy(body = it)) }, label = { Text("Mensaje") },
                    minLines = 3, colors = fieldColors, modifier = Modifier.fillMaxWidth(),
                )
                Text("Variables: {fecha}, {hora}, {barbero}", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activa", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    TurnosSwitch(form.active) { onChange(form.copy(active = it)) }
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

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewState = NotificationsUiState(
    prefs = FakeDataSource.notificationPrefs,
    templates = FakeDataSource.templates,
    lastDelivery = "Último envío: 9 de 9 avisos entregados correctamente.",
)

@Composable
private fun NotificationsPreviewHost(state: NotificationsUiState) = TurnosTheme {
    NotificationsScreen(state, {}, { _, _ -> }, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, heightDp = 1000, name = "Notificaciones - configuradas")
@Composable
private fun NotificationsPreview() = NotificationsPreviewHost(previewState)

@Preview(showBackground = true, name = "Notificaciones - sin plantillas")
@Composable
private fun NotificationsEmptyPreview() = NotificationsPreviewHost(
    previewState.copy(templates = emptyList(), lastDelivery = null),
)

@Preview(showBackground = true, name = "Notificaciones - editando plantilla")
@Composable
private fun NotificationsEditPreview() = NotificationsPreviewHost(
    previewState.copy(form = TemplateForm("m2", "Recordatorio de cita", "Te esperamos mañana a las {hora} con {barbero}.")),
)
