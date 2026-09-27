package com.example.turnos.ui.screens.availability

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AvailabilityScreen(onBack: () -> Unit) {
    val activeDays = remember { mutableStateListOf(true, true, true, true, true, false, false) }
    val blocks = remember { mutableStateListOf<TimeBlock>().apply { addAll(MockData.timeBlocks) } }
    val slots = remember { mutableStateListOf(*MockData.previewSlots.toTypedArray()) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TurnosTopBar("Control de Disponibilidad", onBack = onBack) },
        bottomBar = {
            BottomActionBar {
                PrimaryButton("GUARDAR CONFIGURACIÓN", onClick = {
                    scope.launch { snackbar.showSnackbar("Configuración guardada") }
                })
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = ScreenBackground,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            InfoBanner(
                "Define tu horario semanal general. Los cambios se aplicarán en tiempo real a tus citas disponibles.",
                icon = Icons.Outlined.Settings,
            )

            SectionHeader("Días Laborables", modifier = Modifier.padding(top = 12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MockData.workDays.forEachIndexed { i, d ->
                    DayCircle(d, activeDays[i]) { activeDays[i] = !activeDays[i] }
                }
            }

            SectionHeader("Bloques de Horario", modifier = Modifier.padding(top = 16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                blocks.forEach { block ->
                    TurnosCard(padding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, null, tint = TurnosOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("${block.start} - ${block.end}", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                            IconButton(onClick = { blocks.remove(block) }) {
                                Icon(Icons.Outlined.DeleteOutline, "Eliminar bloque", tint = InkSecondary)
                            }
                        }
                    }
                }
                SecondaryButton(
                    "AGREGAR BLOQUE",
                    icon = Icons.Filled.Add,
                    onClick = { blocks.add(TimeBlock("t${blocks.size + 10}", "07:00 PM", "08:00 PM")) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(16.dp))
            TurnosCard {
                Text("Vista Previa (Hoy)", style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    "Toca un horario libre para bloquearlo manualmente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slots.forEachIndexed { i, s ->
                        SlotRow(s.time, s.state) {
                            slots[i] = when (s.state) {
                                SlotState.AVAILABLE -> s.copy(state = SlotState.BLOCKED)
                                SlotState.BLOCKED -> s.copy(state = SlotState.AVAILABLE)
                                SlotState.OCCUPIED -> s // ocupado por una cita: no editable
                            }
                        }
                    }
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
            Spacer(Modifier.height(8.dp))
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

@Preview(showBackground = true)
@Composable
private fun AvailabilityPreview() = TurnosTheme { AvailabilityScreen {} }
