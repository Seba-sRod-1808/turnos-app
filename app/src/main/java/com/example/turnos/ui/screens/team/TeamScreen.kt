package com.example.turnos.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.Barber
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*

@Composable
fun TeamRoute(
    onBack: () -> Unit,
    onViewAgenda: () -> Unit,
    viewModel: TeamViewModel = turnosViewModel { TeamViewModel(barberRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(TeamEvent.OnMessageShown) }

    TeamScreen(state, snackbar) { event ->
        when (event) {
            TeamEvent.OnBackClick -> onBack()
            is TeamEvent.OnViewAgendaClick -> onViewAgenda()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun TeamScreen(
    state: TeamState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (TeamEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Equipo / Barberos",
        onBack = { onEvent(TeamEvent.OnBackClick) },
        actionIcon = Icons.Outlined.PersonAddAlt,
        actionDescription = "Agregar barbero",
        onAction = { onEvent(TeamEvent.OnAddBarberClick) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        if (state.isLoading) {
            LoadingContent(Modifier.padding(padding), "Cargando equipo…")
            return@TurnosScaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Miembros", state.barbers.size.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                    StatCard("Disponibles", state.availableCount.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                    StatCard("Citas hoy", state.appointmentsToday.toString(), Modifier.weight(1f))
                }
            }
            item { SectionHeader("Miembros del equipo", state.dateLabel) }
            if (state.barbers.isEmpty()) {
                item { EmptyContent("Todavía no hay barberos en tu equipo.", icon = Icons.Outlined.Groups) }
            }
            items(state.barbers, key = { it.id }) { b ->
                BarberCard(
                    b,
                    onManage = { onEvent(TeamEvent.OnManageClick(b)) },
                    onAgenda = { onEvent(TeamEvent.OnViewAgendaClick(b)) },
                )
            }
            item {
                SecondaryButton(
                    "AGREGAR BARBERO",
                    icon = Icons.Outlined.PersonAddAlt,
                    onClick = { onEvent(TeamEvent.OnAddBarberClick) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    state.form?.let { form -> BarberFormDialog(form, onEvent) }
}

@Composable
private fun BarberCard(b: Barber, onManage: () -> Unit, onAgenda: () -> Unit) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(b.name, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(b.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(b.role, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
            BarberStatusChip(b.status)
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGray)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, null, tint = InkSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(b.schedule, style = MaterialTheme.typography.bodySmall, color = Ink, modifier = Modifier.weight(1f))
            Text(
                if (b.appointmentsToday > 0) "${b.appointmentsToday} citas hoy" else "Sin citas",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeutralButton("Cambiar estado", onManage, Modifier.weight(1f))
            SecondaryButton("Ver agenda", onAgenda, Modifier.weight(1f), compact = true)
        }
    }
}

@Composable
private fun BarberFormDialog(form: BarberForm, onEvent: (TeamEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(TeamEvent.OnDismissForm) },
        containerColor = SurfaceWhite,
        title = { Text("Agregar barbero") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    form.name, { onEvent(TeamEvent.OnFormChange(form.copy(name = it))) },
                    label = { Text("Nombre") }, singleLine = true, colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    form.role, { onEvent(TeamEvent.OnFormChange(form.copy(role = it))) },
                    label = { Text("Puesto") }, singleLine = true, colors = turnosFieldColors(), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = form.isValid, onClick = { onEvent(TeamEvent.OnSaveBarber) }) {
                Text("Agregar", color = if (form.isValid) TurnosOrange else InkMuted)
            }
        },
        dismissButton = { TextButton(onClick = { onEvent(TeamEvent.OnDismissForm) }) { Text("Cancelar", color = InkSecondary) } },
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Composable
private fun Host(state: TeamState) = TurnosTheme { TeamScreen(state) {} }

@Preview(showBackground = true, heightDp = 1000, name = "Equipo - con barberos")
@Composable
private fun TeamPreview() = Host(TeamState(PreviewData.barbers, "Lunes, 15 de junio", isLoading = false))

@Preview(showBackground = true, name = "Equipo - vacío")
@Composable
private fun TeamEmptyPreview() = Host(TeamState(dateLabel = "Lunes, 15 de junio", isLoading = false))

@Preview(showBackground = true, name = "Equipo - agregar barbero")
@Composable
private fun TeamFormPreview() = Host(TeamState(PreviewData.barbers, "Lunes, 15 de junio", isLoading = false, form = BarberForm("Carlos Pérez")))

@Preview(showBackground = true, name = "Equipo - cargando")
@Composable
private fun TeamLoadingPreview() = Host(TeamState(isLoading = true))
