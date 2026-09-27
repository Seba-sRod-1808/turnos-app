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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.Barber
import com.example.turnos.data.model.BarberStatus
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

data class TeamUiState(
    val barbers: List<Barber> = emptyList(),
    val dateLabel: String = "",
    val isLoading: Boolean = false,
) {
    val availableCount: Int get() = barbers.count { it.status == BarberStatus.AVAILABLE }
    val appointmentsToday: Int get() = barbers.sumOf { it.appointmentsToday }
}

@Composable
fun TeamScreen(
    state: TeamUiState,
    snackbarHostState: SnackbarHostState? = null,
    onBack: () -> Unit,
    onAddBarber: () -> Unit,
    onManage: (Barber) -> Unit,
    onViewAgenda: (Barber) -> Unit,
) {
    TurnosScaffold(
        title = "Equipo / Barberos",
        onBack = onBack,
        actionIcon = Icons.Outlined.PersonAddAlt,
        actionDescription = "Agregar barbero",
        onAction = onAddBarber,
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
                BarberCard(b, onManage = { onManage(b) }, onAgenda = { onViewAgenda(b) })
            }
            item {
                SecondaryButton("AGREGAR BARBERO", icon = Icons.Outlined.PersonAddAlt, onClick = onAddBarber, modifier = Modifier.fillMaxWidth())
            }
        }
    }
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
            NeutralButton("Gestionar", onManage, Modifier.weight(1f))
            SecondaryButton("Ver agenda", onAgenda, Modifier.weight(1f), compact = true)
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Composable
private fun TeamPreviewHost(state: TeamUiState) = TurnosTheme {
    TeamScreen(state = state, onBack = {}, onAddBarber = {}, onManage = {}, onViewAgenda = {})
}

@Preview(showBackground = true, heightDp = 1000, name = "Equipo - con barberos")
@Composable
private fun TeamPreview() = TeamPreviewHost(TeamUiState(FakeDataSource.barbers, "Sábado, 15 de junio"))

@Preview(showBackground = true, name = "Equipo - vacío")
@Composable
private fun TeamEmptyPreview() = TeamPreviewHost(TeamUiState(dateLabel = "Sábado, 15 de junio"))

@Preview(showBackground = true, name = "Equipo - cargando")
@Composable
private fun TeamLoadingPreview() = TeamPreviewHost(TeamUiState(isLoading = true))
