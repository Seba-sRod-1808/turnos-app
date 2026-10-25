package com.example.turnos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.turnos.data.local.entity.BarberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarberDao {
    @Query("SELECT * FROM barbers ORDER BY id")
    fun observeAll(): Flow<List<BarberEntity>>

    @Query("SELECT * FROM barbers WHERE status != 'ABSENT' ORDER BY id LIMIT 1")
    suspend fun firstAvailable(): BarberEntity?

    @Insert
    suspend fun insert(barber: BarberEntity): Long

    @Insert
    suspend fun insertAll(barbers: List<BarberEntity>)

    @Query("UPDATE barbers SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
