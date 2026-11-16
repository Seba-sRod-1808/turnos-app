package com.example.turnos.ui.screens.setupaccount

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.UserRole
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun SetupAccountRoute(
    onCompleted: (UserRole) -> Unit,
    onCancel: () -> Unit,
    viewModel: SetupAccountViewModel = turnosViewModel { handle -> SetupAccountViewModel(handle, authRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.completedRole) {
        state.completedRole?.let { role ->
            onCompleted(role)
            viewModel.onEvent(SetupAccountEvent.OnNavigationHandled)
        }
    }

    SetupAccountScreen(state) { event ->
        when (event) {
            SetupAccountEvent.OnCancelClick -> onCancel()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun SetupAccountScreen(state: SetupAccountState, onEvent: (SetupAccountEvent) -> Unit) {
    Scaffold(
        topBar = { TurnosTopBar(title = "Configura tu cuenta", onBack = { onEvent(SetupAccountEvent.OnCancelClick) }) },
        containerColor = SurfaceWhite,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (state.userName.isBlank()) "¡Bienvenido!" else "¡Bienvenido, ${state.userName}!",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                "Entraste con la contraseña temporal del local. Crea tu propia contraseña y una pregunta de seguridad para recuperarla si la olvidas.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
            )
            state.errorMessage?.let { ErrorBanner(it) }

            PasswordField(
                label = "Nueva contraseña",
                value = state.newPassword,
                visible = state.passwordVisible,
                onValueChange = { onEvent(SetupAccountEvent.OnNewPasswordChange(it)) },
                onToggleVisibility = { onEvent(SetupAccountEvent.OnTogglePasswordVisibility) },
                error = state.passwordError,
                enabled = !state.isSaving,
            )
            PasswordField(
                label = "Confirmar contraseña",
                value = state.confirmPassword,
                visible = state.passwordVisible,
                onValueChange = { onEvent(SetupAccountEvent.OnConfirmPasswordChange(it)) },
                onToggleVisibility = { onEvent(SetupAccountEvent.OnTogglePasswordVisibility) },
                error = state.confirmError,
                enabled = !state.isSaving,
            )

            SectionHeader("Pregunta de seguridad")
            TurnosCard(padding = 4.dp) {
                state.questions.forEach { question ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = question == state.selectedQuestion,
                                enabled = !state.isSaving,
                                role = Role.RadioButton,
                                onClick = { onEvent(SetupAccountEvent.OnQuestionSelected(question)) },
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = question == state.selectedQuestion,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = TurnosOrange),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(question, style = MaterialTheme.typography.bodyMedium, color = Ink)
                    }
                }
            }
            LabeledTextField(
                label = "Respuesta",
                value = state.answer,
                onValueChange = { onEvent(SetupAccountEvent.OnAnswerChange(it)) },
                error = state.answerError,
                enabled = !state.isSaving,
            )
            Text(
                "No importan mayúsculas ni tildes al responder.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            LoadingPrimaryButton("GUARDAR Y ENTRAR", state.isSaving, onClick = { onEvent(SetupAccountEvent.OnSaveClick) })
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Host(state: SetupAccountState) = TurnosTheme { SetupAccountScreen(state) {} }

@Preview(showBackground = true, heightDp = 1000, name = "Primer ingreso - vacío")
@Composable
private fun SetupEmptyPreview() = Host(SetupAccountState(userName = "Pedro"))

@Preview(showBackground = true, heightDp = 1000, name = "Primer ingreso - errores")
@Composable
private fun SetupErrorsPreview() = Host(
    SetupAccountState(
        userName = "Pedro",
        newPassword = "123",
        confirmPassword = "12",
        passwordError = "Mínimo 6 caracteres",
        confirmError = "Las contraseñas no coinciden",
        answerError = "Escribe una respuesta que recuerdes",
    ),
)
