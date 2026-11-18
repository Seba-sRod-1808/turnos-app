package com.example.turnos.navigation

import com.example.turnos.data.model.User
import com.example.turnos.data.model.UserRole
import kotlinx.serialization.Serializable

/**
 * Destinos serializables para la navegación con seguridad de tipos.
 *
 * Entre pantallas solo se transfieren identificadores primitivos; cada ViewModel consulta el
 * estado completo desde Room. En `BookingDestination`, `rescheduleAppointmentId = -1L` indica
 * una cita nueva; en `HistoryDestination`, `barberId = -1L` muestra la agenda general.
 */
@Serializable data object AuthGraph
@Serializable data object ClientGraph
@Serializable data object BusinessGraph

@Serializable data object LoginDestination
@Serializable data object ForgotPasswordDestination
@Serializable data class SetupAccountDestination(val userId: Long)

@Serializable data object CatalogDestination

@Serializable data class BookingDestination(
    val serviceId: Long,
    val rescheduleAppointmentId: Long = -1L,
)

@Serializable data class HistoryDestination(
    val asBusiness: Boolean,
    val barberId: Long = -1L,
)

@Serializable data object BusinessHomeDestination
@Serializable data object ClientsDestination
@Serializable data object ServicesDestination
@Serializable data object AvailabilityDestination
@Serializable data object TeamDestination
@Serializable data object PaymentsDestination
@Serializable data object NotificationsDestination

fun homeGraphFor(role: UserRole): Any = if (role == UserRole.BUSINESS) BusinessGraph else ClientGraph

fun startDestinationFor(user: User?): Any = user?.let { homeGraphFor(it.role) } ?: AuthGraph
