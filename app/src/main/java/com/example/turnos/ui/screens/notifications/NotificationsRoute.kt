package com.example.turnos.ui.screens.notifications

import androidx.compose.runtime.*
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.source.FakeDataSource

@Composable
fun NotificationsRoute(onBack: () -> Unit = {}) {
    var state by remember {
        mutableStateOf(
            NotificationsUiState(
                prefs = FakeDataSource.notificationPrefs,
                templates = FakeDataSource.templates,
                lastDelivery = "Último envío: 9 de 9 avisos entregados correctamente.",
            )
        )
    }

    NotificationsScreen(
        state = state,
        onBack = onBack,
        onTogglePref = { p, on ->
            state = state.copy(prefs = state.prefs.map { if (it.id == p.id) it.copy(enabled = on) else it })
        },
        onEditTemplate = { t -> state = state.copy(form = TemplateForm(t.id, t.title, t.preview, t.active)) },
        onNewTemplate = { state = state.copy(form = TemplateForm()) },
        onFormChange = { state = state.copy(form = it) },
        onFormSave = {
            state.form?.let { f ->
                val templates = if (f.isNew) {
                    state.templates + MessageTemplate(
                        id = "m${System.currentTimeMillis()}",
                        title = f.title.trim(),
                        preview = f.body.trim(),
                        timing = "Al reservar",
                        channel = "WhatsApp",
                        active = f.active,
                    )
                } else {
                    state.templates.map {
                        if (it.id == f.id) it.copy(title = f.title.trim(), preview = f.body.trim(), active = f.active) else it
                    }
                }
                state = state.copy(templates = templates, form = null)
            }
        },
        onFormDismiss = { state = state.copy(form = null) },
    )
}
