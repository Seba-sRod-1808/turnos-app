package com.example.turnos.ui.screens.business

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.turnos.data.model.BusinessProfile

enum class AdminSection(val label: String, val icon: ImageVector) {
    SERVICES("Servicios y Precios", Icons.Outlined.ContentCut),
    AVAILABILITY("Horarios y Disponibilidad", Icons.Outlined.Schedule),
    TEAM("Equipo / Barberos", Icons.Outlined.Groups),
    HISTORY("Historial de Citas", Icons.AutoMirrored.Outlined.EventNote),
    PAYMENTS("Métodos de Pago", Icons.Outlined.Payments),
    NOTIFICATIONS("Notificaciones", Icons.Outlined.Notifications),
}

data class MyBusinessState(
    val profile: BusinessProfile? = null,
    val ownerName: String = "",
    val appointmentsToday: Int = 0,
    val pending: Int = 0,
    val revenueToday: Int = 0,
    val sections: List<AdminSection> = AdminSection.entries,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val isLoggedOut: Boolean = false,
)
