package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.AvailabilityDao
import com.example.turnos.data.local.entity.BlockedSlotEntity
import com.example.turnos.data.local.entity.TimeBlockEntity
import com.example.turnos.data.local.entity.WorkDayEntity
import com.example.turnos.data.model.TimeBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Horario semanal del negocio + bloqueos manuales.
 * Los días se indexan 0 = lunes … 6 = domingo.
 */

class AvailabilityRepository(private val dao: AvailabilityDao) {

    fun observeWorkDays(): Flow<List<Boolean>> = dao.observeWorkDays().map { it.toWeek() }

    fun observeBlocks(): Flow<List<TimeBlock>> = dao.observeBlocks().map { list -> list.map { it.toModel() } }

    fun observeBlockedStarts(date: LocalDate): Flow<Set<Int>> =
        dao.observeBlockedOn(date.toString()).map { list -> list.map { it.startMinutes }.toSet() }

    suspend fun getWorkDays(): List<Boolean> = dao.getWorkDays().toWeek()

    suspend fun getBlocks(): List<TimeBlock> = dao.getBlocks().map { it.toModel() }

    suspend fun getBlockedStarts(date: LocalDate): Set<Int> =
        dao.getBlockedOn(date.toString()).map { it.startMinutes }.toSet()

    suspend fun saveSchedule(workDays: List<Boolean>, blocks: List<TimeBlock>) {
        dao.replaceSchedule(
            days = workDays.mapIndexed { i, active -> WorkDayEntity(dayOfWeek = i + 1, active = active) },
            blocks = blocks.map { TimeBlockEntity(startMinutes = it.startMinutes, endMinutes = it.endMinutes) },
        )
    }

    suspend fun setBlocked(date: LocalDate, startMinutes: Int, blocked: Boolean) {
        if (blocked) dao.insertBlocked(BlockedSlotEntity(date.toString(), startMinutes))
        else dao.deleteBlocked(date.toString(), startMinutes)
    }

    private fun List<WorkDayEntity>.toWeek(): List<Boolean> =
        (1..7).map { day -> firstOrNull { it.dayOfWeek == day }?.active ?: false }
}
