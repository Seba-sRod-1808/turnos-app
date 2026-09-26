package com.example.turnos.data.model

/** Modelos de UI. Sin lógica de negocio: el backend los reemplazará. */

data class Service(
    val id: String,
    val name: String,
    val description: String,
    val durationMin: Int,
    val price: Int,
    val active: Boolean = true,
    val paused: Boolean = false,
)

data class Barber(
    val id: String,
    val name: String,
    val role: String,
    val rating: Int = 5,
    val status: BarberStatus = BarberStatus.AVAILABLE,
    val schedule: String = "",
    val appointmentsToday: Int = 0,
)

enum class BarberStatus { AVAILABLE, IN_SERVICE, ABSENT }

/** Estados definidos en las reglas de negocio del entregable. */
enum class AppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW }

data class Appointment(
    val id: String,
    val service: String,
    val barber: String,
    val dateLabel: String,
    val price: Int,
    val durationMin: Int,
    val status: AppointmentStatus,
)

enum class SlotState { AVAILABLE, OCCUPIED, BLOCKED }

data class TimeSlot(val time: String, val state: SlotState)

data class TimeBlock(val id: String, val start: String, val end: String)

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
    val id: String,
    val title: String,
    val preview: String,
    val timing: String,
    val channel: String,
    val active: Boolean,
)

data class BusinessProfile(
    val name: String,
    val address: String,
    val rating: Double,
    val appointmentsToday: Int,
    val pending: Int,
    val revenueToday: Int,
)
