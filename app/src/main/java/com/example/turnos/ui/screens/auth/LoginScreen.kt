package com.example.turnos.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.ui.components.InfoBanner
import com.example.turnos.ui.components.PrimaryButton
import com.example.turnos.ui.components.TurnosTopBar
import com.example.turnos.ui.theme.*

enum class UserRole { CLIENT, BUSINESS }

/** Estado completo de la pantalla de login. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val role: UserRole = UserRole.CLIENT,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val errorMessage: String? = null,
)

/** Composable stateless: solo dibuja [state] y reporta eventos. */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
) {
    Scaffold(
        topBar = { TurnosTopBar(title = "Autenticación y Perfiles") },
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
            Text(
                "Turnos",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                "Tu turno, sin filas.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(24.dp))

            RoleSelector(role = state.role, enabled = !state.isLoading, onChange = onRoleChange)
            Spacer(Modifier.height(20.dp))

            if (state.errorMessage != null) {
                InfoBanner(
                    state.errorMessage,
                    icon = Icons.Outlined.ErrorOutline,
                    fg = DangerRed, bg = DangerSoft, border = DangerRed.copy(alpha = 0.4f),
                )
                Spacer(Modifier.height(16.dp))
            }

            FieldLabel("Correo electrónico")
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = { Text("Correo electrónico") },
                trailingIcon = { Icon(Icons.Outlined.Email, null, tint = TurnosOrange) },
                isError = state.emailError != null,
                supportingText = if (state.emailError != null) {
                    { Text(state.emailError) }
                } else null,
                enabled = !state.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.small,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Contraseña")
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = { Text("Contraseña") },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = InkSecondary,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                shape = MaterialTheme.shapes.small,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(28.dp))
                }
            } else {
                PrimaryButton("INICIAR SESIÓN", onClick = onLoginClick)
            }
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                TextLink("¿Olvidó su contraseña?", Modifier.weight(1f), onForgotPasswordClick)
                TextLink("Crear cuenta", onClick = onCreateAccountClick)
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = DividerGray)
                Text("O", style = MaterialTheme.typography.bodySmall, color = InkMuted, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(Modifier.weight(1f), color = DividerGray)
            }
            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
            ) {
                Icon(Icons.Outlined.AccountCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Continuar con Google", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier.size(112.dp).clip(CircleShape).background(TurnosOrangeSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Chair, contentDescription = null, tint = TurnosOrange, modifier = Modifier.size(64.dp))
        Icon(
            Icons.Filled.ContentCut, contentDescription = null, tint = TurnosOrange,
            modifier = Modifier.size(26.dp).align(Alignment.TopEnd).offset(x = (-14).dp, y = 14.dp),
        )
    }
}

@Composable
private fun RoleSelector(role: UserRole, enabled: Boolean, onChange: (UserRole) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(NeutralSoft).padding(4.dp),
    ) {
        listOf(UserRole.CLIENT to "Soy cliente", UserRole.BUSINESS to "Soy negocio").forEach { (r, label) ->
            val selected = r == role
            Box(
                Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (selected) SurfaceWhite else NeutralSoft)
                    .clickable(enabled = enabled) { onChange(r) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) TurnosOrange else InkSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun TextLink(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = TurnosOrange,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TurnosOrange,
    unfocusedBorderColor = DividerGray,
    cursorColor = TurnosOrange,
    focusedPlaceholderColor = InkMuted,
    unfocusedPlaceholderColor = InkMuted,
)

// ---------------------------------------------------------------------------
// Previews: una por cada estado de la pantalla
// ---------------------------------------------------------------------------

@Composable
private fun LoginPreviewHost(state: LoginUiState) = TurnosTheme {
    LoginScreen(state, {}, {}, {}, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, name = "Login - vacío")
@Composable
private fun LoginEmptyPreview() = LoginPreviewHost(LoginUiState())

@Preview(showBackground = true, name = "Login - llenando (negocio)")
@Composable
private fun LoginFilledPreview() = LoginPreviewHost(
    LoginUiState(email = "demo@turnos.com", password = "123456", role = UserRole.BUSINESS),
)

@Preview(showBackground = true, name = "Login - cargando")
@Composable
private fun LoginLoadingPreview() = LoginPreviewHost(
    LoginUiState(email = "demo@turnos.com", password = "123456", isLoading = true),
)

@Preview(showBackground = true, name = "Login - correo inválido")
@Composable
private fun LoginEmailErrorPreview() = LoginPreviewHost(
    LoginUiState(email = "demo@", password = "123456", emailError = "Ingresa un correo válido"),
)

@Preview(showBackground = true, name = "Login - credenciales incorrectas")
@Composable
private fun LoginErrorPreview() = LoginPreviewHost(
    LoginUiState(email = "demo@turnos.com", password = "000000", errorMessage = "Correo o contraseña incorrectos."),
)
