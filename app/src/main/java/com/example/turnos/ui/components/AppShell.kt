package com.example.turnos.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.turnos.ui.theme.*

/**
 * Piezas de la app que no pertenecen a una pantalla: splash de arranque, aviso sin conexión
 * y la solicitud del permiso de notificaciones (Android 13+).
 */

@Composable
fun SplashContent() {
    Column(
        Modifier.fillMaxSize().background(SurfaceWhite),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandMark()
        Spacer(Modifier.height(16.dp))
        Text("Turnos", style = MaterialTheme.typography.headlineMedium, color = Ink)
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(WarningSoft)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CloudOff, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Sin conexión: estás viendo tu agenda guardada en el teléfono.",
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Preview(showBackground = true, name = "Splash")
@Composable
private fun SplashPreview() = TurnosTheme { SplashContent() }

@Preview(showBackground = true, name = "Sin conexión")
@Composable
private fun OfflinePreview() = TurnosTheme { OfflineBanner() }
