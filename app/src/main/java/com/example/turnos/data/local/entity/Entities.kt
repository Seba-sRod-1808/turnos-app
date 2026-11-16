package com.example.turnos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.turnos.data.model.*
import java.time.LocalDate

/*
 * Tablas de Room. Los enums se guardan como String (su .name) y las fechas
 * como texto ISO (yyyy-MM-dd) para no necesitar TypeConverters.
 * Cada entidad trae su mapper toModel() hacia el modelo de dominio.
 *
 * La cita guarda nombre del servicio, barbero y precio "congelados" al reservar:
 * si el negocio cambia el precio después, el historial no se altera.
 * dayOfWeek de los días laborables usa java.time: 1 = lunes … 7 = domingo.
 * Un bloqueo manual aplica a un horario puntual: un slot bloqueado no se ofrece.
 */

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val username: String,
    val phone: String = "",
    val passwordHash: String,
    val role: String,
    val mustChangePassword: Boolean = false,
    val securityQuestion: String? = null,
    val securityAnswerHash: String? = null,
) {
    fun toModel() = User(
        id = id,
        name = name,
        username = username,
        role = UserRole.valueOf(role),
        phone = phone,
        mustChangePassword = mustChangePassword,
        hasRecoveryQuestion = securityQuestion != null,
    )
}

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double,
) {
    fun toModel() = BusinessProfile(name, address, phone, rating)

    companion object {
        const val SINGLE_ROW_ID = 1
    }
}

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val durationMin: Int,
    val price: Int,
    val active: Boolean = true,
) {
    fun toModel() = Service(id, name, description, durationMin, price, active)
}

fun Service.toEntity() = ServiceEntity(id, name, description, durationMin, price, active)

@Entity(tableName = "barbers")
data class BarberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val rating: Int,
    val status: String,
    val schedule: String,
) {
    fun toModel(appointmentsToday: Int = 0) =
        Barber(id, name, role, rating, BarberStatus.valueOf(status), schedule, appointmentsToday)
}

@Entity(tableName = "appointments", indices = [Index("date"), Index("clientId"), Index("barberId")])
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceId: Long,
    val serviceName: String,
    val barberId: Long,
    val barberName: String,
    val clientId: Long,
    val clientName: String,
    val date: String,
    val startMinutes: Int,
    val durationMin: Int,
    val price: Int,
    val status: String,
    val lateCancellation: Boolean = false,
) {
    fun toModel() = Appointment(
        id = id,
        serviceId = serviceId,
        serviceName = serviceName,
        barberId = barberId,
        barberName = barberName,
        clientId = clientId,
        clientName = clientName,
        date = LocalDate.parse(date),
        startMinutes = startMinutes,
        durationMin = durationMin,
        price = price,
        status = AppointmentStatus.valueOf(status),
        lateCancellation = lateCancellation,
    )
}

@Entity(tableName = "work_days")
data class WorkDayEntity(
    @PrimaryKey val dayOfWeek: Int,
    val active: Boolean,
)

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startMinutes: Int,
    val endMinutes: Int,
) {
    fun toModel() = TimeBlock(id, startMinutes, endMinutes)
}

@Entity(tableName = "blocked_slots", primaryKeys = ["date", "startMinutes"])
data class BlockedSlotEntity(
    val date: String,
    val startMinutes: Int,
)

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val detail: String,
    val enabled: Boolean,
    val connected: Boolean,
) {
    fun toModel() = PaymentMethod(id, name, detail, enabled, connected)
}

@Entity(tableName = "notification_prefs")
data class NotificationPrefEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
) {
    fun toModel() = NotificationPref(id, title, subtitle, enabled)
}

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val timing: String,
    val channel: String,
    val active: Boolean,
    val kind: String = TemplateKind.CUSTOM.name,
) {
    fun toModel() = MessageTemplate(id, title, body, timing, channel, active, TemplateKind.valueOf(kind))
}

fun MessageTemplate.toEntity() = MessageTemplateEntity(id, title, body, timing, channel, active, kind.name)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val date: String,
    val name: String,
    val year: Int,
    val countryCode: String,
) {
    fun toModel() = Holiday(LocalDate.parse(date), name)
}
