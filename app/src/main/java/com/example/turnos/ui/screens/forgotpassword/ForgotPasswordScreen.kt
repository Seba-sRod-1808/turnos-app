package com.example.turnos.ui.screens.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun ForgotPasswordRoute(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = turnosViewModel { ForgotPasswordViewModel(authRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ForgotPasswordScreen(state) { event ->
        when (event) {
            ForgotPasswordEvent.OnBackToLoginClick -> onBackToLogin()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun ForgotPasswordScreen(state: ForgotPasswordState, onEvent: (ForgotPasswordEvent) -> Unit) {
    val onBack = {
        if (state.step == RecoveryStep.SECURITY_QUESTION || state.step == RecoveryStep.NEW_PASSWORD) {
            onEvent(ForgotPasswordEvent.OnStepBackClick)
        } else {
            onEvent(ForgotPasswordEvent.OnBackToLoginClick)
        }
    }
    Scaffold(
        topBar = { TurnosTopBar(title = "Recuperar contraseña", onBack = onBack) },
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
            StepIndicator(state.step)
            state.errorMessage?.let { ErrorBanner(it) }

            when (state.step) {
                RecoveryStep.USERNAME -> {
                    StepHeader(Icons.Outlined.Person, "¿Cuál es tu usuario?", "Es el usuario que te asignó el local.")
                    LabeledTextField(
                        label = "Usuario",
                        value = state.username,
                        onValueChange = { onEvent(ForgotPasswordEvent.OnUsernameChange(it)) },
                        enabled = !state.isLoading,
                    )
                    LoadingPrimaryButton("CONTINUAR", state.isLoading, onClick = { onEvent(ForgotPasswordEvent.OnContinueClick) })
                }
                RecoveryStep.SECURITY_QUESTION -> {
                    StepHeader(Icons.Outlined.QuestionAnswer, state.securityQuestion, "Responde tu pregunta de seguridad.")
                    LabeledTextField(
                        label = "Respuesta",
                        value = state.answer,
                        onValueChange = { onEvent(ForgotPasswordEvent.OnAnswerChange(it)) },
                        enabled = !state.isLoading && !state.isLocked,
                    )
                    if (state.isLocked) {
                        InfoBanner("Por seguridad se bloqueó la recuperación. El local puede darte una contraseña temporal desde su panel de Clientes.")
                    } else {
                        LoadingPrimaryButton("VERIFICAR", state.isLoading, onClick = { onEvent(ForgotPasswordEvent.OnContinueClick) })
                    }
                }
                RecoveryStep.NEW_PASSWORD -> {
                    StepHeader(Icons.Outlined.LockReset, "Crea una nueva contraseña", "Mínimo 6 caracteres.")
                    PasswordField(
                        label = "Nueva contraseña",
                        value = state.newPassword,
                        visible = state.passwordVisible,
                        onValueChange = { onEvent(ForgotPasswordEvent.OnNewPasswordChange(it)) },
                        onToggleVisibility = { onEvent(ForgotPasswordEvent.OnTogglePasswordVisibility) },
                        error = state.passwordError,
                        enabled = !state.isLoading,
                    )
                    PasswordField(
                        label = "Confirmar contraseña",
                        value = state.confirmPassword,
                        visible = state.passwordVisible,
                        onValueChange = { onEvent(ForgotPasswordEvent.OnConfirmPasswordChange(it)) },
                        onToggleVisibility = { onEvent(ForgotPasswordEvent.OnTogglePasswordVisibility) },
                        error = state.confirmError,
                        enabled = !state.isLoading,
                    )
                    LoadingPrimaryButton("GUARDAR CONTRASEÑA", state.isLoading, onClick = { onEvent(ForgotPasswordEvent.OnContinueClick) })
                }
                RecoveryStep.DONE -> {
                    StepHeader(Icons.Outlined.CheckCircle, "¡Listo!", "Tu contraseña se actualizó. Ya puedes iniciar sesión.")
                    PrimaryButton("VOLVER A INICIAR SESIÓN", onClick = { onEvent(ForgotPasswordEvent.OnBackToLoginClick) })
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: RecoveryStep) {
    val total = RecoveryStep.entries.size - 1
    val current = minOf(step.ordinal + 1, total)
    LinearProgressIndicator(
        progress = { current / total.toFloat() },
        color = TurnosOrange,
        trackColor = TurnosOrangeSoft,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepHeader(icon: ImageVector, title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        IconBadge(icon)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Host(state: ForgotPasswordState) = TurnosTheme { ForgotPasswordScreen(state) {} }

@Preview(showBackground = true, name = "Recuperar - usuario")
@Composable
private fun UsernameStepPreview() = Host(ForgotPasswordState(username = "cliente"))

@Preview(showBackground = true, name = "Recuperar - pregunta con error")
@Composable
private fun QuestionStepPreview() = Host(
    ForgotPasswordState(
        step = RecoveryStep.SECURITY_QUESTION,
        username = "cliente",
        securityQuestion = "¿En qué ciudad naciste?",
        answer = "xela",
        attemptsLeft = 2,
        errorMessage = "Respuesta incorrecta. Te quedan 2 intentos.",
    ),
)

@Preview(showBackground = true, name = "Recuperar - bloqueado")
@Composable
private fun LockedPreview() = Host(
    ForgotPasswordState(
        step = RecoveryStep.SECURITY_QUESTION,
        securityQuestion = "¿En qué ciudad naciste?",
        attemptsLeft = 0,
        errorMessage = "Demasiados intentos. Pide al local que restablezca tu contraseña.",
    ),
)

@Preview(showBackground = true, name = "Recuperar - nueva contraseña")
@Composable
private fun NewPasswordPreview() = Host(
    ForgotPasswordState(step = RecoveryStep.NEW_PASSWORD, newPassword = "123", passwordError = "Mínimo 6 caracteres"),
)

@Preview(showBackground = true, name = "Recuperar - listo")
@Composable
private fun DonePreview() = Host(ForgotPasswordState(step = RecoveryStep.DONE))
