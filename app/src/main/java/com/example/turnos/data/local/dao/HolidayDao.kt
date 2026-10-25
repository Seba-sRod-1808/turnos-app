package com.example.turnos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.turnos.data.local.entity.HolidayEntity

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE year = :year AND countryCode = :countryCode ORDER BY date")
    suspend fun getByYear(year: Int, countryCode: String): List<HolidayEntity>

    @Upsert
    suspend fun upsertAll(holidays: List<HolidayEntity>)
}
