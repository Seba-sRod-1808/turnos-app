package com.example.turnos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.turnos.data.local.entity.BlockedSlotEntity
import com.example.turnos.data.local.entity.TimeBlockEntity
import com.example.turnos.data.local.entity.WorkDayEntity
import kotlinx.coroutines.flow.Flow

/**
 * Días laborables y bloques de horario. Guardar días y bloques usa una sola transacción (todo o nada).
 */

@Dao
interface AvailabilityDao {
    @Query("SELECT * FROM work_days ORDER BY dayOfWeek")
    fun observeWorkDays(): Flow<List<WorkDayEntity>>

    @Query("SELECT * FROM work_days ORDER BY dayOfWeek")
    suspend fun getWorkDays(): List<WorkDayEntity>

    @Upsert
    suspend fun upsertWorkDays(days: List<WorkDayEntity>)

    @Query("SELECT * FROM time_blocks ORDER BY startMinutes")
    fun observeBlocks(): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_blocks ORDER BY startMinutes")
    suspend fun getBlocks(): List<TimeBlockEntity>

    @Query("DELETE FROM time_blocks")
    suspend fun clearBlocks()

    @Insert
    suspend fun insertBlocks(blocks: List<TimeBlockEntity>)

    @Transaction
    suspend fun replaceSchedule(days: List<WorkDayEntity>, blocks: List<TimeBlockEntity>) {
        upsertWorkDays(days)
        clearBlocks()
        insertBlocks(blocks)
    }

    @Query("SELECT * FROM blocked_slots WHERE date = :date")
    fun observeBlockedOn(date: String): Flow<List<BlockedSlotEntity>>

    @Query("SELECT * FROM blocked_slots WHERE date = :date")
    suspend fun getBlockedOn(date: String): List<BlockedSlotEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBlocked(slot: BlockedSlotEntity)

    @Query("DELETE FROM blocked_slots WHERE date = :date AND startMinutes = :startMinutes")
    suspend fun deleteBlocked(date: String, startMinutes: Int)
}
