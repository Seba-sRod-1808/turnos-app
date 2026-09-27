package com.example.turnos.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val prefs = remember { mutableStateListOf(*MockData.notificationPrefs.toTypedArray()) }
    val templates = remember { mutableStateListOf(*MockData.templates.toTypedArray()) }
    var editing by remember { mutableStateOf<MessageTemplate?>(null) }
    val prefIcons = listOf(
        Icons.Outlined.EventAvailable,
        Icons.Outlined.EventBusy,
        Icons.Outlined.WbSunny,
        Icons.Outlined.Paid,
    )

    TurnosScaffold(
        title = "Notificaciones",
        onBack = onBack,
        actionIcon = Icons.Outlined.NotificationsActive,
        actionDescription = "Probar notificación",
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionHeader("Preferencias operativas", "${prefs.count { it.enabled }} activas")
            TurnosCard(padding = 4.dp) {
                prefs.forEachIndexed { i, p ->
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(prefIcons.getOrElse(i) { Icons.Outlined.Notifications })
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text(p.subtitle, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        TurnosSwitch(p.enabled) { prefs[i] = p.copy(enabled = it) }
                    }
                    if (i < prefs.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 10.dp), color = DividerGray)
                }
            }

            SectionHeader("Plantillas para citas", "Personalizables", Modifier.padding(top = 6.dp))
            templates.forEach { t ->
                TemplateCard(t, onEdit = { editing = t })
            }
            SecondaryButton(
                "NUEVA PLANTILLA",
                icon = Icons.Filled.Add,
                onClick = { editing = MessageTemplate("", "", "", "Al reservar", "WhatsApp", true) },
                modifier = Modifier.fillMaxWidth(),
            )

            InfoBanner(
                "Último envío: 9 de 9 avisos entregados correctamente.",
                icon = Icons.Outlined.MarkChatRead,
                fg = InfoBlue, bg = InfoSoft, border = InfoSoft,
            )
        }
    }

    editing?.let { t ->
        TemplateEditorDialog(
            initial = t,
            onDismiss = { editing = null },
            onSave = { updated ->
                val i = templates.indexOfFirst { it.id == t.id }
                if (t.id.isEmpty() || i < 0) templates.add(updated.copy(id = "m${System.currentTimeMillis()}"))
                else templates[i] = updated
                editing = null
            },
        )
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
private fun TemplateEditorDialog(initial: MessageTemplate, onDismiss: () -> Unit, onSave: (MessageTemplate) -> Unit) {
    var title by remember { mutableStateOf(initial.title) }
    var body by remember { mutableStateOf(initial.preview) }
    var active by remember { mutableStateOf(initial.active) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TurnosOrange, focusedLabelColor = TurnosOrange, cursorColor = TurnosOrange,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text(if (initial.id.isEmpty()) "Nueva plantilla" else "Editar plantilla") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Nombre") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(body, { body = it }, label = { Text("Mensaje") }, minLines = 3, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                Text("Variables: {fecha}, {hora}, {barbero}", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activa", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    TurnosSwitch(active) { active = it }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                onSave(initial.copy(title = title.trim(), preview = body.trim(), active = active))
            }) { Text("Guardar", color = TurnosOrange) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = InkSecondary) } },
    )
}

@Preview(showBackground = true)
@Composable
private fun NotificationsPreview() = TurnosTheme { NotificationsScreen {} }
