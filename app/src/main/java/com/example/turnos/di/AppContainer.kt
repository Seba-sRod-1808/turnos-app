package com.example.turnos.di

import android.content.Context
import com.example.turnos.data.local.DatabaseSeeder
import com.example.turnos.data.local.TurnosDatabase
import com.example.turnos.data.remote.NetworkModule
import com.example.turnos.data.repository.*

/**
 * Inyección de dependencias manual: un solo lugar donde se crean la base de datos,
 * el cliente de red y los repositorios. Cada ViewModel recibe aquí lo que necesita
 * por constructor (sin Hilt/Koin para no agregar dependencias pesadas).
 */
class AppContainer(context: Context) {

    private val database = TurnosDatabase.build(context)
    private val holidaysApi = NetworkModule.holidaysApi()

    val seeder = DatabaseSeeder(database)

    val sessionRepository = SessionRepository()
    val authRepository = AuthRepository(database.userDao(), sessionRepository)
    val serviceRepository = ServiceRepository(database.serviceDao())
    val barberRepository = BarberRepository(database.barberDao(), database.appointmentDao())
    val appointmentRepository = AppointmentRepository(database.appointmentDao())
    val availabilityRepository = AvailabilityRepository(database.availabilityDao())
    val settingsRepository = SettingsRepository(database.settingsDao())
    val holidayRepository = HolidayRepository(holidaysApi, database.holidayDao())
}
