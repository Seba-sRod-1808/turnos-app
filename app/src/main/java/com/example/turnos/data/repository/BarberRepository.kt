package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.AppointmentDao
import com.example.turnos.data.local.dao.BarberDao
import com.example.turnos.data.local.entity.BarberEntity
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.Barber
import com.example.turnos.data.model.BarberStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class BarberRepository(
    private val barberDao: BarberDao,
    private val appointmentDao: AppointmentDao,
) {
    /** Equipo con la cantidad de citas activas de hoy por barbero. */
    fun observeTeam(today: LocalDate = LocalDate.now()): Flow<List<Barber>> =
        combine(barberDao.observeAll(), appointmentDao.observeAll()) { barbers, appointments ->
            val todayActive = appointments.filter {
                it.date == today.toString() &&
                    (it.status == AppointmentStatus.PENDING.name || it.status == AppointmentStatus.CONFIRMED.name)
            }
            barbers.map { b -> b.toModel(appointmentsToday = todayActive.count { it.barberName == b.name }) }
        }

    suspend fun add(name: String, role: String) {
        barberDao.insert(
            BarberEntity(name = name.trim(), role = role.trim(), rating = 5, status = BarberStatus.AVAILABLE.name, schedule = "09:00 AM - 06:00 PM"),
        )
    }

    suspend fun setStatus(id: Long, status: BarberStatus) = barberDao.updateStatus(id, status.name)

    /** Barbero que se asigna a una reserva nueva (el primero que no está ausente). */
    suspend fun firstAvailableName(): String = barberDao.firstAvailable()?.name ?: "Por asignar"
}
