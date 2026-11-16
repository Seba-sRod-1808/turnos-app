package com.example.turnos.data.local

import androidx.room.withTransaction
import com.example.turnos.data.local.entity.*
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.BarberStatus
import com.example.turnos.data.model.TemplateKind
import com.example.turnos.data.model.UserRole
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.util.PasswordHasher
import java.time.LocalDate

/**
 * Llena la base de datos con datos de demostración la primera vez que se abre la app.
 * Las fechas de las citas son relativas al día de instalación para que siempre
 * haya citas próximas, pasadas y una pendiente de cerrar (la de ayer).
 * "pedro" entra con contraseña temporal para mostrar el flujo de primer ingreso.
 */

class DatabaseSeeder(private val db: TurnosDatabase) {

    suspend fun seedIfEmpty() {
        if (db.userDao().count() > 0) return
        val today = LocalDate.now()

        db.withTransaction {
            db.userDao().insert(
                UserEntity(
                    name = "Carlos Méndez",
                    username = DEMO_BUSINESS_USERNAME,
                    phone = "5555-0101",
                    passwordHash = PasswordHasher.hash(DEMO_PASSWORD),
                    role = UserRole.BUSINESS.name,
                    securityQuestion = AuthRepository.SECURITY_QUESTIONS[1],
                    securityAnswerHash = PasswordHasher.hashAnswer(DEMO_SECURITY_ANSWER),
                ),
            )
            val clientId = db.userDao().insert(
                UserEntity(
                    name = "Ana López",
                    username = DEMO_CLIENT_USERNAME,
                    phone = "5555-0202",
                    passwordHash = PasswordHasher.hash(DEMO_PASSWORD),
                    role = UserRole.CLIENT.name,
                    securityQuestion = AuthRepository.SECURITY_QUESTIONS[1],
                    securityAnswerHash = PasswordHasher.hashAnswer(DEMO_SECURITY_ANSWER),
                ),
            )
            db.userDao().insert(
                UserEntity(
                    name = "Pedro Castillo",
                    username = DEMO_NEW_CLIENT_USERNAME,
                    passwordHash = PasswordHasher.hash(DEMO_TEMPORARY_PASSWORD),
                    role = UserRole.CLIENT.name,
                    mustChangePassword = true,
                ),
            )

            db.settingsDao().upsertBusinessProfile(
                BusinessProfileEntity(name = "Barbería El Clásico", address = "5ta Avenida 12-45, Zona 1", phone = "2222-3344", rating = 4.5),
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
                    appointment(clientId, "Ana López", 1, "Corte Clásico", ALEX, today.plusDays(2), 14 * 60, 45, 150, AppointmentStatus.CONFIRMED),
                    appointment(clientId, "Ana López", 2, "Afeitado con Navaja", ALEX, today.plusDays(5), 10 * 60 + 30, 30, 100, AppointmentStatus.PENDING),
                    appointment(clientId, "Ana López", 1, "Corte Clásico", ALEX, today.minusDays(10), 16 * 60, 45, 150, AppointmentStatus.COMPLETED),
                    appointment(clientId, "Ana López", 3, "Arreglo de Barba", MATEO, today.minusDays(25), 11 * 60, 30, 90, AppointmentStatus.CANCELLED),
                    appointment(0, "Luis Pérez", 4, "Corte + Barba", MATEO, today, 10 * 60, 70, 220, AppointmentStatus.CONFIRMED),
                    appointment(0, "Jorge Ramírez", 1, "Corte Clásico", ALEX, today, 15 * 60, 45, 150, AppointmentStatus.PENDING),
                    appointment(0, "Mario Gómez", 2, "Afeitado con Navaja", ALEX, today.minusDays(1), 17 * 60, 30, 100, AppointmentStatus.CONFIRMED),
                    appointment(0, "Jorge Ramírez", 2, "Afeitado con Navaja", ALEX, today.minusDays(3), 9 * 60, 30, 100, AppointmentStatus.COMPLETED),
                    appointment(0, "Raúl Ortiz", 4, "Corte + Barba", MATEO, today.minusDays(1), 14 * 60, 70, 220, AppointmentStatus.COMPLETED),
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
                    MessageTemplateEntity(title = "Confirmación de cita", body = "Tu cita de {servicio} está confirmada para el {fecha} con {barbero}.", timing = "Al reservar", channel = "App", active = true, kind = TemplateKind.CONFIRMATION.name),
                    MessageTemplateEntity(title = "Recordatorio de cita", body = "Te esperamos a las {hora} con {barbero} para tu {servicio}.", timing = "2 h antes", channel = "App", active = true, kind = TemplateKind.REMINDER.name),
                    MessageTemplateEntity(title = "Agradecimiento", body = "Gracias por visitarnos. ¿Cómo fue tu experiencia?", timing = "2 h después", channel = "SMS", active = false, kind = TemplateKind.FOLLOW_UP.name),
                )
            )
        }
    }

    private fun appointment(
        clientId: Long, clientName: String, serviceId: Long, serviceName: String, barber: Pair<Long, String>,
        date: LocalDate, start: Int, duration: Int, price: Int, status: AppointmentStatus,
    ) = AppointmentEntity(
        serviceId = serviceId, serviceName = serviceName, barberId = barber.first, barberName = barber.second,
        clientId = clientId, clientName = clientName, date = date.toString(),
        startMinutes = start, durationMin = duration, price = price, status = status.name,
    )

    companion object {
        const val DEMO_CLIENT_USERNAME = "cliente"
        const val DEMO_BUSINESS_USERNAME = "negocio"
        const val DEMO_NEW_CLIENT_USERNAME = "pedro"
        const val DEMO_PASSWORD = "123456"
        const val DEMO_TEMPORARY_PASSWORD = "temporal"
        const val DEMO_SECURITY_ANSWER = "guatemala"

        private val ALEX = 1L to "Alex Rivera"
        private val MATEO = 2L to "Mateo López"
    }
}
