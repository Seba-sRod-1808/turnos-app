package com.example.turnos.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.local.DatabaseSeeder
import com.example.turnos.data.model.UserRole
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de inicio de sesión.
 *
 * Gestiona la autenticación de credenciales, el acceso rápido mediante cuentas de demostración
 * y la redirección hacia la pantalla principal según el rol o al cambio de contraseña temporal.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnUsernameChange -> _state.update { it.copy(username = event.value, errorMessage = null) }
            is LoginEvent.OnPasswordChange -> _state.update { it.copy(password = event.value, errorMessage = null) }
            LoginEvent.OnTogglePasswordVisibility -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is LoginEvent.OnUseDemoAccount -> _state.update {
                it.copy(
                    username = if (event.role == UserRole.CLIENT) DatabaseSeeder.DEMO_CLIENT_USERNAME else DatabaseSeeder.DEMO_BUSINESS_USERNAME,
                    password = DatabaseSeeder.DEMO_PASSWORD,
                    errorMessage = null,
                )
            }
            LoginEvent.OnLoginClick -> login()
            LoginEvent.OnNavigationHandled -> _state.update { it.copy(destination = null) }
            LoginEvent.OnForgotPasswordClick -> Unit
        }
    }

    private fun login() {
        val current = _state.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Completa tu usuario y contraseña.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(current.username, current.password)
                .onSuccess { result ->
                    val destination = when (result) {
                        is LoginResult.LoggedIn -> LoginDestination.Home(result.user.role)
                        is LoginResult.NeedsSetup -> LoginDestination.FirstLoginSetup(result.userId)
                    }
                    _state.update { it.copy(isLoading = false, password = "", destination = destination) }
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
