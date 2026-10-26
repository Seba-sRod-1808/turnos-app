package com.example.turnos.ui.screens.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeSlot
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*
import com.example.turnos.util.formatDayChip
import com.example.turnos.util.quetzales
import java.time.LocalDate

@Composable
fun BookingRoute(
    onBack: () -> Unit,
    onViewAgenda: () -> Unit,
    viewModel: BookingViewModel = turnosViewModel { handle ->
        BookingViewModel(
            handle, serviceRepository, appointmentRepository, availabilityRepository,
            barberRepository, holidayRepository, sessionRepository, settingsRepository,
        )
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BookingScreen(state) { event ->
        when (event) {
            BookingEvent.OnBackClick -> onBack()
            BookingEvent.OnViewAgendaClick -> onViewAgenda()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun BookingScreen(state: BookingState, onEvent: (BookingEvent) -> Unit) {
    TurnosScaffold(
        title = if (state.isReschedule) "Reprogramar Cita" else "Confirmación de Cita",
        onBack = { onEvent(BookingEvent.OnBackClick) },
        bottomBar = {
            if (state.service != null) {
                BottomActionBar {
                    if (state.isConfirmed) {
                        PrimaryButton("VER CITA EN AGENDA", onClick = { onEvent(BookingEvent.OnViewAgendaClick) })
                    } else {
                        LoadingPrimaryButton(
                            if (state.isReschedule) "CONFIRMAR NUEVO HORARIO" else "CONFIRMAR RESERVA",
                            isLoading = state.isConfirming,
                            enabled = state.canConfirm,
                            onClick = { onEvent(BookingEvent.OnConfirmClick) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        val service = state.service
        when {
            state.isLoading -> LoadingContent(Modifier.padding(padding), "Preparando calendario…")
            service == null -> ErrorContent(
                state.errorMessage ?: "No se encontró el servicio.",
                onRetry = { onEvent(BookingEvent.OnBackClick) },
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                state.errorMessage?.let { ErrorBanner(it) }

                // ---------- Sumario ----------
                TurnosCard {
                    Text("Sumario", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(state.barberName, 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Barbero:", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            Text(state.barberName, style = MaterialTheme.typography.titleSmall, color = Ink)
                            RatingStars(5)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = DividerGray)
                    Spacer(Modifier.height(12.dp))
                    LabelValue("Servicio", service.name)
                    LabelValue("Duración", "${service.durationMin} min")
                    LabelValue("Fecha", state.dateLabel)
                }

                // ---------- Fecha y hora ----------
                TurnosCard {
                    Text("Fecha y hora", style = MaterialTheme.typography.titleSmall, color = Ink)
                    state.holidaysNotice?.let {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CloudOff, null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.days, key = { it.date.toString() }) { day ->
                            DayChip(day, selected = day.date == state.selectedDate, enabled = state.isEditable) {
                                onEvent(BookingEvent.OnSelectDay(day.date))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val selectedDay = state.selectedDay
                    when {
                        selectedDay == null -> Text("Elige un día.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                        selectedDay.holidayName != null ->
                            ClosedDayMessage("Feriado: ${selectedDay.holidayName}. El local está cerrado.")
                        !selectedDay.isWorkDay -> ClosedDayMessage("El local no atiende este día.")
                        state.isLoadingSlots -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(24.dp))
                        }
                        state.slots.none { it.state == SlotState.AVAILABLE } -> ClosedDayMessage("No quedan horarios disponibles este día.")
                        else -> SlotGrid(state.slots, state.selectedSlot, state.isEditable) { onEvent(BookingEvent.OnSelectSlot(it)) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Solo se muestran como disponibles los horarios libres para ${service.durationMin} min.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }

                // ---------- Mapa ----------
                TurnosCard {
                    Text("Mapa", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Spacer(Modifier.height(12.dp))
                    MapPlaceholder(Modifier.fillMaxWidth().height(140.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("${state.businessName} · ${state.businessAddress}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }

                // ---------- Estado ----------
                TurnosCard {
                    Text("Estado", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isConfirmed) {
                            StatusChip("Confirmada", SuccessGreen, SuccessSoft, icon = Icons.Filled.CheckCircle)
                        } else {
                            AppointmentStatusChip(AppointmentStatus.PENDING)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(quetzales(service.price), style = MaterialTheme.typography.titleMedium, color = Ink)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.Schedule, null, tint = InkMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("${service.durationMin} min", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }

                InfoBanner("Puedes cancelar o reprogramar sin penalización hasta 2 horas antes de tu cita.")
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = Ink)
    }
}

@Composable
private fun ClosedDayMessage(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(Icons.Outlined.EventBusy, null, tint = InkMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
    }
}

@Composable
private fun DayChip(day: DayOption, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.small
    val background = when {
        selected -> TurnosOrange
        !day.isOpen -> NeutralSoft
        else -> SurfaceWhite
    }
    Column(
        Modifier
            .clip(shape)
            .background(background)
            .border(BorderStroke(1.dp, if (selected) TurnosOrange else DividerGray), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            day.label,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                selected -> Color.White
                !day.isOpen -> InkMuted
                else -> Ink
            },
        )
        day.closedReason?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else InkMuted)
        }
    }
}

/** Rejilla de horarios: ocupados y bloqueados se muestran deshabilitados. */
@Composable
private fun SlotGrid(slots: List<TimeSlot>, selected: Int?, enabled: Boolean, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    val available = s.state == SlotState.AVAILABLE
                    val isSel = s.startMinutes == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                when {
                                    isSel -> TurnosOrange
                                    available -> SuccessSoft
                                    else -> NeutralSoft
                                }
                            )
                            .clickable(enabled = enabled && available) { onSelect(s.startMinutes) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            s.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                isSel -> Color.White
                                available -> SuccessGreen
                                else -> InkMuted
                            },
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Espacio reservado para el mapa (solo una "sombra").
 * TODO: reemplazar por Google Maps (maps-compose) cuando se integre la API.
 */
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.small).background(NeutralSoft))
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewDays = (0 until 7).map { i ->
    val d = LocalDate.of(2026, 9, 14).plusDays(i.toLong())
    DayOption(d, formatDayChip(d), isWorkDay = d.dayOfWeek.value <= 5, holidayName = if (d.dayOfMonth == 15) "Día de la Independencia" else null)
}

private val sample = BookingState(
    isLoading = false,
    service = PreviewData.services.first(),
    barberName = "Alex Rivera",
    businessName = PreviewData.business.name,
    businessAddress = PreviewData.business.address,
    days = previewDays,
    selectedDate = previewDays.first().date,
    slots = PreviewData.slots,
)

@Composable
private fun Host(state: BookingState) = TurnosTheme { BookingScreen(state) {} }

@Preview(showBackground = true, heightDp = 1150, name = "Reserva - eligiendo horario")
@Composable
private fun BookingInitialPreview() = Host(sample)

@Preview(showBackground = true, heightDp = 1150, name = "Reserva - horario elegido")
@Composable
private fun BookingSelectedPreview() = Host(sample.copy(selectedSlot = 11 * 60))

@Preview(showBackground = true, heightDp = 1150, name = "Reserva - confirmada")
@Composable
private fun BookingConfirmedPreview() = Host(sample.copy(selectedSlot = 11 * 60, isConfirmed = true))

@Preview(showBackground = true, heightDp = 1150, name = "Reserva - día feriado (API)")
@Composable
private fun BookingHolidayPreview() = Host(sample.copy(selectedDate = previewDays[1].date, slots = emptyList()))

@Preview(showBackground = true, heightDp = 1150, name = "Reserva - horario ya tomado / sin conexión")
@Composable
private fun BookingErrorPreview() = Host(
    sample.copy(
        errorMessage = "Ese horario acaba de ser reservado. Elige otro.",
        holidaysNotice = "Sin conexión: se usan los feriados guardados.",
    ),
)

@Preview(showBackground = true, name = "Reserva - cargando")
@Composable
private fun BookingLoadingPreview() = Host(BookingState(isLoading = true))
