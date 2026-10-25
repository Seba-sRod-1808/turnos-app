package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.AppointmentDao
import com.example.turnos.data.local.entity.AppointmentEntity
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class AppointmentRepository(private val dao: AppointmentDao) {

    fun observeAll(): Flow<List<Appointment>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeForClient(clientId: Long): Flow<List<Appointment>> =
        dao.observeForClient(clientId).map { list -> list.map { it.toModel() } }

    suspend fun getActiveOn(date: LocalDate): List<Appointment> = dao.getActiveOn(date.toString()).map { it.toModel() }

    suspend fun getById(id: Long): Appointment? = dao.getById(id)?.toModel()

    suspend fun create(appointment: Appointment): Long = dao.insert(
        AppointmentEntity(
            serviceId = appointment.serviceId,
            serviceName = appointment.serviceName,
            barberName = appointment.barberName,
            clientId = appointment.clientId,
            clientName = appointment.clientName,
            date = appointment.date.toString(),
            startMinutes = appointment.startMinutes,
            durationMin = appointment.durationMin,
            price = appointment.price,
            status = appointment.status.name,
        )
    )

    suspend fun reschedule(id: Long, date: LocalDate, startMinutes: Int) =
        dao.reschedule(id, date.toString(), startMinutes)

    suspend fun updateStatus(id: Long, status: AppointmentStatus, lateCancellation: Boolean = false) =
        dao.updateStatus(id, status.name, lateCancellation)
}
