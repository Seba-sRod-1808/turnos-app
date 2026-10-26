package com.example.turnos.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.OnNameChange -> _state.update { it.copy(name = event.value, nameError = null) }
            is RegisterEvent.OnEmailChange -> _state.update { it.copy(email = event.value, emailError = null, errorMessage = null) }
            is RegisterEvent.OnPasswordChange -> _state.update { it.copy(password = event.value, passwordError = null) }
            is RegisterEvent.OnConfirmPasswordChange -> _state.update { it.copy(confirmPassword = event.value, confirmError = null) }
            RegisterEvent.OnTogglePasswordVisibility -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is RegisterEvent.OnRoleChange -> _state.update { it.copy(role = event.role) }
            RegisterEvent.OnRegisterClick -> register()
            RegisterEvent.OnNavigationHandled -> _state.update { it.copy(registeredRole = null) }
            RegisterEvent.OnBackClick -> Unit // navegación: la maneja la Route
        }
    }

    private fun register() {
        val s = _state.value
        val validated = s.copy(
            nameError = if (s.name.isBlank()) "Ingresa tu nombre" else null,
            emailError = if (!Validators.isValidEmail(s.email)) "Ingresa un correo válido" else null,
            passwordError = if (!Validators.isValidPassword(s.password)) "Mínimo ${Validators.MIN_PASSWORD_LENGTH} caracteres" else null,
            confirmError = if (s.confirmPassword != s.password) "Las contraseñas no coinciden" else null,
        )
        _state.value = validated
        if (listOf(validated.nameError, validated.emailError, validated.passwordError, validated.confirmError).any { it != null }) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(s.name, s.email, s.password, s.role)
                .onSuccess { user -> _state.update { it.copy(isLoading = false, registeredRole = user.role) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
