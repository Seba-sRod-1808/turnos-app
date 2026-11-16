package com.example.turnos.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.notifications.AppointmentNotifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Las plantillas de confirmación y recordatorio son las que usan las notificaciones reales de la app;
 * el tiempo de la plantilla de recordatorio define con cuántas horas de anticipación se avisa.
 */

class NotificationsViewModel(
    private val repository: SettingsRepository,
    private val notifier: AppointmentNotifier,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        combine(repository.observeNotificationPrefs(), repository.observeTemplates()) { prefs, templates -> prefs to templates }
            .onEach { (prefs, templates) -> _state.update { it.copy(isLoading = false, prefs = prefs, templates = templates) } }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudo cargar la configuración.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            is NotificationsEvent.OnTogglePref -> viewModelScope.launch {
                repository.setPrefEnabled(event.pref.id, event.enabled)
            }
            is NotificationsEvent.OnEditTemplate -> _state.update {
                val t = event.template
                it.copy(form = TemplateForm(t.id, t.title, t.body, t.timing, t.channel, t.active, t.kind))
            }
            NotificationsEvent.OnNewTemplate -> _state.update { it.copy(form = TemplateForm()) }
            is NotificationsEvent.OnFormChange -> _state.update { it.copy(form = event.form) }
            NotificationsEvent.OnSaveForm -> saveForm()
            NotificationsEvent.OnDismissForm -> _state.update { it.copy(form = null) }
            NotificationsEvent.OnSendTestClick -> {
                val sent = notifier.notifyTest()
                _state.update {
                    it.copy(userMessage = if (sent) "Notificación de prueba enviada." else "Activa el permiso de notificaciones de Turnos en Ajustes.")
                }
            }
            NotificationsEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            NotificationsEvent.OnBackClick -> Unit
        }
    }

    private fun saveForm() {
        val f = _state.value.form ?: return
        if (!f.isValid) return
        viewModelScope.launch {
            repository.saveTemplate(MessageTemplate(f.id, f.title.trim(), f.body.trim(), f.timing, f.channel, f.active, f.kind))
            _state.update { it.copy(form = null, userMessage = if (f.isNew) "Plantilla creada." else "Plantilla actualizada.") }
        }
    }
}
