package com.example.turnos.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*
import com.example.turnos.util.quetzales

@Composable
fun MyBusinessRoute(
    onSectionClick: (AdminSection) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: MyBusinessViewModel = turnosViewModel {
        MyBusinessViewModel(appointmentRepository, authRepository, sessionRepository, settingsRepository)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(MyBusinessEvent.OnMessageShown) }
    LaunchedEffect(state.isLoggedOut) { if (state.isLoggedOut) onLoggedOut() }

    MyBusinessScreen(state, snackbar) { event ->
        when (event) {
            is MyBusinessEvent.OnSectionClick -> onSectionClick(event.section)
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun MyBusinessScreen(
    state: MyBusinessState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (MyBusinessEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Mi Local",
        actionIcon = Icons.AutoMirrored.Filled.Logout,
        actionDescription = "Cerrar sesión",
        onAction = { onEvent(MyBusinessEvent.OnLogoutClick) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        val profile = state.profile
        if (state.isLoading || profile == null) {
            LoadingContent(Modifier.padding(padding))
            return@TurnosScaffold
        }
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TurnosCard(padding = 12.dp) {
                PhotoPlaceholder(Icons.Outlined.Storefront, Modifier.height(140.dp))
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(profile.name, style = MaterialTheme.typography.titleLarge, color = Ink, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFF5B301), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(profile.rating.toString(), style = MaterialTheme.typography.titleSmall, color = Ink)
                }
                Text(profile.address, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                if (profile.phone.isNotBlank()) {
                    Text("Tel. ${profile.phone}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                Spacer(Modifier.height(12.dp))
                SecondaryButton("Editar Perfil", onClick = { onEvent(MyBusinessEvent.OnEditProfileClick) }, modifier = Modifier.fillMaxWidth(), compact = true)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Citas Hoy", state.appointmentsToday.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                StatCard("Pendientes", state.pending.toString(), Modifier.weight(1f))
                StatCard("Ingresos", quetzales(state.revenueToday), Modifier.weight(1f), valueColor = SuccessGreen)
            }

            SectionHeader("Administración")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.sections.forEach { section ->
                    NavRow(section.icon, section.label) { onEvent(MyBusinessEvent.OnSectionClick(section)) }
                }
            }
        }
    }

    state.profileForm?.let { form -> ProfileDialog(form, onEvent) }
}

@Composable
private fun ProfileDialog(form: ProfileForm, onEvent: (MyBusinessEvent) -> Unit) {
    val change: (ProfileForm) -> Unit = { onEvent(MyBusinessEvent.OnProfileFormChange(it)) }
    AlertDialog(
        onDismissRequest = { onEvent(MyBusinessEvent.OnDismissProfile) },
        containerColor = SurfaceWhite,
        title = { Text("Editar perfil del local") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    form.name, { change(form.copy(name = it, nameError = null)) },
                    label = { Text("Nombre del local") }, singleLine = true, isError = form.nameError != null,
                    supportingText = form.nameError?.let { error -> @Composable { Text(error) } },
                    colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    form.address, { change(form.copy(address = it)) },
                    label = { Text("Dirección") }, colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    form.phone, { change(form.copy(phone = it, phoneError = null)) },
                    label = { Text("Teléfono") }, singleLine = true, isError = form.phoneError != null,
                    supportingText = form.phoneError?.let { error -> @Composable { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onEvent(MyBusinessEvent.OnSaveProfile) }) { Text("Guardar", color = TurnosOrange) } },
        dismissButton = { TextButton(onClick = { onEvent(MyBusinessEvent.OnDismissProfile) }) { Text("Cancelar", color = InkSecondary) } },
    )
}

@Composable
private fun Host(state: MyBusinessState) = TurnosTheme { MyBusinessScreen(state) {} }

@Preview(showBackground = true, heightDp = 900, name = "Mi Local - contenido")
@Composable
private fun MyBusinessPreview() = Host(
    MyBusinessState(profile = PreviewData.business, appointmentsToday = 8, pending = 3, revenueToday = 1200, isLoading = false),
)

@Preview(showBackground = true, name = "Mi Local - editar perfil")
@Composable
private fun MyBusinessEditPreview() = Host(
    MyBusinessState(
        profile = PreviewData.business,
        isLoading = false,
        profileForm = ProfileForm("", "5ta Avenida 12-45, Zona 1", "22", nameError = "El nombre es obligatorio", phoneError = "Teléfono inválido"),
    ),
)

@Preview(showBackground = true, name = "Mi Local - cargando")
@Composable
private fun MyBusinessLoadingPreview() = Host(MyBusinessState(isLoading = true))
