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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.turnos.ui.components.PrimaryButton
import com.example.turnos.ui.components.TurnosTopBar
import com.example.turnos.ui.theme.*

enum class UserRole { CLIENT, BUSINESS }

@Composable
fun LoginScreen(onLogin: (UserRole) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var role by rememberSaveable { mutableStateOf(UserRole.CLIENT) }

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

            // Selector de rol: el entregable contempla clientes y administradores.
            RoleSelector(role = role, onChange = { role = it })
            Spacer(Modifier.height(20.dp))

            FieldLabel("Correo electrónico")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo electrónico") },
                trailingIcon = { Icon(Icons.Outlined.Email, null, tint = TurnosOrange) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.small,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Contraseña")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
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

            PrimaryButton("INICIAR SESIÓN", onClick = { onLogin(role) })
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                TextLink("¿Olvidó su contraseña?", Modifier.weight(1f)) { }
                TextLink("Crear cuenta") { }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = DividerGray)
                Text("O", style = MaterialTheme.typography.bodySmall, color = InkMuted, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(Modifier.weight(1f), color = DividerGray)
            }
            Spacer(Modifier.height(20.dp))

            // TODO: reemplazar por el botón oficial de Google Sign-In (Credential Manager).
            OutlinedButton(
                onClick = { onLogin(role) },
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
private fun RoleSelector(role: UserRole, onChange: (UserRole) -> Unit) {
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
                    .clickable { onChange(r) }
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

@Preview(showBackground = true)
@Composable
private fun LoginPreview() = TurnosTheme { LoginScreen(onLogin = {}) }
