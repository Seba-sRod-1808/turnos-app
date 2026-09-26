package com.example.turnos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TurnosColors = lightColorScheme(
    primary = TurnosOrange,
    onPrimary = SurfaceWhite,
    primaryContainer = TurnosOrangeSoft,
    onPrimaryContainer = TurnosOrangeDark,
    secondary = Ink,
    onSecondary = SurfaceWhite,
    background = ScreenBackground,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = NeutralSoft,
    onSurfaceVariant = InkSecondary,
    outline = DividerGray,
    outlineVariant = DividerGray,
    error = DangerRed,
)

val TurnosShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

/** El diseño es solo claro: la app fuerza el esquema claro. */
@Composable
fun TurnosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TurnosColors,
        typography = TurnosTypography,
        shapes = TurnosShapes,
        content = content,
    )
}
