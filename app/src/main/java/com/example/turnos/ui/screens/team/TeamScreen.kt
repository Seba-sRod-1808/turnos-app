package com.example.turnos.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.Barber
import com.example.turnos.data.model.BarberStatus
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TeamScreen(onBack: () -> Unit) {
    val barbers = MockData.barbers
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notify: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }

    Scaffold(
        topBar = {
            TurnosTopBar(
                "Equipo / Barberos",
                onBack = onBack,
                actionIcon = Icons.Outlined.PersonAddAlt,
                actionDescription = "Agregar barbero",
                onAction = { notify("Invitar nuevo barbero") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = ScreenBackground,
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Miembros", barbers.size.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                    StatCard("Disponibles", barbers.count { it.status == BarberStatus.AVAILABLE }.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                    StatCard("Citas hoy", barbers.sumOf { it.appointmentsToday }.toString(), Modifier.weight(1f))
                }
            }
            item { SectionHeader("Miembros del equipo", "Sábado, 15 de junio") }
            items(barbers, key = { it.id }) { b ->
                BarberCard(
                    b,
                    onManage = { notify("Gestionar a ${b.name}") },
                    onAgenda = { notify("Agenda de ${b.name}") },
                )
            }
            item {
                SecondaryButton(
                    "AGREGAR BARBERO",
                    icon = Icons.Outlined.PersonAddAlt,
                    onClick = { notify("Invitar nuevo barbero") },
                    modifier = Modifier.fillMaxWidth(),
                )
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

@Preview(showBackground = true)
@Composable
private fun TeamPreview() = TurnosTheme { TeamScreen {} }
