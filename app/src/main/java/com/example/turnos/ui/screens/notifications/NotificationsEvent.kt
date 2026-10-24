package com.example.turnos.ui.screens.notifications

import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.model.NotificationPref

sealed interface NotificationsEvent {
    data class OnTogglePref(val pref: NotificationPref, val enabled: Boolean) : NotificationsEvent
    data class OnEditTemplate(val template: MessageTemplate) : NotificationsEvent
    data object OnNewTemplate : NotificationsEvent
    data class OnFormChange(val form: TemplateForm) : NotificationsEvent
    data object OnSaveForm : NotificationsEvent
    data object OnDismissForm : NotificationsEvent
    data object OnSendTestClick : NotificationsEvent
    data object OnMessageShown : NotificationsEvent

    /** Solo navegación: lo resuelve la Route. */
    data object OnBackClick : NotificationsEvent
}
