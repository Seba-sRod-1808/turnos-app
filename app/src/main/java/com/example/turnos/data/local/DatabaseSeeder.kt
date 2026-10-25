package com.example.turnos.data.local

import androidx.room.withTransaction
import com.example.turnos.data.local.entity.*
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.BarberStatus
import com.example.turnos.data.model.UserRole
import com.example.turnos.util.PasswordHasher
import java.time.LocalDate

/**
 * Llena la base de datos con datos de demostración la primera vez que se abre la app.
 * Las fechas de las citas son relativas al día de instalación para que siempre
 * haya citas próximas y pasadas.
 */

class DatabaseSeeder(private val db: TurnosDatabase) {

    suspend fun seedIfEmpty() {
        if (db.userDao().count() > 0) return
        val today = LocalDate.now()

        db.withTransaction {
            val clientId = db.userDao().insert(
                UserEntity(name = "Ana López", email = DEMO_CLIENT_EMAIL, passwordHash = PasswordHasher.hash(DEMO_PASSWORD), role = UserRole.CLIENT.name),
            )
            db.userDao().insert(
                UserEntity(name = "Barbería El Clásico", email = DEMO_BUSINESS_EMAIL, passwordHash = PasswordHasher.hash(DEMO_PASSWORD), role = UserRole.BUSINESS.name),
            )

            db.serviceDao().insertAll(
                listOf(
                    ServiceEntity(1, "Corte Clásico", "Corte tradicional y lavado", 45, 150),
                    ServiceEntity(2, "Afeitado con Navaja", "Afeitado tradicional con toalla caliente", 30, 100),
                    ServiceEntity(3, "Arreglo de Barba", "Perfilado y toalla caliente", 30, 90),
                    ServiceEntity(4, "Corte + Barba", "Servicio completo", 70, 220),
                    ServiceEntity(5, "Corte Infantil", "Hasta 12 años", 35, 110, active = false),
                )
            )

            db.barberDao().insertAll(
                listOf(
                    BarberEntity(1, "Alex Rivera", "Barbero principal", 5, BarberStatus.AVAILABLE.name, "09:00 AM - 06:00 PM"),
                    BarberEntity(2, "Mateo López", "Barbero senior", 5, BarberStatus.IN_SERVICE.name, "10:00 AM - 07:00 PM"),
                    BarberEntity(3, "Diego Morales", "Barbero", 4, BarberStatus.ABSENT.name, "Descanso programado"),
                )
            )

            db.availabilityDao().upsertWorkDays((1..7).map { WorkDayEntity(it, active = it <= 5) })
            db.availabilityDao().insertBlocks(
                listOf(TimeBlockEntity(startMinutes = 9 * 60, endMinutes = 12 * 60), TimeBlockEntity(startMinutes = 14 * 60, endMinutes = 18 * 60)),
            )

            db.appointmentDao().insertAll(
                listOf(
                    appointment(clientId, "Ana López", 1, "Corte Clásico", "Alex Rivera", today.plusDays(2), 14 * 60, 45, 150, AppointmentStatus.CONFIRMED),
                    appointment(clientId, "Ana López", 2, "Afeitado con Navaja", "Alex Rivera", today.plusDays(5), 10 * 60 + 30, 30, 100, AppointmentStatus.PENDING),
                    appointment(clientId, "Ana López", 1, "Corte Clásico", "Alex Rivera", today.minusDays(10), 16 * 60, 45, 150, AppointmentStatus.COMPLETED),
                    appointment(clientId, "Ana López", 3, "Arreglo de Barba", "Mateo López", today.minusDays(25), 11 * 60, 30, 90, AppointmentStatus.CANCELLED),
                    appointment(0, "Carlos Méndez", 4, "Corte + Barba", "Mateo López", today, 10 * 60, 70, 220, AppointmentStatus.CONFIRMED),
                    appointment(0, "Luis Pérez", 1, "Corte Clásico", "Alex Rivera", today, 15 * 60, 45, 150, AppointmentStatus.PENDING),
                    appointment(0, "Jorge Ramírez", 2, "Afeitado con Navaja", "Alex Rivera", today.minusDays(3), 9 * 60, 30, 100, AppointmentStatus.COMPLETED),
                    appointment(0, "Pedro Castillo", 4, "Corte + Barba", "Mateo López", today.minusDays(1), 14 * 60, 70, 220, AppointmentStatus.COMPLETED),
                )
            )

            db.settingsDao().insertPaymentMethods(
                listOf(
                    PaymentMethodEntity("cash", "Efectivo", "Cobro en el local", enabled = true, connected = false),
                    PaymentMethodEntity("card", "Tarjeta", "Visa y Mastercard · comisión 3.5%", enabled = true, connected = true),
                    PaymentMethodEntity("transfer", "Transferencia bancaria", "Banco Industrial · •••• 4721", enabled = true, connected = false),
                    PaymentMethodEntity("link", "Link de pago", "Envía un enlace seguro por WhatsApp", enabled = false, connected = false),
                )
            )
            db.settingsDao().insertNotificationPrefs(
                listOf(
                    NotificationPrefEntity("new", "Nuevas citas", "Aviso inmediato al equipo asignado", true),
                    NotificationPrefEntity("changes", "Cambios y cancelaciones", "Notifica reprogramaciones o bajas", true),
                    NotificationPrefEntity("daily", "Resumen diario", "Cada día a las 8:00 AM", true),
                    NotificationPrefEntity("payments", "Pagos recibidos", "Solo pagos digitales confirmados", false),
                )
            )
            db.settingsDao().insertTemplates(
                listOf(
                    MessageTemplateEntity(title = "Confirmación de cita", body = "Tu cita en Barbería El Clásico está confirmada para el {fecha}.", timing = "Al reservar", channel = "WhatsApp", active = true),
                    MessageTemplateEntity(title = "Recordatorio de cita", body = "Te esperamos mañana a las {hora} con {barbero}.", timing = "24 h antes", channel = "WhatsApp", active = true),
                    MessageTemplateEntity(title = "Agradecimiento", body = "Gracias por visitarnos. ¿Cómo fue tu experiencia?", timing = "2 h después", channel = "SMS", active = false),
                )
            )
        }
    }

    private fun appointment(
        clientId: Long, clientName: String, serviceId: Long, serviceName: String, barber: String,
        date: LocalDate, start: Int, duration: Int, price: Int, status: AppointmentStatus,
    ) = AppointmentEntity(
        serviceId = serviceId, serviceName = serviceName, barberName = barber,
        clientId = clientId, clientName = clientName, date = date.toString(),
        startMinutes = start, durationMin = duration, price = price, status = status.name,
    )

    companion object {
        const val DEMO_CLIENT_EMAIL = "cliente@turnos.com"
        const val DEMO_BUSINESS_EMAIL = "negocio@turnos.com"
        const val DEMO_PASSWORD = "123456"
    }
}
