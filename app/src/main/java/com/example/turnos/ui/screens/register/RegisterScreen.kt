package com.example.turnos.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.UserRole
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun RegisterRoute(
    onRegistered: (UserRole) -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = turnosViewModel { RegisterViewModel(authRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.registeredRole) {
        state.registeredRole?.let { role ->
            onRegistered(role)
            viewModel.onEvent(RegisterEvent.OnNavigationHandled)
        }
    }

    RegisterScreen(state) { event ->
        when (event) {
            RegisterEvent.OnBackClick -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun RegisterScreen(state: RegisterState, onEvent: (RegisterEvent) -> Unit) {
    Scaffold(
        topBar = { TurnosTopBar(title = "Crear cuenta", onBack = { onEvent(RegisterEvent.OnBackClick) }) },
        containerColor = SurfaceWhite,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            BrandMark(Modifier.align(Alignment.CenterHorizontally), size = 80)
            Text(
                "¿Cómo usarás Turnos?",
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
            )
            SegmentedSelector(
                options = listOf(UserRole.CLIENT to "Soy cliente", UserRole.BUSINESS to "Soy negocio"),
                selected = state.role,
                onSelect = { onEvent(RegisterEvent.OnRoleChange(it)) },
                enabled = !state.isLoading,
            )

            state.errorMessage?.let { ErrorBanner(it) }

            LabeledTextField(
                label = "Nombre completo",
                value = state.name,
                onValueChange = { onEvent(RegisterEvent.OnNameChange(it)) },
                error = state.nameError,
                enabled = !state.isLoading,
                trailingIcon = Icons.Outlined.Person,
            )
            LabeledTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = { onEvent(RegisterEvent.OnEmailChange(it)) },
                error = state.emailError,
                enabled = !state.isLoading,
                trailingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
            )
            PasswordField(
                label = "Contraseña",
                value = state.password,
                visible = state.passwordVisible,
                onValueChange = { onEvent(RegisterEvent.OnPasswordChange(it)) },
                onToggleVisibility = { onEvent(RegisterEvent.OnTogglePasswordVisibility) },
                error = state.passwordError,
                enabled = !state.isLoading,
            )
            PasswordField(
                label = "Confirmar contraseña",
                value = state.confirmPassword,
                visible = state.passwordVisible,
                onValueChange = { onEvent(RegisterEvent.OnConfirmPasswordChange(it)) },
                onToggleVisibility = { onEvent(RegisterEvent.OnTogglePasswordVisibility) },
                error = state.confirmError,
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(4.dp))
            LoadingPrimaryButton("CREAR CUENTA", state.isLoading, onClick = { onEvent(RegisterEvent.OnRegisterClick) })
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Composable
private fun Host(state: RegisterState) = TurnosTheme { RegisterScreen(state) {} }

@Preview(showBackground = true, heightDp = 900, name = "Registro - vacío")
@Composable
private fun RegisterEmptyPreview() = Host(RegisterState())

@Preview(showBackground = true, heightDp = 900, name = "Registro - errores de validación")
@Composable
private fun RegisterValidationPreview() = Host(
    RegisterState(
        email = "ana@", password = "123", confirmPassword = "12",
        nameError = "Ingresa tu nombre", emailError = "Ingresa un correo válido",
        passwordError = "Mínimo 6 caracteres", confirmError = "Las contraseñas no coinciden",
    ),
)

@Preview(showBackground = true, heightDp = 900, name = "Registro - correo ya usado")
@Composable
private fun RegisterDuplicatePreview() = Host(
    RegisterState(
        name = "Ana López", email = "cliente@turnos.com", password = "123456", confirmPassword = "123456",
        errorMessage = "Ya existe una cuenta con ese correo.",
    ),
)

@Preview(showBackground = true, heightDp = 900, name = "Registro - creando (negocio)")
@Composable
private fun RegisterLoadingPreview() = Host(
    RegisterState(name = "Mi Barbería", email = "mibarberia@correo.com", role = UserRole.BUSINESS, isLoading = true),
)
