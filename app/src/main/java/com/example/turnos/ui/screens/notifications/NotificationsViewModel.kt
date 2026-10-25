package com.example.turnos.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationsViewModel(private val repository: SettingsRepository) : ViewModel() {

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
                it.copy(form = TemplateForm(t.id, t.title, t.body, t.timing, t.channel, t.active))
            }
            NotificationsEvent.OnNewTemplate -> _state.update { it.copy(form = TemplateForm()) }
            is NotificationsEvent.OnFormChange -> _state.update { it.copy(form = event.form) }
            NotificationsEvent.OnSaveForm -> saveForm()
            NotificationsEvent.OnDismissForm -> _state.update { it.copy(form = null) }
            NotificationsEvent.OnSendTestClick ->
                _state.update { it.copy(userMessage = "Notificación de prueba enviada (simulada).") }
            NotificationsEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            NotificationsEvent.OnBackClick -> Unit
        }
    }

    private fun saveForm() {
        val f = _state.value.form ?: return
        if (!f.isValid) return
        viewModelScope.launch {
            repository.saveTemplate(MessageTemplate(f.id, f.title.trim(), f.body.trim(), f.timing, f.channel, f.active))
            _state.update { it.copy(form = null, userMessage = if (f.isNew) "Plantilla creada." else "Plantilla actualizada.") }
        }
    }
}
