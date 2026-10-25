package com.example.turnos.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.model.NotificationPref
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = turnosViewModel { NotificationsViewModel(settingsRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(NotificationsEvent.OnMessageShown) }

    NotificationsScreen(state, snackbar) { event ->
        when (event) {
            NotificationsEvent.OnBackClick -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun NotificationsScreen(
    state: NotificationsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (NotificationsEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Notificaciones",
        onBack = { onEvent(NotificationsEvent.OnBackClick) },
        actionIcon = Icons.Outlined.NotificationsActive,
        actionDescription = "Enviar notificación de prueba",
        onAction = { onEvent(NotificationsEvent.OnSendTestClick) },
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
            item { SectionHeader("Preferencias operativas", "${state.activePrefs} activas") }
            item {
                TurnosCard(padding = 4.dp) {
                    state.prefs.forEachIndexed { i, p ->
                        PrefRow(p, prefIcon(p.id)) { onEvent(NotificationsEvent.OnTogglePref(p, it)) }
                        if (i < state.prefs.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 10.dp), color = DividerGray)
                    }
                }
            }

            item { SectionHeader("Plantillas para citas", "Personalizables", Modifier.padding(top = 6.dp)) }
            if (state.templates.isEmpty()) {
                item { EmptyContent("No tienes plantillas. Crea una para avisar a tus clientes.", icon = Icons.Outlined.ChatBubbleOutline) }
            }
            items(state.templates, key = { it.id }) { t -> TemplateCard(t) { onEvent(NotificationsEvent.OnEditTemplate(t)) } }
            item {
                SecondaryButton("NUEVA PLANTILLA", icon = Icons.Filled.Add, onClick = { onEvent(NotificationsEvent.OnNewTemplate) }, modifier = Modifier.fillMaxWidth())
            }

            state.lastDelivery?.let { msg ->
                item { InfoBanner(msg, icon = Icons.Outlined.MarkChatRead, fg = InfoBlue, bg = InfoSoft, border = InfoSoft) }
            }
        }
    }

    state.form?.let { TemplateEditorDialog(it, onEvent) }
}

private fun prefIcon(id: String): ImageVector = when (id) {
    "new" -> Icons.Outlined.EventAvailable
    "changes" -> Icons.Outlined.EventBusy
    "daily" -> Icons.Outlined.WbSunny
    "payments" -> Icons.Outlined.Paid
    else -> Icons.Outlined.Notifications
}

@Composable
private fun PrefRow(p: NotificationPref, icon: ImageVector, onToggle: (Boolean) -> Unit) {
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
        Text(t.body, style = MaterialTheme.typography.bodySmall, color = InkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun TemplateEditorDialog(form: TemplateForm, onEvent: (NotificationsEvent) -> Unit) {
    val change: (TemplateForm) -> Unit = { onEvent(NotificationsEvent.OnFormChange(it)) }
    AlertDialog(
        onDismissRequest = { onEvent(NotificationsEvent.OnDismissForm) },
        containerColor = SurfaceWhite,
        title = { Text(if (form.isNew) "Nueva plantilla" else "Editar plantilla") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    form.title, { change(form.copy(title = it)) }, label = { Text("Nombre") },
                    singleLine = true, colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    form.body, { change(form.copy(body = it)) }, label = { Text("Mensaje") },
                    minLines = 3, colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
                Text("Variables: {fecha}, {hora}, {barbero}", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                SegmentedSelector(
                    options = listOf("WhatsApp" to "WhatsApp", "SMS" to "SMS"),
                    selected = form.channel,
                    onSelect = { change(form.copy(channel = it)) },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activa", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    TurnosSwitch(form.active) { change(form.copy(active = it)) }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = form.isValid, onClick = { onEvent(NotificationsEvent.OnSaveForm) }) {
                Text("Guardar", color = if (form.isValid) TurnosOrange else InkMuted)
            }
        },
        dismissButton = { TextButton(onClick = { onEvent(NotificationsEvent.OnDismissForm) }) { Text("Cancelar", color = InkSecondary) } },
    )
}

private val sample = NotificationsState(prefs = PreviewData.notificationPrefs, templates = PreviewData.templates, isLoading = false)

@Composable
private fun Host(state: NotificationsState) = TurnosTheme { NotificationsScreen(state) {} }

@Preview(showBackground = true, heightDp = 1000, name = "Notificaciones - configuradas")
@Composable
private fun NotificationsPreview() = Host(sample)

@Preview(showBackground = true, name = "Notificaciones - sin plantillas")
@Composable
private fun NotificationsEmptyPreview() = Host(sample.copy(templates = emptyList(), lastDelivery = null))

@Preview(showBackground = true, name = "Notificaciones - editando plantilla")
@Composable
private fun NotificationsEditPreview() = Host(
    sample.copy(form = TemplateForm(2, "Recordatorio de cita", "Te esperamos mañana a las {hora} con {barbero}.", "24 h antes")),
)
