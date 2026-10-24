package com.example.turnos.ui.screens.availability

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.SlotState
import com.example.turnos.di.turnosViewModel
import com.example.turnos.domain.BusinessRules
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*
import com.example.turnos.util.formatDateFull
import com.example.turnos.util.formatTime

@Composable
fun AvailabilityRoute(
    onBack: () -> Unit,
    viewModel: AvailabilityViewModel = turnosViewModel {
        AvailabilityViewModel(availabilityRepository, appointmentRepository, holidayRepository)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(AvailabilityEvent.OnMessageShown) }

    AvailabilityScreen(state, snackbar) { event ->
        when (event) {
            AvailabilityEvent.OnBackClick -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun AvailabilityScreen(
    state: AvailabilityState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (AvailabilityEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Control de Disponibilidad",
        onBack = { onEvent(AvailabilityEvent.OnBackClick) },
        snackbarHostState = snackbarHostState,
        bottomBar = {
            BottomActionBar {
                LoadingPrimaryButton(
                    "GUARDAR CONFIGURACIÓN",
                    isLoading = state.isSaving,
                    enabled = state.canSave,
                    onClick = { onEvent(AvailabilityEvent.OnSaveClick) },
                )
            }
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingContent(Modifier.padding(padding))
            return@TurnosScaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                InfoBanner(
                    "Define tu horario semanal general. Los cambios se aplicarán en tiempo real a tus citas disponibles.",
                    icon = Icons.Outlined.Settings,
                )
            }
            state.validationError?.let { msg -> item { ErrorBanner(msg) } }

            item { SectionHeader("Días Laborables", modifier = Modifier.padding(top = 4.dp)) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    state.dayLabels.forEachIndexed { i, d ->
                        DayCircle(d, state.workDays.getOrElse(i) { false }) { onEvent(AvailabilityEvent.OnToggleDay(i)) }
                    }
                }
            }

            item { SectionHeader("Bloques de Horario", modifier = Modifier.padding(top = 8.dp)) }
            items(state.blocks, key = { "${it.startMinutes}-${it.endMinutes}" }) { block ->
                TurnosCard(padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = TurnosOrange, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(block.label, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onEvent(AvailabilityEvent.OnDeleteBlock(block)) }) {
                            Icon(Icons.Outlined.DeleteOutline, "Eliminar bloque", tint = InkSecondary)
                        }
                    }
                }
            }
            item {
                SecondaryButton("AGREGAR BLOQUE", icon = Icons.Filled.Add, onClick = { onEvent(AvailabilityEvent.OnAddBlockClick) }, modifier = Modifier.fillMaxWidth())
            }

            item {
                Spacer(Modifier.height(6.dp))
                TurnosCard {
                    Text("Vista Previa (Hoy)", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text("Toca un horario libre para bloquearlo manualmente.", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                    Spacer(Modifier.height(10.dp))
                    if (state.todaySlots.isEmpty()) {
                        Text("Sin bloques de horario para hoy.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.todaySlots.forEach { s -> SlotRow(s.label, s.state) { onEvent(AvailabilityEvent.OnToggleSlot(s.startMinutes)) } }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Legend(SuccessGreen, "Disponible")
                        Spacer(Modifier.width(14.dp))
                        Legend(InkMuted, "Ocupado")
                        Spacer(Modifier.width(14.dp))
                        Legend(DangerRed, "Bloqueado")
                    }
                }
            }

            item { SectionHeader("Próximos feriados (Guatemala)", "Nager.Date API", Modifier.padding(top = 8.dp)) }
            item { HolidaysCard(state.holidays) { onEvent(AvailabilityEvent.OnRetryHolidays) } }
        }
    }

    state.blockForm?.let { form -> BlockFormDialog(form, onEvent) }
}

@Composable
private fun HolidaysCard(holidays: HolidaysUi, onRetry: () -> Unit) {
    TurnosCard(padding = 14.dp) {
        when {
            holidays.isLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Consultando feriados…", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            }
            holidays.error != null -> Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CloudOff, null, tint = DangerRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(holidays.error, style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
                Spacer(Modifier.height(8.dp))
                SecondaryButton("REINTENTAR", onClick = onRetry, compact = true)
            }
            holidays.upcoming.isEmpty() -> Text("No hay feriados próximos registrados.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (holidays.fromCache) {
                    Text("Sin conexión: mostrando feriados guardados.", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                }
                holidays.upcoming.forEach { h ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Outlined.Celebration)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(h.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text(formatDateFull(h.date), style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                    }
                }
                Text(
                    "En estos días el local aparece cerrado para reservas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        }
    }
}

@Composable
private fun BlockFormDialog(form: BlockForm, onEvent: (AvailabilityEvent) -> Unit) {
    val step = BusinessRules.SLOT_STEP_MIN
    AlertDialog(
        onDismissRequest = { onEvent(AvailabilityEvent.OnDismissBlockForm) },
        containerColor = SurfaceWhite,
        title = { Text("Nuevo bloque de horario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeStepper("Desde", form.startMinutes) { onEvent(AvailabilityEvent.OnBlockFormChange(form.copy(startMinutes = it))) }
                TimeStepper("Hasta", form.endMinutes) { onEvent(AvailabilityEvent.OnBlockFormChange(form.copy(endMinutes = it))) }
                form.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = DangerRed) }
                Text("Pasos de $step minutos.", style = MaterialTheme.typography.bodySmall, color = InkMuted)
            }
        },
        confirmButton = { TextButton(onClick = { onEvent(AvailabilityEvent.OnConfirmBlock) }) { Text("Agregar", color = TurnosOrange) } },
        dismissButton = { TextButton(onClick = { onEvent(AvailabilityEvent.OnDismissBlockForm) }) { Text("Cancelar", color = InkSecondary) } },
    )
}

@Composable
private fun TimeStepper(label: String, minutes: Int, onChange: (Int) -> Unit) {
    val step = BusinessRules.SLOT_STEP_MIN
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = InkSecondary, modifier = Modifier.width(56.dp))
        IconButton(onClick = { onChange((minutes - step).coerceAtLeast(0)) }) { Icon(Icons.Filled.Remove, "Menos") }
        Text(formatTime(minutes), style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange((minutes + step).coerceAtMost(24 * 60 - step)) }) { Icon(Icons.Filled.Add, "Más") }
    }
}

