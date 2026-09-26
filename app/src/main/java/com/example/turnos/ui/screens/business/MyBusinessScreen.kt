package com.example.turnos.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.navigation.Routes
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/** Panel principal del administrador del negocio. */
@Composable
fun MyBusinessScreen(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val b = MockData.business

    TurnosScaffold(
        title = "Mi Local",
        actionIcon = Icons.AutoMirrored.Filled.Logout,
        actionDescription = "Cerrar sesión",
        onAction = onLogout,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TurnosCard(padding = 12.dp) {
                PhotoPlaceholder(Icons.Outlined.Storefront, Modifier.height(140.dp))
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.name, style = MaterialTheme.typography.titleLarge, color = Ink, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFF5B301), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(b.rating.toString(), style = MaterialTheme.typography.titleSmall, color = Ink)
                }
                Text(b.address, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Spacer(Modifier.height(12.dp))
                SecondaryButton("Editar Perfil", onClick = { }, modifier = Modifier.fillMaxWidth(), compact = true)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Citas Hoy", b.appointmentsToday.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                StatCard("Pendientes", b.pending.toString(), Modifier.weight(1f))
                StatCard("Ingresos", quetzales(b.revenueToday), Modifier.weight(1f), valueColor = SuccessGreen)
            }

            SectionHeader("Administración")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NavRow(Icons.Outlined.ContentCut, "Servicios y Precios") { onNavigate(Routes.SERVICES) }
                NavRow(Icons.Outlined.Schedule, "Horarios y Disponibilidad") { onNavigate(Routes.AVAILABILITY) }
                NavRow(Icons.Outlined.Groups, "Equipo / Barberos") { onNavigate(Routes.TEAM) }
                NavRow(Icons.AutoMirrored.Outlined.EventNote, "Historial de Citas") { onNavigate(Routes.BUSINESS_HISTORY) }
                NavRow(Icons.Outlined.Payments, "Métodos de Pago") { onNavigate(Routes.PAYMENTS) }
                NavRow(Icons.Outlined.Notifications, "Notificaciones") { onNavigate(Routes.NOTIFICATIONS) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyBusinessPreview() = TurnosTheme { MyBusinessScreen({}, {}) }
