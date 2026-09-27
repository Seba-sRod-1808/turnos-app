package com.example.turnos.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.ui.components.*
import com.example.turnos.ui.screens.catalog.ClientBottomBar
import com.example.turnos.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Historial de citas. La misma pantalla sirve a ambos roles:
 * - Cliente: puede cancelar o reprogramar.
 * - Negocio: puede marcar como completada o no presente.
 */
@Composable
fun HistoryScreen(
    isBusiness: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val upcoming = remember { mutableStateListOf(*MockData.upcoming.toTypedArray()) }
    val past = remember { mutableStateListOf(*MockData.past.toTypedArray()) }
    var toCancel by remember { mutableStateOf<Appointment?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun moveToPast(a: Appointment, status: AppointmentStatus) {
        upcoming.remove(a)
        past.add(0, a.copy(status = status))
    }

    Scaffold(
        topBar = {
            Column {
                TurnosTopBar("Historial de Citas", onBack = onBack)
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = SurfaceWhite,
                    contentColor = TurnosOrange,
                    indicator = { positions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(positions[tab]),
                            color = TurnosOrange,
                        )
                    },
                ) {
                    listOf("Próximas", "Pasadas").forEachIndexed { i, label ->
                        Tab(
                            selected = tab == i,
                            onClick = { tab = i },
                            selectedContentColor = TurnosOrange,
                            unselectedContentColor = InkSecondary,
                            text = { Text(label, style = MaterialTheme.typography.titleSmall) },
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isBusiness) ClientBottomBar(selected = 1, onServices = onBack, onAppointments = {}, onLogout = onLogout)
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = ScreenBackground,
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (tab == 0) {
                item { Overline("Próximas citas") }
                if (upcoming.isEmpty()) item { EmptyState("No tienes citas próximas.") }
                items(upcoming, key = { it.id }) { a ->
                    AppointmentCard(a) {
                        if (isBusiness) {
                            NeutralButton("No presente", { moveToPast(a, AppointmentStatus.NO_SHOW) }, Modifier.weight(1f))
                            SecondaryButton("Completada", { moveToPast(a, AppointmentStatus.COMPLETED) }, Modifier.weight(1f), compact = true)
                        } else {
                            NeutralButton("Cancelar", { toCancel = a }, Modifier.weight(1f))
                            SecondaryButton(
                                "Reprogramar",
                                { scope.launch { snackbar.showSnackbar("Elige un nuevo horario para ${a.service}") } },
                                Modifier.weight(1f),
                                compact = true,
                            )
                        }
                    }
                }
                past.firstOrNull()?.let { last ->
                    item { Overline("Último historial", Modifier.padding(top = 8.dp)) }
                    item(key = "last-${last.id}") { AppointmentCard(last, dimmed = true) }
                }
            } else {
                item { Overline("Citas pasadas") }
                if (past.isEmpty()) item { EmptyState("Aún no hay historial.") }
                items(past, key = { it.id }) { AppointmentCard(it, dimmed = true) }
            }

            if (isBusiness) {
                item {
                    InfoBanner(
                        "El administrador puede marcar citas como completadas o no presentes desde este panel.",
                        icon = Icons.Outlined.Info,
                    )
                }
            }
        }
    }

    toCancel?.let { a ->
        AlertDialog(
            onDismissRequest = { toCancel = null },
            title = { Text("¿Cancelar cita?") },
            text = {
                Text("${a.service} · ${a.dateLabel}\n\nSi faltan menos de 2 horas, se registrará como cancelación tardía.")
            },
            confirmButton = {
                TextButton(onClick = { moveToPast(a, AppointmentStatus.CANCELLED); toCancel = null }) {
                    Text("Cancelar cita", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { toCancel = null }) { Text("Volver", color = InkSecondary) }
            },
            containerColor = SurfaceWhite,
        )
    }
}

@Composable
private fun AppointmentCard(
    a: Appointment,
    dimmed: Boolean = false,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = if (dimmed) Modifier.alpha(0.75f) else Modifier) {
            Avatar(a.barber, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.service, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text("Con ${a.barber}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
            AppointmentStatusChip(a.status)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarToday, null, tint = InkSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(a.dateLabel, style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.weight(1f))
            Text(quetzales(a.price), style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
        if (actions != null) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = actions)
        }
    }
}

@Composable
private fun Overline(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = InkSecondary, modifier = modifier)
}

@Composable
private fun EmptyState(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = InkMuted, modifier = Modifier.padding(vertical = 16.dp))
}

@Preview(showBackground = true)
@Composable
private fun HistoryPreview() = TurnosTheme { HistoryScreen(isBusiness = false, onBack = {}) }
