package com.example.turnos.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.User
import com.example.turnos.data.repository.ClientCredentials
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*

@Composable
fun ClientsRoute(
    onBack: () -> Unit,
    viewModel: ClientsViewModel = turnosViewModel { ClientsViewModel(clientRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(ClientsEvent.OnMessageShown) }

    ClientsScreen(state, snackbar) { event ->
        when (event) {
            ClientsEvent.OnBackClick -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun ClientsScreen(
    state: ClientsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (ClientsEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Clientes y accesos",
        onBack = { onEvent(ClientsEvent.OnBackClick) },
        actionIcon = Icons.Outlined.PersonAddAlt,
        actionDescription = "Nuevo cliente",
        onAction = { onEvent(ClientsEvent.OnAddClick) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        if (state.isLoading) {
            LoadingContent(Modifier.padding(padding), "Cargando clientes…")
            return@TurnosScaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                InfoBanner(
                    "Crea la cuenta de cada cliente y entrégale su usuario y contraseña temporal. En su primer ingreso elegirá su propia contraseña.",
                    icon = Icons.Outlined.Key,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Clientes", state.clients.size.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                    StatCard("Sin primer ingreso", state.pendingFirstLogin.toString(), Modifier.weight(1f))
                }
            }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onEvent(ClientsEvent.OnQueryChange(it)) },
                    placeholder = { Text("Buscar por nombre o usuario") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = turnosFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (state.visibleClients.isEmpty()) {
                item {
                    EmptyContent(
                        if (state.query.isBlank()) "Aún no tienes clientes registrados." else "Ningún cliente coincide con “${state.query}”.",
                        icon = Icons.Outlined.People,
                    )
                }
            }
            items(state.visibleClients, key = { it.id }) { client ->
                ClientCard(client) { onEvent(ClientsEvent.OnResetPasswordClick(client)) }
            }
            item {
                SecondaryButton(
                    "NUEVO CLIENTE",
                    icon = Icons.Outlined.PersonAddAlt,
                    onClick = { onEvent(ClientsEvent.OnAddClick) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    state.form?.let { NewClientDialog(it, state.isSaving, onEvent) }
    state.clientToReset?.let { client ->
        AlertDialog(
            onDismissRequest = { onEvent(ClientsEvent.OnDismissReset) },
            containerColor = SurfaceWhite,
            title = { Text("¿Restablecer contraseña?") },
            text = {
                Text("Se generará una contraseña temporal para ${client.name}. Al ingresar con ella deberá crear una nueva.")
            },
            confirmButton = { TextButton(onClick = { onEvent(ClientsEvent.OnConfirmReset) }) { Text("Restablecer", color = TurnosOrange) } },
            dismissButton = { TextButton(onClick = { onEvent(ClientsEvent.OnDismissReset) }) { Text("Cancelar", color = InkSecondary) } },
        )
    }
    state.credentials?.let { CredentialsDialog(it) { onEvent(ClientsEvent.OnCredentialsDismissed) } }
}

@Composable
private fun ClientCard(client: User, onResetPassword: () -> Unit) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(client.name, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(client.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    "@${client.username}" + if (client.phone.isNotBlank()) " · ${client.phone}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                )
            }
            if (client.mustChangePassword) {
                StatusChip("Sin ingresar", WarningAmber, WarningSoft)
            } else {
                StatusChip("Activo", SuccessGreen, SuccessSoft)
            }
        }
        Spacer(Modifier.height(10.dp))
        NeutralButton("Restablecer contraseña", onResetPassword, Modifier.fillMaxWidth())
    }
}

@Composable
private fun NewClientDialog(form: NewClientForm, isSaving: Boolean, onEvent: (ClientsEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onEvent(ClientsEvent.OnDismissForm) },
        containerColor = SurfaceWhite,
        title = { Text("Nuevo cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField("Nombre completo", form.name, form.nameError) { onEvent(ClientsEvent.OnNameChange(it)) }
                DialogField("Usuario", form.username, form.usernameError) { onEvent(ClientsEvent.OnUsernameChange(it)) }
                DialogField("Teléfono (opcional)", form.phone, form.phoneError, KeyboardType.Phone) {
                    onEvent(ClientsEvent.OnPhoneChange(it))
                }
                Text(
                    "La contraseña temporal se genera al guardar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = !isSaving, onClick = { onEvent(ClientsEvent.OnSaveClient) }) {
                Text(if (isSaving) "Guardando…" else "Crear cuenta", color = TurnosOrange)
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = { onEvent(ClientsEvent.OnDismissForm) }) { Text("Cancelar", color = InkSecondary) }
        },
    )
}

@Composable
private fun DialogField(
    label: String,
    value: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = turnosFieldColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CredentialsDialog(credentials: ClientCredentials, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        icon = { Icon(Icons.Outlined.LockReset, null, tint = TurnosOrange) },
        title = { Text("Credenciales de ${credentials.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Entrégale estos datos al cliente. La contraseña no se volverá a mostrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary,
                )
                CredentialRow("Usuario", credentials.username)
                CredentialRow("Contraseña temporal", credentials.temporaryPassword)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Listo", color = TurnosOrange) } },
    )
}

@Composable
private fun CredentialRow(label: String, value: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(NeutralSoft)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = InkSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Ink, fontFamily = FontFamily.Monospace)
    }
}

private val sample = ClientsState(clients = PreviewData.clients, isLoading = false)

@Composable
private fun Host(state: ClientsState) = TurnosTheme { ClientsScreen(state) {} }

@Preview(showBackground = true, heightDp = 900, name = "Clientes - lista")
@Composable
private fun ClientsPreview() = Host(sample)

@Preview(showBackground = true, name = "Clientes - nuevo con errores")
@Composable
private fun NewClientPreview() = Host(
    sample.copy(form = NewClientForm(name = "María Pérez", username = "ma", usernameError = "3 a 20 caracteres: letras, números, punto o guion bajo")),
)

@Preview(showBackground = true, name = "Clientes - credenciales generadas")
@Composable
private fun CredentialsPreview() = Host(sample.copy(credentials = ClientCredentials("María Pérez", "maria.perez", "k7hm4qzt")))

@Preview(showBackground = true, name = "Clientes - vacío")
@Composable
private fun ClientsEmptyPreview() = Host(ClientsState(isLoading = false))
