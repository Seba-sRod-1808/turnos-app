package com.example.turnos.ui.screens.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.Barber
import com.example.turnos.data.model.Service
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeSlot
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

data class BookingUiState(
    val service: Service? = null,
    val barber: Barber? = null,
    val businessName: String = "",
    val businessAddress: String = "",
    val days: List<String> = emptyList(),
    val selectedDayIndex: Int = 0,
    val slots: List<TimeSlot> = emptyList(),
    val selectedSlot: String? = null,
    val isConfirming: Boolean = false,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null,
) {
    val canConfirm: Boolean get() = selectedSlot != null && !isConfirming && !isConfirmed
    val dateLabel: String
        get() = if (selectedSlot != null && days.isNotEmpty()) "${days[selectedDayIndex]} de Junio, $selectedSlot"
        else "Selecciona un horario"
}

@Composable
fun BookingConfirmationScreen(
    state: BookingUiState,
    onBack: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onSelectSlot: (String) -> Unit,
    onConfirm: () -> Unit,
    onViewAgenda: () -> Unit,
) {
    TurnosScaffold(
        title = "Confirmación de Cita",
        onBack = onBack,
        bottomBar = {
            BottomActionBar {
                when {
                    state.isConfirmed -> PrimaryButton("VER CITA EN AGENDA", onClick = onViewAgenda)
                    state.isConfirming -> Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(28.dp))
                    }
                    else -> PrimaryButton("CONFIRMAR RESERVA", onClick = onConfirm, enabled = state.canConfirm)
                }
            }
        },
    ) { padding ->
        val service = state.service
        val barber = state.barber
        if (service == null || barber == null) {
            LoadingContent(Modifier.padding(padding))
            return@TurnosScaffold
        }
        val editable = !state.isConfirmed && !state.isConfirming

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.errorMessage != null) {
                InfoBanner(
                    state.errorMessage,
                    icon = Icons.Outlined.ErrorOutline,
                    fg = DangerRed, bg = DangerSoft, border = DangerRed.copy(alpha = 0.4f),
                )
            }

            // ---------- Sumario ----------
            TurnosCard {
                Text("Sumario", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(barber.name, 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Barbero:", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        Text(barber.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                        RatingStars(barber.rating)
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
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(state.days) { i, d ->
                        SelectChip(d, selected = i == state.selectedDayIndex, enabled = editable) { onSelectDay(i) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (state.slots.none { it.state == SlotState.AVAILABLE }) {
                    Text("No hay horarios disponibles este día.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                } else {
                    SlotGrid(state.slots, state.selectedSlot, editable, onSelectSlot)
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
                Text(
                    "${state.businessName} · ${state.businessAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                )
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

@Composable
private fun LabelValue(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = Ink)
    }
}

@Composable
private fun SelectChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.small
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) TurnosOrange else SurfaceWhite)
            .border(BorderStroke(1.dp, if (selected) TurnosOrange else DividerGray), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else Ink)
    }
}

/** Rejilla de horarios: ocupados y bloqueados se muestran deshabilitados. */
@Composable
private fun SlotGrid(slots: List<TimeSlot>, selected: String?, enabled: Boolean, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    val available = s.state == SlotState.AVAILABLE
                    val isSel = s.time == selected
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
                            .clickable(enabled = enabled && available) { onSelect(s.time) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            s.time,
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

private val previewState = BookingUiState(
    service = FakeDataSource.services.first(),
    barber = FakeDataSource.barbers.first(),
    businessName = FakeDataSource.business.name,
    businessAddress = FakeDataSource.business.address,
    days = FakeDataSource.bookingDays,
    slots = FakeDataSource.previewSlots,
)

@Composable
private fun BookingPreviewHost(state: BookingUiState) = TurnosTheme {
    BookingConfirmationScreen(state, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, heightDp = 1100, name = "Reserva - sin horario")
@Composable
private fun BookingInitialPreview() = BookingPreviewHost(previewState)

@Preview(showBackground = true, heightDp = 1100, name = "Reserva - horario elegido")
@Composable
private fun BookingSelectedPreview() = BookingPreviewHost(previewState.copy(selectedSlot = "11:00 AM"))

@Preview(showBackground = true, heightDp = 1100, name = "Reserva - confirmando")
@Composable
private fun BookingConfirmingPreview() = BookingPreviewHost(previewState.copy(selectedSlot = "11:00 AM", isConfirming = true))

@Preview(showBackground = true, heightDp = 1100, name = "Reserva - confirmada")
@Composable
private fun BookingConfirmedPreview() = BookingPreviewHost(previewState.copy(selectedSlot = "11:00 AM", isConfirmed = true))

@Preview(showBackground = true, heightDp = 1100, name = "Reserva - horario ya tomado")
@Composable
private fun BookingErrorPreview() = BookingPreviewHost(
    previewState.copy(errorMessage = "Ese horario acaba de ser reservado por alguien más. Elige otro."),
)

@Preview(showBackground = true, heightDp = 1100, name = "Reserva - día sin horarios")
@Composable
private fun BookingNoSlotsPreview() = BookingPreviewHost(
    previewState.copy(selectedDayIndex = 5, slots = previewState.slots.map { it.copy(state = SlotState.OCCUPIED) }),
)
