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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.screens.catalog.ClientBottomBar
import com.example.turnos.ui.screens.catalog.ClientTab
import com.example.turnos.ui.theme.*
import com.example.turnos.util.quetzales

@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    onReschedule: (serviceId: Long, appointmentId: Long) -> Unit,
    onGoToCatalog: () -> Unit = onBack,
    onLoggedOut: () -> Unit = {},
    viewModel: HistoryViewModel = turnosViewModel { handle ->
        HistoryViewModel(handle, appointmentRepository, authRepository, sessionRepository)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(HistoryEvent.OnMessageShown) }
    LaunchedEffect(state.isLoggedOut) { if (state.isLoggedOut) onLoggedOut() }

    HistoryScreen(state, snackbar) { event ->
        when (event) {
            is HistoryEvent.OnRescheduleClick -> onReschedule(event.appointment.serviceId, event.appointment.id)
            HistoryEvent.OnBackClick -> onBack()
            HistoryEvent.OnGoToCatalog -> onGoToCatalog()
            else -> viewModel.onEvent(event)
        }
    }
}

/**
 * Historial de citas para ambos roles:
 * - Cliente: cancelar o reprogramar.
 * - Negocio: confirmar, marcar completada o no presente.
 */
@Composable
fun HistoryScreen(
    state: HistoryState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (HistoryEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                TurnosTopBar("Historial de Citas", onBack = { onEvent(HistoryEvent.OnBackClick) })
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
                            onClick = { onEvent(HistoryEvent.OnTabSelected(tab)) },
                            selectedContentColor = TurnosOrange,
                            unselectedContentColor = InkSecondary,
                            text = { Text(label, style = MaterialTheme.typography.titleSmall) },
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!state.isBusiness) {
                ClientBottomBar(
                    selected = ClientTab.APPOINTMENTS,
                    onServices = { onEvent(HistoryEvent.OnGoToCatalog) },
                    onAppointments = {},
                    onLogout = { onEvent(HistoryEvent.OnLogoutClick) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                item { Overline(if (state.isBusiness) "Agenda del local" else "Próximas citas") }
                if (state.upcoming.isEmpty()) {
                    item { EmptyContent("No hay citas próximas.", icon = Icons.Outlined.EventBusy) }
                }
                items(state.upcoming, key = { it.id }) { a ->
                    AppointmentCard(a, showClient = state.isBusiness) {
                        if (state.isBusiness) {
                            NeutralButton("No presente", { onEvent(HistoryEvent.OnMarkNoShow(a)) }, Modifier.weight(1f))
                            if (a.status == AppointmentStatus.PENDING) {
                                SecondaryButton("Confirmar", { onEvent(HistoryEvent.OnConfirmAppointment(a)) }, Modifier.weight(1f), compact = true)
                            } else {
                                SecondaryButton("Completada", { onEvent(HistoryEvent.OnMarkCompleted(a)) }, Modifier.weight(1f), compact = true)
                            }
                        } else {
                            NeutralButton("Cancelar", { onEvent(HistoryEvent.OnCancelClick(a)) }, Modifier.weight(1f))
                            SecondaryButton("Reprogramar", { onEvent(HistoryEvent.OnRescheduleClick(a)) }, Modifier.weight(1f), compact = true)
                        }
                    }
                }
                state.past.firstOrNull()?.let { last ->
                    item { Overline("Último historial", Modifier.padding(top = 8.dp)) }
                    item(key = "last-${last.id}") { AppointmentCard(last, showClient = state.isBusiness, dimmed = true) }
                }
            } else {
                item { Overline("Citas pasadas") }
                if (state.past.isEmpty()) item { EmptyContent("Aún no hay historial.") }
                items(state.past, key = { it.id }) { AppointmentCard(it, showClient = state.isBusiness, dimmed = true) }
            }

            if (state.isBusiness) {
                item {
                    InfoBanner(
                        "El administrador puede confirmar citas y marcarlas como completadas o no presentes desde este panel.",
                        icon = Icons.Outlined.Info,
                    )
                }
            }
        }
    }

    state.appointmentToCancel?.let { a ->
        AlertDialog(
            onDismissRequest = { onEvent(HistoryEvent.OnDismissCancel) },
            title = { Text("¿Cancelar cita?") },
            text = { Text("${a.serviceName} · ${a.dateLabel}\n\nSi faltan menos de 2 horas, se registrará como cancelación tardía.") },
            confirmButton = {
                TextButton(onClick = { onEvent(HistoryEvent.OnConfirmCancel) }) { Text("Cancelar cita", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HistoryEvent.OnDismissCancel) }) { Text("Volver", color = InkSecondary) }
            },
            containerColor = SurfaceWhite,
        )
    }
}

@Composable
private fun AppointmentCard(
    a: Appointment,
    showClient: Boolean,
    dimmed: Boolean = false,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = if (dimmed) Modifier.alpha(0.75f) else Modifier) {
            Avatar(if (showClient) a.clientName else a.barberName, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.serviceName, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    if (showClient) "${a.clientName} · con ${a.barberName}" else "Con ${a.barberName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AppointmentStatusChip(a.status)
                if (a.lateCancellation) {
                    Spacer(Modifier.height(4.dp))
                    StatusChip("Tardía", WarningAmber, WarningSoft)
                }
            }
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

private val sample = HistoryState(isLoading = false, upcoming = PreviewData.upcoming, past = PreviewData.past)

@Composable
private fun Host(state: HistoryState) = TurnosTheme { HistoryScreen(state) {} }

@Preview(showBackground = true, name = "Historial cliente - próximas")
@Composable
private fun HistoryClientPreview() = Host(sample)

@Preview(showBackground = true, name = "Historial cliente - pasadas")
@Composable
private fun HistoryPastPreview() = Host(sample.copy(selectedTab = HistoryTab.PAST))

@Preview(showBackground = true, name = "Historial cliente - diálogo cancelar")
@Composable
private fun HistoryCancelDialogPreview() = Host(sample.copy(appointmentToCancel = PreviewData.upcoming.first()))

@Preview(showBackground = true, name = "Historial negocio")
@Composable
private fun HistoryBusinessPreview() = Host(sample.copy(isBusiness = true))

@Preview(showBackground = true, name = "Historial - sin citas")
@Composable
private fun HistoryEmptyPreview() = Host(HistoryState(isLoading = false))

@Preview(showBackground = true, name = "Historial - cargando")
@Composable
private fun HistoryLoadingPreview() = Host(HistoryState(isLoading = true))
