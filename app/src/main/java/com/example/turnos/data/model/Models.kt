package com.example.turnos.data.model

import com.example.turnos.util.formatDateLong
import com.example.turnos.util.formatTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Modelos de dominio que consume la UI. Son independientes de Room y de la API:
 * los repositorios convierten entidades / DTOs a estos modelos.
 */

enum class UserRole { CLIENT, BUSINESS }

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
)

data class Service(
    val id: Long,
    val name: String,
    val description: String,
    val durationMin: Int,
    val price: Int,
    val active: Boolean = true,
)

enum class BarberStatus { AVAILABLE, IN_SERVICE, ABSENT }

data class Barber(
    val id: Long,
    val name: String,
    val role: String,
    val rating: Int = 5,
    val status: BarberStatus = BarberStatus.AVAILABLE,
    val schedule: String = "",
    val appointmentsToday: Int = 0,
)

/** Estados definidos en las reglas de negocio del entregable. */
enum class AppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW }

data class Appointment(
    val id: Long,
    val serviceId: Long,
    val serviceName: String,
    val barberName: String,
    val clientId: Long,
    val clientName: String,
    val date: LocalDate,
    /** Minutos desde medianoche (ej. 14:00 = 840). */
    val startMinutes: Int,
    val durationMin: Int,
    val price: Int,
    val status: AppointmentStatus,
    val lateCancellation: Boolean = false,
) {
    val startDateTime: LocalDateTime get() = date.atTime(LocalTime.of(startMinutes / 60, startMinutes % 60))
    val endMinutes: Int get() = startMinutes + durationMin
    val dateLabel: String get() = "${formatDateLong(date)}, ${formatTime(startMinutes)}"

    /** Una cita "activa" bloquea su horario en la agenda. */
    val isActive: Boolean get() = status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED
}

enum class SlotState { AVAILABLE, OCCUPIED, BLOCKED }

data class TimeSlot(val startMinutes: Int, val state: SlotState) {
    val label: String get() = formatTime(startMinutes)
}

data class TimeBlock(val id: Long, val startMinutes: Int, val endMinutes: Int) {
    val label: String get() = "${formatTime(startMinutes)} - ${formatTime(endMinutes)}"
}

data class PaymentMethod(
    val id: String,
    val name: String,
    val detail: String,
    val enabled: Boolean,
    val connected: Boolean = false,
)

data class NotificationPref(
    val id: String,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
)

data class MessageTemplate(
    val id: Long,
    val title: String,
    val body: String,
    val timing: String,
    val channel: String,
    val active: Boolean,
)

/** Datos fijos del local (perfil del negocio). */
data class BusinessProfile(
    val name: String,
    val address: String,
    val rating: Double,
)

data class Holiday(val date: LocalDate, val name: String)
