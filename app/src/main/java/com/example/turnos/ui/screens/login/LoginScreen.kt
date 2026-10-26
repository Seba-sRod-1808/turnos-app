package com.example.turnos.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.UserRole
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/**
 * Route: conecta el ViewModel con la Screen y resuelve la navegación.
 */
@Composable
fun LoginRoute(
    onLoggedIn: (UserRole) -> Unit,
    onCreateAccount: () -> Unit,
    viewModel: LoginViewModel = turnosViewModel { LoginViewModel(authRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.loggedInRole) {
        state.loggedInRole?.let { role ->
            onLoggedIn(role)
            viewModel.onEvent(LoginEvent.OnNavigationHandled)
        }
    }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(LoginEvent.OnMessageShown) }

    LoginScreen(
        state = state,
        snackbarHostState = snackbar,
        onEvent = { event ->
            when (event) {
                LoginEvent.OnCreateAccountClick -> onCreateAccount()
                else -> viewModel.onEvent(event)
            }
        },
    )
}

/** Screen stateless: solo pinta [state] y dispara eventos. */
@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = { TurnosTopBar(title = "Autenticación y Perfiles") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                label = "Correo electrónico",
                value = state.email,
                onValueChange = { onEvent(LoginEvent.OnEmailChange(it)) },
                error = state.emailError,
                enabled = !state.isLoading,
                trailingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
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
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                TextLink("¿Olvidó su contraseña?", Modifier.weight(1f)) { onEvent(LoginEvent.OnForgotPasswordClick) }
                TextLink("Crear cuenta") { onEvent(LoginEvent.OnCreateAccountClick) }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = DividerGray)
                Text("O", style = MaterialTheme.typography.bodySmall, color = InkMuted, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(Modifier.weight(1f), color = DividerGray)
            }
            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = { onEvent(LoginEvent.OnGoogleClick) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
            ) {
                Icon(Icons.Outlined.AccountCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Continuar con Google", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(24.dp))
            DemoAccounts(enabled = !state.isLoading, onUse = { onEvent(LoginEvent.OnUseDemoAccount(it)) })
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Accesos rápidos a las cuentas de prueba (útil para revisar la app). */
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

// ---------------------------------------------------------------------------
// Previews: una por estado
// ---------------------------------------------------------------------------

@Composable
private fun Host(state: LoginState) = TurnosTheme { LoginScreen(state, onEvent = {}) }

@Preview(showBackground = true, heightDp = 900, name = "Login - vacío")
@Composable
private fun LoginEmptyPreview() = Host(LoginState())

@Preview(showBackground = true, heightDp = 900, name = "Login - cargando")
@Composable
private fun LoginLoadingPreview() = Host(LoginState(email = "cliente@turnos.com", password = "123456", isLoading = true))

@Preview(showBackground = true, heightDp = 900, name = "Login - correo inválido")
@Composable
private fun LoginEmailErrorPreview() = Host(LoginState(email = "cliente@", password = "123456", emailError = "Ingresa un correo válido"))

@Preview(showBackground = true, heightDp = 900, name = "Login - credenciales incorrectas")
@Composable
private fun LoginErrorPreview() = Host(
    LoginState(email = "cliente@turnos.com", password = "000000", errorMessage = "Correo o contraseña incorrectos."),
)
