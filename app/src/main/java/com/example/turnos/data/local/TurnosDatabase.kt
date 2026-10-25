package com.example.turnos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.turnos.data.local.dao.*
import com.example.turnos.data.local.entity.*

/**
 * Mientras el esquema cambia entre entregas, la BD se recrea en vez de migrar (dropAllTables).
 */

@Database(
    entities = [
        UserEntity::class,
        ServiceEntity::class,
        BarberEntity::class,
        AppointmentEntity::class,
        WorkDayEntity::class,
        TimeBlockEntity::class,
        BlockedSlotEntity::class,
        PaymentMethodEntity::class,
        NotificationPrefEntity::class,
        MessageTemplateEntity::class,
        HolidayEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class TurnosDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serviceDao(): ServiceDao
    abstract fun barberDao(): BarberDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun availabilityDao(): AvailabilityDao
    abstract fun settingsDao(): SettingsDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        fun build(context: Context): TurnosDatabase =
            Room.databaseBuilder(context, TurnosDatabase::class.java, "turnos.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