@Composable
private fun DayCircle(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (active) TurnosOrange else SurfaceWhite)
            .border(BorderStroke(1.dp, if (active) TurnosOrange else DividerGray), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (active) Color.White else InkSecondary)
    }
}

private data class Look(val fg: Color, val bg: Color, val label: String, val icon: ImageVector)

@Composable
private fun SlotRow(time: String, state: SlotState, onClick: () -> Unit) {
    val look = when (state) {
        SlotState.AVAILABLE -> Look(SuccessGreen, SuccessSoft, "Disponible", Icons.Filled.Check)
        SlotState.OCCUPIED -> Look(InkSecondary, SurfaceWhite, "Ocupado", Icons.Filled.Lock)
        SlotState.BLOCKED -> Look(DangerRed, DangerSoft, "Bloqueado", Icons.Filled.Block)
    }
    val shape = MaterialTheme.shapes.small
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(look.bg)
            .border(BorderStroke(1.dp, if (state == SlotState.OCCUPIED) DividerGray else look.fg.copy(alpha = 0.5f)), shape)
            .clickable(enabled = state != SlotState.OCCUPIED, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(time, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
        Icon(look.icon, null, tint = look.fg, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(look.label, style = MaterialTheme.typography.bodySmall, color = look.fg)
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val sample = AvailabilityState(
    isLoading = false,
    workDays = listOf(true, true, true, true, true, false, false),
    blocks = PreviewData.blocks,
    todaySlots = PreviewData.slots,
    holidays = HolidaysUi(isLoading = false, upcoming = PreviewData.holidays),
    hasUnsavedChanges = true,
)

@Composable
private fun Host(state: AvailabilityState) = TurnosTheme { AvailabilityScreen(state) {} }

@Preview(showBackground = true, heightDp = 1400, name = "Disponibilidad - configurada")
@Composable
private fun AvailabilityPreview() = Host(sample)

@Preview(showBackground = true, heightDp = 1400, name = "Disponibilidad - sin días (error)")
@Composable
private fun AvailabilityNoDaysPreview() = Host(sample.copy(workDays = List(7) { false }))

@Preview(showBackground = true, heightDp = 1400, name = "Disponibilidad - feriados sin conexión")
@Composable
private fun AvailabilityHolidaysErrorPreview() = Host(
    sample.copy(holidays = HolidaysUi(isLoading = false, error = "No se pudieron consultar los feriados. Revisa tu conexión.")),
)

@Preview(showBackground = true, name = "Disponibilidad - agregar bloque")
@Composable
private fun AvailabilityBlockFormPreview() = Host(sample.copy(blockForm = BlockForm(error = "Ese bloque se traslapa con otro existente.")))
