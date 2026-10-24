package com.example.turnos.ui.screens.notifications

import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.model.NotificationPref

/** Campos del formulario de plantilla. */
data class TemplateForm(
    val id: Long = 0, // 0 = plantilla nueva
    val title: String = "",
    val body: String = "",
    val timing: String = "Al reservar",
    val channel: String = "WhatsApp",
    val active: Boolean = true,
) {
    val isNew: Boolean get() = id == 0L
    val isValid: Boolean get() = title.isNotBlank() && body.isNotBlank()
}

data class NotificationsState(
    val prefs: List<NotificationPref> = emptyList(),
    val templates: List<MessageTemplate> = emptyList(),
    val lastDelivery: String? = "Último envío: 9 de 9 avisos entregados correctamente.",
    /** Formulario abierto; null = diálogo cerrado. */
    val form: TemplateForm? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
) {
    val activePrefs: Int get() = prefs.count { it.enabled }
}
