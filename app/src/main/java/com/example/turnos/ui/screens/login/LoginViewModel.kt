package com.example.turnos.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.local.DatabaseSeeder
import com.example.turnos.data.model.UserRole
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange ->
                _state.update { it.copy(email = event.value, emailError = null, errorMessage = null) }
            is LoginEvent.OnPasswordChange ->
                _state.update { it.copy(password = event.value, errorMessage = null) }
            LoginEvent.OnTogglePasswordVisibility ->
                _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is LoginEvent.OnUseDemoAccount -> _state.update {
                it.copy(
                    email = if (event.role == UserRole.CLIENT) DatabaseSeeder.DEMO_CLIENT_EMAIL else DatabaseSeeder.DEMO_BUSINESS_EMAIL,
                    password = DatabaseSeeder.DEMO_PASSWORD,
                    emailError = null,
                    errorMessage = null,
                )
            }
            LoginEvent.OnLoginClick -> login()
            LoginEvent.OnForgotPasswordClick, LoginEvent.OnGoogleClick ->
                _state.update { it.copy(userMessage = "Disponible en una próxima versión.") }
            LoginEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            LoginEvent.OnNavigationHandled -> _state.update { it.copy(loggedInRole = null) }
            LoginEvent.OnCreateAccountClick -> Unit // navegación: la maneja la Route
        }
    }

    private fun login() {
        val current = _state.value
        when {
            current.email.isBlank() || current.password.isBlank() -> {
                _state.update { it.copy(errorMessage = "Completa tu correo y contraseña.") }
                return
            }
            !Validators.isValidEmail(current.email) -> {
                _state.update { it.copy(emailError = "Ingresa un correo válido") }
                return
            }
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, emailError = null) }
            authRepository.login(current.email, current.password)
                .onSuccess { user -> _state.update { it.copy(isLoading = false, password = "", loggedInRole = user.role) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
