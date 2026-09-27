package com.example.turnos.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.screens.catalog.ClientBottomBar
import com.example.turnos.ui.theme.*

enum class HistoryTab { UPCOMING, PAST }

data class HistoryUiState(
    val isBusiness: Boolean = false,
    val selectedTab: HistoryTab = HistoryTab.UPCOMING,
    val upcoming: List<Appointment> = emptyList(),
    val past: List<Appointment> = emptyList(),
    /** Cita pendiente de confirmar su cancelación (muestra el diálogo). */
    val appointmentToCancel: Appointment? = null,
    val isLoading: Boolean = false,
)

/**
 * Historial de citas. La misma pantalla sirve a ambos roles:
 * - Cliente: puede cancelar o reprogramar.
 * - Negocio: puede marcar como completada o no presente.
 */
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    snackbarHostState: SnackbarHostState? = null,
    onBack: () -> Unit,
    onTabSelected: (HistoryTab) -> Unit,
    onCancelClick: (Appointment) -> Unit,
    onConfirmCancel: () -> Unit,
    onDismissCancel: () -> Unit,
    onRescheduleClick: (Appointment) -> Unit,
    onMarkCompleted: (Appointment) -> Unit,
    onMarkNoShow: (Appointment) -> Unit,
    onGoToCatalog: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            Column {
                TurnosTopBar("Historial de Citas", onBack = onBack)
                val tabIndex = state.selectedTab.ordinal
                TabRow(
                    selectedTabIndex = tabIndex,
                    containerColor = SurfaceWhite,
                    contentColor = TurnosOrange,
                    indicator = { positions ->
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[tabIndex]), color = TurnosOrange)
                    },
                ) {
                    listOf(HistoryTab.UPCOMING to "Próximas", HistoryTab.PAST to "Pasadas").forEach { (tab, label) ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            selectedContentColor = TurnosOrange,
                            unselectedContentColor = InkSecondary,
                            text = { Text(label, style = MaterialTheme.typography.titleSmall) },
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!state.isBusiness) ClientBottomBar(selected = 1, onServices = onGoToCatalog, onAppointments = {}, onLogout = onLogout)
        },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        containerColor = ScreenBackground,
    ) { padding ->
        if (state.isLoading) {
            LoadingContent(Modifier.padding(padding), "Cargando citas…")
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.selectedTab == HistoryTab.UPCOMING) {
                item { Overline("Próximas citas") }
                if (state.upcoming.isEmpty()) {
                    item { EmptyContent("No tienes citas próximas.", icon = Icons.Outlined.EventBusy) }
                }
                items(state.upcoming, key = { it.id }) { a ->
                    AppointmentCard(a) {
                        if (state.isBusiness) {
                            NeutralButton("No presente", { onMarkNoShow(a) }, Modifier.weight(1f))
                            SecondaryButton("Completada", { onMarkCompleted(a) }, Modifier.weight(1f), compact = true)
                        } else {
                            NeutralButton("Cancelar", { onCancelClick(a) }, Modifier.weight(1f))
                            SecondaryButton("Reprogramar", { onRescheduleClick(a) }, Modifier.weight(1f), compact = true)
                        }
                    }
                }
                state.past.firstOrNull()?.let { last ->
                    item { Overline("Último historial", Modifier.padding(top = 8.dp)) }
                    item(key = "last-${last.id}") { AppointmentCard(last, dimmed = true) }
                }
            } else {
                item { Overline("Citas pasadas") }
                if (state.past.isEmpty()) item { EmptyContent("Aún no hay historial.") }
                items(state.past, key = { it.id }) { AppointmentCard(it, dimmed = true) }
            }

            if (state.isBusiness) {
                item {
                    InfoBanner(
                        "El administrador puede marcar citas como completadas o no presentes desde este panel.",
                        icon = Icons.Outlined.Info,
                    )
                }
            }
        }
    }

    state.appointmentToCancel?.let { a ->
        AlertDialog(
            onDismissRequest = onDismissCancel,
            title = { Text("¿Cancelar cita?") },
            text = {
                Text("${a.service} · ${a.dateLabel}\n\nSi faltan menos de 2 horas, se registrará como cancelación tardía.")
            },
            confirmButton = { TextButton(onClick = onConfirmCancel) { Text("Cancelar cita", color = DangerRed) } },
            dismissButton = { TextButton(onClick = onDismissCancel) { Text("Volver", color = InkSecondary) } },
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

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewState = HistoryUiState(upcoming = FakeDataSource.upcoming, past = FakeDataSource.past)

@Composable
private fun HistoryPreviewHost(state: HistoryUiState) = TurnosTheme {
    HistoryScreen(
        state = state, onBack = {}, onTabSelected = {}, onCancelClick = {}, onConfirmCancel = {},
        onDismissCancel = {}, onRescheduleClick = {}, onMarkCompleted = {}, onMarkNoShow = {},
    )
}

@Preview(showBackground = true, name = "Historial cliente - próximas")
@Composable
private fun HistoryClientPreview() = HistoryPreviewHost(previewState)

@Preview(showBackground = true, name = "Historial cliente - pasadas")
@Composable
private fun HistoryPastPreview() = HistoryPreviewHost(previewState.copy(selectedTab = HistoryTab.PAST))

@Preview(showBackground = true, name = "Historial cliente - diálogo cancelar")
@Composable
private fun HistoryCancelDialogPreview() = HistoryPreviewHost(
    previewState.copy(appointmentToCancel = FakeDataSource.upcoming.first()),
)

@Preview(showBackground = true, name = "Historial negocio")
@Composable
private fun HistoryBusinessPreview() = HistoryPreviewHost(previewState.copy(isBusiness = true))

@Preview(showBackground = true, name = "Historial - sin citas")
@Composable
private fun HistoryEmptyPreview() = HistoryPreviewHost(HistoryUiState())

@Preview(showBackground = true, name = "Historial - cargando")
@Composable
private fun HistoryLoadingPreview() = HistoryPreviewHost(HistoryUiState(isLoading = true))
