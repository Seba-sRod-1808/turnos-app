package com.example.turnos.navigation

import com.example.turnos.data.model.User
import com.example.turnos.data.model.UserRole
import kotlinx.serialization.Serializable

/*
 * Navegación type-safe: cada destino es un objeto/clase @Serializable.
 * Entre pantallas solo viajan IDs; cada ViewModel carga el resto desde Room.
 * En la reserva, rescheduleAppointmentId = -1 significa reserva nueva.
 * En el historial, barberId = -1 muestra todas las citas; otro valor, la agenda de ese barbero.
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

/** Con sesión guardada se abre directamente el inicio de su rol. */
fun startDestinationFor(user: User?): Any = user?.let { homeGraphFor(it.role) } ?: AuthGraph
