package com.example.turnos.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.turnos.TurnosApp
import java.time.LocalDateTime

/**
 * Se ejecuta a la hora del recordatorio. Vuelve a leer la cita por si cambió o se canceló.
 */

class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as TurnosApp).container
        val appointment = container.appointmentRepository.getById(inputData.getLong(KEY_APPOINTMENT_ID, -1L))
        if (appointment != null && appointment.isActive && !appointment.hasStarted(LocalDateTime.now())) {
            container.appointmentNotifier.notifyReminder(appointment)
        }
        return Result.success()
    }

    companion object {
        const val KEY_APPOINTMENT_ID = "appointment_id"
    }
}
