package com.example.turnos.navigation

import kotlinx.serialization.Serializable

/*
 * Navegación type-safe: cada destino es un objeto/clase @Serializable.
 * Entre pantallas solo viajan IDs; cada ViewModel carga el resto desde Room.
 */

// ----- Grafos anidados -----
@Serializable data object AuthGraph
@Serializable data object ClientGraph
@Serializable data object BusinessGraph

// ----- Autenticación -----
@Serializable data object LoginDestination
@Serializable data object RegisterDestination

// ----- Cliente -----
@Serializable data object CatalogDestination

/**
 * @param serviceId servicio a reservar.
 * @param rescheduleAppointmentId cita a reprogramar; -1 = reserva nueva.
 */
@Serializable data class BookingDestination(
    val serviceId: Long,
    val rescheduleAppointmentId: Long = -1L,
)

// ----- Compartido -----
/** El mismo historial sirve al cliente (sus citas) y al negocio (toda la agenda). */
@Serializable data class HistoryDestination(val asBusiness: Boolean)

// ----- Negocio -----
@Serializable data object BusinessHomeDestination
@Serializable data object ServicesDestination
@Serializable data object AvailabilityDestination
@Serializable data object TeamDestination
@Serializable data object PaymentsDestination
@Serializable data object NotificationsDestination
