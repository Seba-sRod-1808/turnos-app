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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.BusinessProfile
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/** Secciones del panel de administración. */
enum class AdminSection(val label: String, val icon: ImageVector) {
    SERVICES("Servicios y Precios", Icons.Outlined.ContentCut),
    AVAILABILITY("Horarios y Disponibilidad", Icons.Outlined.Schedule),
    TEAM("Equipo / Barberos", Icons.Outlined.Groups),
    HISTORY("Historial de Citas", Icons.AutoMirrored.Outlined.EventNote),
    PAYMENTS("Métodos de Pago", Icons.Outlined.Payments),
    NOTIFICATIONS("Notificaciones", Icons.Outlined.Notifications),
}

data class MyBusinessUiState(
    val business: BusinessProfile? = null,
    val sections: List<AdminSection> = AdminSection.entries,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/** Panel principal del administrador del negocio. */
@Composable
fun MyBusinessScreen(
    state: MyBusinessUiState,
    onSectionClick: (AdminSection) -> Unit,
    onEditProfile: () -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
) {
    TurnosScaffold(
        title = "Mi Local",
        actionIcon = Icons.AutoMirrored.Filled.Logout,
        actionDescription = "Cerrar sesión",
        onAction = onLogout,
    ) { padding ->
        val b = state.business
        when {
            state.isLoading -> LoadingContent(Modifier.padding(padding))
            state.errorMessage != null || b == null ->
                ErrorContent(state.errorMessage ?: "No se encontró el negocio.", onRetry, Modifier.padding(padding))
            else -> Column(
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
                    SecondaryButton("Editar Perfil", onClick = onEditProfile, modifier = Modifier.fillMaxWidth(), compact = true)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Citas Hoy", b.appointmentsToday.toString(), Modifier.weight(1f), valueColor = TurnosOrange)
                    StatCard("Pendientes", b.pending.toString(), Modifier.weight(1f))
                    StatCard("Ingresos", quetzales(b.revenueToday), Modifier.weight(1f), valueColor = SuccessGreen)
                }

                SectionHeader("Administración")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.sections.forEach { section ->
                        NavRow(section.icon, section.label) { onSectionClick(section) }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Composable
private fun MyBusinessPreviewHost(state: MyBusinessUiState) = TurnosTheme {
    MyBusinessScreen(state, {}, {}, {}, {})
}

@Preview(showBackground = true, heightDp = 900, name = "Mi Local - contenido")
@Composable
private fun MyBusinessPreview() = MyBusinessPreviewHost(MyBusinessUiState(business = FakeDataSource.business))

@Preview(showBackground = true, name = "Mi Local - cargando")
@Composable
private fun MyBusinessLoadingPreview() = MyBusinessPreviewHost(MyBusinessUiState(isLoading = true))

@Preview(showBackground = true, name = "Mi Local - error")
@Composable
private fun MyBusinessErrorPreview() = MyBusinessPreviewHost(
    MyBusinessUiState(errorMessage = "No se pudo cargar tu negocio. Revisa tu conexión."),
)
