package com.example.turnos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.turnos.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date, startMinutes")
    fun observeAll(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY date, startMinutes")
    fun observeForClient(clientId: Long): Flow<List<AppointmentEntity>>

    /** Citas que ocupan agenda (pendientes o confirmadas) en una fecha. */
    @Query("SELECT * FROM appointments WHERE date = :date AND status IN ('PENDING', 'CONFIRMED')")
    suspend fun getActiveOn(date: String): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getById(id: Long): AppointmentEntity?

    @Insert
    suspend fun insert(appointment: AppointmentEntity): Long

    @Insert
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Query("UPDATE appointments SET status = :status, lateCancellation = :late WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, late: Boolean)

    @Query("UPDATE appointments SET date = :date, startMinutes = :startMinutes, status = 'CONFIRMED' WHERE id = :id")
    suspend fun reschedule(id: Long, date: String, startMinutes: Int)
}
