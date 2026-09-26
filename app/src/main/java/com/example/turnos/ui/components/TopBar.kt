package com.example.turnos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.turnos.ui.theme.*

/**
 * Barra superior de los diseños: blanca, título en negrita, flecha de regreso
 * (o tijeras de marca en pantallas raíz) y acciones en naranja a la derecha.
 */
@Composable
fun TurnosTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionDescription: String? = null,
    onAction: () -> Unit = {},
) {
    Column(Modifier.background(SurfaceWhite).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Ink)
                }
            } else {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ContentCut, contentDescription = null, tint = TurnosOrange)
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
            if (actionIcon != null) {
                IconButton(onClick = onAction) {
                    Icon(actionIcon, contentDescription = actionDescription, tint = Ink)
                }
            }
        }
        HorizontalDivider(color = DividerGray)
    }
}

/** Scaffold estándar: top bar + fondo gris claro + barra inferior opcional. */
@Composable
fun TurnosScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionDescription: String? = null,
    onAction: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = { TurnosTopBar(title, onBack, actionIcon, actionDescription, onAction) },
        bottomBar = bottomBar,
        containerColor = ScreenBackground,
        content = content,
    )
}

/** Contenedor para un botón fijo abajo (ej. "GUARDAR CONFIGURACIÓN"). */
@Composable
fun BottomActionBar(content: @Composable () -> Unit) {
    Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) { content() }
    }
}
