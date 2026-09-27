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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.data.model.TimeSlot
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

data class AvailabilityUiState(
    val dayLabels: List<String> = emptyList(),
    val activeDays: List<Boolean> = emptyList(),
    val blocks: List<TimeBlock> = emptyList(),
    val todaySlots: List<TimeSlot> = emptyList(),
    val isSaving: Boolean = false,
) {
    val validationError: String?
        get() = when {
            activeDays.none { it } -> "Selecciona al menos un día laborable."
            blocks.isEmpty() -> "Agrega al menos un bloque de horario."
            else -> null
        }
}

@Composable
fun AvailabilityScreen(
    state: AvailabilityUiState,
    snackbarHostState: SnackbarHostState? = null,
    onBack: () -> Unit,
    onToggleDay: (Int) -> Unit,
    onDeleteBlock: (TimeBlock) -> Unit,
    onAddBlock: () -> Unit,
    onToggleSlot: (Int) -> Unit,
    onSave: () -> Unit,
) {
    TurnosScaffold(
        title = "Control de Disponibilidad",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            BottomActionBar {
                if (state.isSaving) {
                    Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(28.dp))
                    }
                } else {
                    PrimaryButton("GUARDAR CONFIGURACIÓN", onClick = onSave, enabled = state.validationError == null)
                }
            }
        },
    ) { padding ->
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
            state.validationError?.let { msg ->
                item {
                    InfoBanner(msg, icon = Icons.Outlined.ErrorOutline, fg = DangerRed, bg = DangerSoft, border = DangerRed.copy(alpha = 0.4f))
                }
            }

            item { SectionHeader("Días Laborables", modifier = Modifier.padding(top = 4.dp)) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    state.dayLabels.forEachIndexed { i, d ->
                        DayCircle(d, state.activeDays.getOrElse(i) { false }) { onToggleDay(i) }
                    }
                }
            }

            item { SectionHeader("Bloques de Horario", modifier = Modifier.padding(top = 8.dp)) }
            items(state.blocks, key = { it.id }) { block ->
                TurnosCard(padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = TurnosOrange, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("${block.start} - ${block.end}", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDeleteBlock(block) }) {
                            Icon(Icons.Outlined.DeleteOutline, "Eliminar bloque", tint = InkSecondary)
                        }
                    }
                }
            }
            item {
                SecondaryButton("AGREGAR BLOQUE", icon = Icons.Filled.Add, onClick = onAddBlock, modifier = Modifier.fillMaxWidth())
            }

            item {
                Spacer(Modifier.height(6.dp))
                TurnosCard {
                    Text("Vista Previa (Hoy)", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text("Toca un horario libre para bloquearlo manualmente.", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.todaySlots.forEachIndexed { i, s -> SlotRow(s.time, s.state) { onToggleSlot(i) } }
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
        }
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

private val previewState = AvailabilityUiState(
    dayLabels = FakeDataSource.workDays,
    activeDays = listOf(true, true, true, true, true, false, false),
    blocks = FakeDataSource.timeBlocks,
    todaySlots = FakeDataSource.previewSlots,
)

@Composable
private fun AvailabilityPreviewHost(state: AvailabilityUiState) = TurnosTheme {
    AvailabilityScreen(state = state, onBack = {}, onToggleDay = {}, onDeleteBlock = {}, onAddBlock = {}, onToggleSlot = {}, onSave = {})
}

@Preview(showBackground = true, heightDp = 1000, name = "Disponibilidad - configurada")
@Composable
private fun AvailabilityPreview() = AvailabilityPreviewHost(previewState)

@Preview(showBackground = true, heightDp = 1000, name = "Disponibilidad - guardando")
@Composable
private fun AvailabilitySavingPreview() = AvailabilityPreviewHost(previewState.copy(isSaving = true))

@Preview(showBackground = true, heightDp = 1000, name = "Disponibilidad - sin días (error)")
@Composable
private fun AvailabilityNoDaysPreview() = AvailabilityPreviewHost(previewState.copy(activeDays = List(7) { false }))

@Preview(showBackground = true, heightDp = 1000, name = "Disponibilidad - sin bloques (error)")
@Composable
private fun AvailabilityNoBlocksPreview() = AvailabilityPreviewHost(previewState.copy(blocks = emptyList()))
