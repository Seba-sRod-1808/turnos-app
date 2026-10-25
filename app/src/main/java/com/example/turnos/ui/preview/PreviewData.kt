package com.example.turnos.ui.preview

import com.example.turnos.data.model.*
import java.time.LocalDate

/**
 * Datos de ejemplo SOLO para los @Preview (la app real lee de Room).
 */

object PreviewData {
    private val today: LocalDate = LocalDate.of(2026, 6, 15)

    val business = BusinessProfile("Barbería El Clásico", "5ta Avenida 12-45, Zona 1", 4.5)

    val services = listOf(
        Service(1, "Corte Clásico", "Corte tradicional y lavado", 45, 150),
        Service(2, "Afeitado con Navaja", "Afeitado tradicional con toalla caliente", 30, 100),
        Service(3, "Arreglo de Barba", "Perfilado y toalla caliente", 30, 90),
        Service(4, "Corte + Barba", "Servicio completo", 70, 220),
        Service(5, "Corte Infantil", "Hasta 12 años", 35, 110, active = false),
    )

    val barbers = listOf(
        Barber(1, "Alex Rivera", "Barbero principal", 5, BarberStatus.AVAILABLE, "09:00 AM - 06:00 PM", 5),
        Barber(2, "Mateo López", "Barbero senior", 5, BarberStatus.IN_SERVICE, "10:00 AM - 07:00 PM", 4),
        Barber(3, "Diego Morales", "Barbero", 4, BarberStatus.ABSENT, "Descanso programado", 0),
    )

    val upcoming = listOf(
        Appointment(1, 1, "Corte Clásico", "Alex Rivera", 1, "Ana López", today.plusDays(2), 14 * 60, 45, 150, AppointmentStatus.CONFIRMED),
        Appointment(2, 2, "Afeitado con Navaja", "Alex Rivera", 1, "Ana López", today.plusDays(7), 10 * 60 + 30, 30, 100, AppointmentStatus.PENDING),
    )

    val past = listOf(
        Appointment(3, 1, "Corte Clásico", "Alex Rivera", 1, "Ana López", today.minusDays(10), 16 * 60, 45, 150, AppointmentStatus.COMPLETED),
        Appointment(4, 3, "Arreglo de Barba", "Mateo López", 1, "Ana López", today.minusDays(20), 11 * 60, 30, 90, AppointmentStatus.CANCELLED, lateCancellation = true),
    )

    val blocks = listOf(TimeBlock(1, 9 * 60, 12 * 60), TimeBlock(2, 14 * 60, 18 * 60))

    val slots = listOf(
        TimeSlot(9 * 60, SlotState.AVAILABLE),
        TimeSlot(10 * 60, SlotState.OCCUPIED),
        TimeSlot(11 * 60, SlotState.AVAILABLE),
        TimeSlot(14 * 60, SlotState.BLOCKED),
        TimeSlot(15 * 60, SlotState.AVAILABLE),
        TimeSlot(16 * 60, SlotState.AVAILABLE),
    )

    val holidays = listOf(
        Holiday(LocalDate.of(2026, 9, 15), "Día de la Independencia"),
        Holiday(LocalDate.of(2026, 10, 20), "Día de la Revolución"),
        Holiday(LocalDate.of(2026, 11, 1), "Día de Todos los Santos"),
    )

    val paymentMethods = listOf(
        PaymentMethod("cash", "Efectivo", "Cobro en el local", true),
        PaymentMethod("card", "Tarjeta", "Visa y Mastercard · comisión 3.5%", true, connected = true),
        PaymentMethod("transfer", "Transferencia bancaria", "Banco Industrial · •••• 4721", true),
        PaymentMethod("link", "Link de pago", "Envía un enlace seguro por WhatsApp", false),
    )

    val notificationPrefs = listOf(
        NotificationPref("new", "Nuevas citas", "Aviso inmediato al equipo asignado", true),
        NotificationPref("changes", "Cambios y cancelaciones", "Notifica reprogramaciones o bajas", true),
        NotificationPref("daily", "Resumen diario", "Cada día a las 8:00 AM", true),
        NotificationPref("payments", "Pagos recibidos", "Solo pagos digitales confirmados", false),
    )

    val templates = listOf(
        MessageTemplate(1, "Confirmación de cita", "Tu cita en Barbería El Clásico está confirmada para el {fecha}.", "Al reservar", "WhatsApp", true),
        MessageTemplate(2, "Recordatorio de cita", "Te esperamos mañana a las {hora} con {barbero}.", "24 h antes", "WhatsApp", true),
        MessageTemplate(3, "Agradecimiento", "Gracias por visitarnos. ¿Cómo fue tu experiencia?", "2 h después", "SMS", false),
    )
}
