package com.example.turnos.data.mock

import com.example.turnos.data.model.*

/** Datos de prueba que reflejan los diseños. Reemplazar por repositorios reales. */
object MockData {

    val business = BusinessProfile(
        name = "Barbería El Clásico",
        address = "5ta Avenida 12-45, Zona 1",
        rating = 4.5,
        appointmentsToday = 8,
        pending = 3,
        revenueToday = 1200,
    )

    val services = listOf(
        Service("s1", "Corte Clásico", "Corte tradicional y lavado", 45, 150),
        Service("s2", "Afeitado con Navaja", "Afeitado tradicional con toalla caliente", 30, 100),
        Service("s3", "Arreglo de Barba", "Perfilado y toalla caliente", 30, 90),
        Service("s4", "Corte + Barba", "Servicio completo", 70, 220),
        Service("s5", "Corte Infantil", "Hasta 12 años", 35, 110, active = false, paused = true),
    )

    val barbers = listOf(
        Barber("b1", "Alex Rivera", "Barbero principal", 5, BarberStatus.AVAILABLE, "09:00 AM - 06:00 PM", 5),
        Barber("b2", "Mateo López", "Barbero senior", 5, BarberStatus.IN_SERVICE, "10:00 AM - 07:00 PM", 4),
        Barber("b3", "Diego Morales", "Barbero", 4, BarberStatus.ABSENT, "Descanso programado", 0),
    )

    val upcoming = listOf(
        Appointment("a1", "Corte Clásico", "Alex Rivera", "15 de Junio, 2:00 PM", 150, 45, AppointmentStatus.CONFIRMED),
        Appointment("a2", "Afeitado con Navaja", "Alex Rivera", "22 de Junio, 10:30 AM", 100, 30, AppointmentStatus.PENDING),
    )

    val past = listOf(
        Appointment("a3", "Corte Clásico", "Alex Rivera", "10 de Mayo, 4:00 PM", 150, 45, AppointmentStatus.COMPLETED),
        Appointment("a4", "Arreglo de Barba", "Mateo López", "28 de Abril, 11:00 AM", 90, 30, AppointmentStatus.CANCELLED),
    )

    val workDays = listOf("L", "M", "Mi", "J", "V", "S", "D")

    val timeBlocks = listOf(
        TimeBlock("t1", "09:00 AM", "12:00 PM"),
        TimeBlock("t2", "02:00 PM", "06:00 PM"),
    )

    val previewSlots = listOf(
        TimeSlot("09:00 AM", SlotState.AVAILABLE),
        TimeSlot("10:00 AM", SlotState.OCCUPIED),
        TimeSlot("11:00 AM", SlotState.AVAILABLE),
        TimeSlot("02:00 PM", SlotState.BLOCKED),
        TimeSlot("03:00 PM", SlotState.AVAILABLE),
    )

    val paymentMethods = listOf(
        PaymentMethod("p1", "Efectivo", "Cobro en el local", true),
        PaymentMethod("p2", "Tarjeta", "Visa y Mastercard · comisión 3.5%", true, connected = true),
        PaymentMethod("p3", "Transferencia bancaria", "Banco Industrial · •••• 4721", true),
        PaymentMethod("p4", "Link de pago", "Envía un enlace seguro por WhatsApp", false),
    )

    val notificationPrefs = listOf(
        NotificationPref("n1", "Nuevas citas", "Aviso inmediato al equipo asignado", true),
        NotificationPref("n2", "Cambios y cancelaciones", "Notifica reprogramaciones o bajas", true),
        NotificationPref("n3", "Resumen diario", "Cada día a las 8:00 AM", true),
        NotificationPref("n4", "Pagos recibidos", "Solo pagos digitales confirmados", false),
    )

    val templates = listOf(
        MessageTemplate("m1", "Confirmación de cita", "Tu cita en Barbería El Clásico está confirmada para el {fecha}.", "Al reservar", "WhatsApp", true),
        MessageTemplate("m2", "Recordatorio de cita", "Te esperamos mañana a las {hora} con {barbero}.", "24 h antes", "WhatsApp", true),
        MessageTemplate("m3", "Agradecimiento", "Gracias por visitarnos. ¿Cómo fue tu experiencia?", "2 h después", "SMS", false),
    )
}
