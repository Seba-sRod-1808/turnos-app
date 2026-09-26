package com.example.turnos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.BarberStatus
import com.example.turnos.ui.theme.*

/** Tarjeta blanca con borde suave: el contenedor base de todos los diseños. */
@Composable
fun TurnosCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(SurfaceWhite)
        .border(1.dp, DividerGray, shape)
    Column(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base).padding(padding),
        content = content,
    )
}

/** Título de sección con texto secundario opcional a la derecha ("3 activos · 1 pausado"). */
@Composable
fun SectionHeader(title: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
    }
}

/** Chip de estado en mayúsculas (CONFIRMADA, PENDIENTE, ACTIVA, PAUSADO…). */
@Composable
fun StatusChip(text: String, fg: Color, bg: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text.uppercase(), color = fg, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AppointmentStatusChip(status: AppointmentStatus) {
    val (label, fg, bg) = when (status) {
        AppointmentStatus.PENDING -> Triple("Pendiente", WarningAmber, WarningSoft)
        AppointmentStatus.CONFIRMED -> Triple("Confirmada", SuccessGreen, SuccessSoft)
        AppointmentStatus.COMPLETED -> Triple("Completada", InkSecondary, NeutralSoft)
        AppointmentStatus.CANCELLED -> Triple("Cancelada", DangerRed, DangerSoft)
        AppointmentStatus.NO_SHOW -> Triple("No presente", DangerRed, DangerSoft)
    }
    StatusChip(label, fg, bg)
}

@Composable
fun BarberStatusChip(status: BarberStatus) {
    val (label, fg, bg) = when (status) {
        BarberStatus.AVAILABLE -> Triple("Disponible", SuccessGreen, SuccessSoft)
        BarberStatus.IN_SERVICE -> Triple("En servicio", TurnosOrange, TurnosOrangeSoft)
        BarberStatus.ABSENT -> Triple("Ausente", InkSecondary, NeutralSoft)
    }
    StatusChip(label, fg, bg)
}

/** Banner informativo naranja con borde (aparece arriba y abajo en varias pantallas). */
@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    fg: Color = TurnosOrange,
    bg: Color = TurnosOrangeSoft,
    border: Color = TurnosOrangeBorder,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .border(BorderStroke(1.dp, border), MaterialTheme.shapes.small)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = fg, style = MaterialTheme.typography.bodySmall)
    }
}

/** Mini tarjeta de métrica (Citas Hoy / Pendientes / Ingresos). */
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Ink) {
    TurnosCard(modifier, padding = 12.dp) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
    }
}

/**
 * Avatar circular con iniciales. Sustituye las fotos de los diseños mientras
 * no haya carga de imágenes (Coil) conectada al backend.
 */
@Composable
fun Avatar(name: String, size: Dp = 44.dp) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF5B4636), Color(0xFF2B211A)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp)
    }
}

/** Placeholder de foto del local / servicio con tonos cálidos tipo barbería. */
@Composable
fun PhotoPlaceholder(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                Brush.linearGradient(listOf(Color(0xFF6E5442), Color(0xFF3A2C22), Color(0xFF1E1712)))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(48.dp))
    }
}

@Composable
fun RatingStars(rating: Int, max: Int = 5, size: Dp = 14.dp) {
    Row {
        repeat(max) { i ->
            Icon(
                if (i < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Color(0xFFF5B301),
                modifier = Modifier.size(size),
            )
        }
    }
}

/** Switch con los colores del diseño. */
@Composable
fun TurnosSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = TurnosOrange,
            checkedBorderColor = TurnosOrange,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = DividerGray,
            uncheckedBorderColor = DividerGray,
        ),
    )
}

/** Ícono cuadrado con fondo naranja suave, usado en listas de ajustes. */
@Composable
fun IconBadge(icon: ImageVector, fg: Color = TurnosOrange, bg: Color = TurnosOrangeSoft) {
    Box(
        Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(bg),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp)) }
}

/** Fila navegable de un menú de administración (Servicios, Horarios, …). */
@Composable
fun NavRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    TurnosCard(onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TurnosOrange, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = InkMuted)
        }
    }
}

/** Formato de moneda en quetzales: Q1,200 */
fun quetzales(amount: Int): String = "Q" + String.format(java.util.Locale.US, "%,d", amount)
