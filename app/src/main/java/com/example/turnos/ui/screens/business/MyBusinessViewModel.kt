package com.example.turnos.ui.screens.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Las estadísticas se recalculan solas cada vez que cambia una cita.
 */

class MyBusinessViewModel(
    appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    session: SessionRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MyBusinessState(ownerName = session.currentUser.value?.name.orEmpty()))
    val state: StateFlow<MyBusinessState> = _state.asStateFlow()

    init {
        settings.observeBusinessProfile()
            .onEach { profile -> _state.update { it.copy(profile = profile) } }
            .launchIn(viewModelScope)

        appointmentRepository.observeAll()
            .onEach { all ->
                val today = all.filter { it.date == LocalDate.now() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        appointmentsToday = today.count { a -> a.isActive || a.status == AppointmentStatus.COMPLETED },
                        pending = all.count { a -> a.status == AppointmentStatus.PENDING },
                        revenueToday = today
                            .filter { a -> a.status == AppointmentStatus.COMPLETED || a.status == AppointmentStatus.CONFIRMED }
                            .sumOf { a -> a.price },
                    )
                }
            }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudieron cargar las estadísticas.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: MyBusinessEvent) {
        when (event) {
            MyBusinessEvent.OnEditProfileClick -> _state.update { s ->
                val profile = s.profile ?: return@update s
                s.copy(profileForm = ProfileForm(profile.name, profile.address, profile.phone))
            }
            is MyBusinessEvent.OnProfileFormChange -> _state.update { it.copy(profileForm = event.form) }
            MyBusinessEvent.OnDismissProfile -> _state.update { it.copy(profileForm = null) }
            MyBusinessEvent.OnSaveProfile -> saveProfile()
            MyBusinessEvent.OnLogoutClick -> {
                authRepository.logout()
                _state.update { it.copy(isLoggedOut = true) }
            }
            MyBusinessEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            is MyBusinessEvent.OnSectionClick -> Unit
        }
    }

    private fun saveProfile() {
        val form = _state.value.profileForm ?: return
        val profile = _state.value.profile ?: return
        val validated = form.copy(
            nameError = if (form.name.isBlank()) "El nombre es obligatorio" else null,
            phoneError = if (!Validators.isValidPhone(form.phone)) "Teléfono inválido" else null,
        )
        if (validated.nameError != null || validated.phoneError != null) {
            _state.update { it.copy(profileForm = validated) }
            return
        }
        viewModelScope.launch {
            settings.saveBusinessProfile(
                profile.copy(name = form.name.trim(), address = form.address.trim(), phone = form.phone.trim()),
            )
            _state.update { it.copy(profileForm = null, userMessage = "Perfil actualizado.") }
        }
    }
}
