package com.example.turnos.ui.screens.auth

import android.util.Patterns
import androidx.compose.runtime.*
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Route del login: maneja el estado y accede a la fuente de datos fake.
 * Credenciales de prueba: demo@turnos.com / 123456
 */
@Composable
fun LoginRoute(onLoginSuccess: (UserRole) -> Unit = {}) {
    var state by remember { mutableStateOf(LoginUiState()) }
    val scope = rememberCoroutineScope()

    fun submit() {
        val email = state.email.trim()
        when {
            email.isBlank() || state.password.isBlank() ->
                state = state.copy(errorMessage = "Completa tu correo y contraseña.", emailError = null)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                state = state.copy(emailError = "Ingresa un correo válido", errorMessage = null)
            else -> scope.launch {
                state = state.copy(isLoading = true, emailError = null, errorMessage = null)
                delay(800) // simula la llamada al servidor
                if (FakeDataSource.login(email, state.password)) {
                    state = state.copy(isLoading = false)
                    onLoginSuccess(state.role)
                } else {
                    state = state.copy(isLoading = false, errorMessage = "Correo o contraseña incorrectos.")
                }
            }
        }
    }

    LoginScreen(
        state = state,
        onEmailChange = { state = state.copy(email = it, emailError = null, errorMessage = null) },
        onPasswordChange = { state = state.copy(password = it, errorMessage = null) },
        onTogglePasswordVisibility = { state = state.copy(passwordVisible = !state.passwordVisible) },
        onRoleChange = { state = state.copy(role = it) },
        onLoginClick = { submit() },
        onGoogleClick = { onLoginSuccess(state.role) }, // TODO: Google Sign-In real
        onForgotPasswordClick = { },
        onCreateAccountClick = { },
    )
}
