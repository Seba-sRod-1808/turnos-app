package com.example.turnos.domain

import com.example.turnos.data.model.Appointment
import com.example.turnos.util.formatDateLong
import com.example.turnos.util.formatTime

/**
 * Reemplaza las variables de las plantillas de mensajes con los datos de la cita.
 */

object TemplateRenderer {
    val VARIABLES = listOf("{fecha}", "{hora}", "{barbero}", "{servicio}", "{cliente}")

    fun render(body: String, appointment: Appointment): String = body
        .replace("{fecha}", "${formatDateLong(appointment.date)}, ${formatTime(appointment.startMinutes)}")
        .replace("{hora}", formatTime(appointment.startMinutes))
        .replace("{barbero}", appointment.barberName)
        .replace("{servicio}", appointment.serviceName)
        .replace("{cliente}", appointment.clientName.substringBefore(" "))
}
