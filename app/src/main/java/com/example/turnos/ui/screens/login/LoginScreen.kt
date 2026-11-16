package com.example.turnos.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.UserRole
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun LoginRoute(
    onLoggedIn: (UserRole) -> Unit,
    onFirstLogin: (userId: Long) -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: LoginViewModel = turnosViewModel { LoginViewModel(authRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.destination) {
        when (val destination = state.destination) {
            is LoginDestination.Home -> onLoggedIn(destination.role)
            is LoginDestination.FirstLoginSetup -> onFirstLogin(destination.userId)
            null -> return@LaunchedEffect
        }
        viewModel.onEvent(LoginEvent.OnNavigationHandled)
    }

    LoginScreen(state) { event ->
        when (event) {
            LoginEvent.OnForgotPasswordClick -> onForgotPassword()
            else -> viewModel.onEvent(event)
        }
    }
}

/**
 * Las cuentas de clientes las crea el dueño del local; por eso no hay registro público.
 */

@Composable
fun LoginScreen(state: LoginState, onEvent: (LoginEvent) -> Unit) {
    Scaffold(
        topBar = { TurnosTopBar(title = "Iniciar sesión") },
        containerColor = SurfaceWhite,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(28.dp))
            BrandMark(Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(8.dp))
            Text("Turnos", style = MaterialTheme.typography.headlineMedium, color = Ink, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                "Tu turno, sin filas.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(24.dp))

            state.errorMessage?.let {
                ErrorBanner(it)
                Spacer(Modifier.height(16.dp))
            }

            LabeledTextField(
                label = "Usuario",
                value = state.username,
                onValueChange = { onEvent(LoginEvent.OnUsernameChange(it)) },
                placeholder = "Usuario asignado por el local",
                enabled = !state.isLoading,
                trailingIcon = Icons.Outlined.Person,
            )
            Spacer(Modifier.height(16.dp))
            PasswordField(
                label = "Contraseña",
                value = state.password,
                visible = state.passwordVisible,
                onValueChange = { onEvent(LoginEvent.OnPasswordChange(it)) },
                onToggleVisibility = { onEvent(LoginEvent.OnTogglePasswordVisibility) },
                enabled = !state.isLoading,
                imeAction = ImeAction.Done,
            )
            Spacer(Modifier.height(20.dp))

            LoadingPrimaryButton("INICIAR SESIÓN", state.isLoading, onClick = { onEvent(LoginEvent.OnLoginClick) })
            Spacer(Modifier.height(8.dp))
            TextLink("¿Olvidó su contraseña?", Modifier.align(Alignment.CenterHorizontally)) {
                onEvent(LoginEvent.OnForgotPasswordClick)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "¿No tienes cuenta? Pídela en tu barbería: ahí te asignan usuario y contraseña.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(24.dp))
            DemoAccounts(enabled = !state.isLoading, onUse = { onEvent(LoginEvent.OnUseDemoAccount(it)) })
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DemoAccounts(enabled: Boolean, onUse: (UserRole) -> Unit) {
    TurnosCard(padding = 12.dp) {
        Text("Cuentas de prueba", style = MaterialTheme.typography.titleSmall, color = Ink)
        Text("Contraseña: 123456", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeutralButton("Cliente", { if (enabled) onUse(UserRole.CLIENT) }, Modifier.weight(1f))
            NeutralButton("Negocio", { if (enabled) onUse(UserRole.BUSINESS) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Host(state: LoginState) = TurnosTheme { LoginScreen(state) {} }

@Preview(showBackground = true, heightDp = 900, name = "Login - vacío")
@Composable
private fun LoginEmptyPreview() = Host(LoginState())

@Preview(showBackground = true, heightDp = 900, name = "Login - cargando")
@Composable
private fun LoginLoadingPreview() = Host(LoginState(username = "cliente", password = "123456", isLoading = true))

@Preview(showBackground = true, heightDp = 900, name = "Login - credenciales incorrectas")
@Composable
private fun LoginErrorPreview() = Host(
    LoginState(username = "cliente", password = "000000", errorMessage = "Usuario o contraseña incorrectos."),
)
