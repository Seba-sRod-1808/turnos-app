package com.example.turnos.navigation

import kotlinx.serialization.Serializable

/*
 * Navegación type-safe: cada destino es un objeto/clase @Serializable.
 * Entre pantallas solo viajan IDs; cada ViewModel carga el resto desde Room.
 * En la reserva, rescheduleAppointmentId = -1 significa reserva nueva.
 */

@Serializable data object AuthGraph
@Serializable data object ClientGraph
@Serializable data object BusinessGraph

@Serializable data object LoginDestination
@Serializable data object RegisterDestination

@Serializable data object CatalogDestination

@Serializable data class BookingDestination(
    val serviceId: Long,
    val rescheduleAppointmentId: Long = -1L,
)

@Serializable data class HistoryDestination(val asBusiness: Boolean)

@Serializable data object BusinessHomeDestination
@Serializable data object ServicesDestination
@Serializable data object AvailabilityDestination
@Serializable data object TeamDestination
@Serializable data object PaymentsDestination
@Serializable data object NotificationsDestination
