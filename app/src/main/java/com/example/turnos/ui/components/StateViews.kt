package com.example.turnos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.ui.theme.*

/** Estados genéricos de una pantalla que carga datos. */
@Composable
fun LoadingContent(modifier: Modifier = Modifier, message: String = "Cargando…") {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = TurnosOrange)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
    }
}

@Composable
fun EmptyContent(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = InkMuted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.CloudOff, null, tint = DangerRed, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        SecondaryButton("REINTENTAR", onClick = onRetry, compact = true)
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun LoadingPreview() = TurnosTheme { LoadingContent() }

@Preview(showBackground = true, name = "Vacío")
@Composable
private fun EmptyPreview() = TurnosTheme { EmptyContent("No hay elementos todavía.") }

@Preview(showBackground = true, name = "Error")
@Composable
private fun ErrorPreview() = TurnosTheme { ErrorContent("No se pudo cargar la información.", onRetry = {}) }
