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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.SlotState
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

private val days = listOf("Lun 15", "Mar 16", "Mié 17", "Jue 18", "Vie 19", "Sáb 20")

@Composable
fun BookingConfirmationScreen(
    serviceId: String,
    onBack: () -> Unit,
    onViewAgenda: () -> Unit,
) {
    val service = remember(serviceId) { MockData.services.firstOrNull { it.id == serviceId } ?: MockData.services.first() }
    val barber = MockData.barbers.first()

    var dayIndex by rememberSaveable { mutableIntStateOf(0) }
    var slot by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmed by rememberSaveable { mutableStateOf(false) }

    TurnosScaffold(
        title = "Confirmación de Cita",
        onBack = onBack,
        bottomBar = {
            BottomActionBar {
                if (confirmed) {
                    PrimaryButton("VER CITA EN AGENDA", onClick = onViewAgenda)
                } else {
                    PrimaryButton("CONFIRMAR RESERVA", onClick = { confirmed = true }, enabled = slot != null)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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
                LabelValue("Fecha", if (slot != null) "${days[dayIndex]} de Junio, $slot" else "Selecciona un horario")
            }

            // ---------- Fecha y hora (reserva autónoma) ----------
            TurnosCard {
                Text("Fecha y hora", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(days) { i, d ->
                        SelectChip(d, selected = i == dayIndex, enabled = !confirmed) { dayIndex = i; slot = null }
                    }
                }
                Spacer(Modifier.height(12.dp))
                FlowRowSlots(
                    selected = slot,
                    enabled = !confirmed,
                    onSelect = { slot = it },
                )
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
                    "${MockData.business.name} · ${MockData.business.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                )
            }

            // ---------- Estado ----------
            TurnosCard {
                Text("Estado", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (confirmed) {
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
private fun FlowRowSlots(selected: String?, enabled: Boolean, onSelect: (String) -> Unit) {
    val rows = MockData.previewSlots.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    val available = s.state == SlotState.AVAILABLE
                    val isSel = s.time == selected
                    val shape = MaterialTheme.shapes.small
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(shape)
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

@Preview(showBackground = true)
@Composable
private fun BookingPreview() = TurnosTheme { BookingConfirmationScreen("s1", {}, {}) }
