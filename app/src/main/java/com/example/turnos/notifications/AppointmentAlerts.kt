package com.example.turnos.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.TemplateKind
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.domain.BusinessRules
import java.time.Duration
import java.time.LocalDateTime

/**
 * Punto único para avisar cambios de una cita: confirma al reservar o reprogramar,
 * programa el recordatorio con WorkManager y lo cancela si la cita se cierra.
 * Cada cita tiene un solo trabajo único, así reprogramar reemplaza el recordatorio anterior.
 */

class AppointmentAlerts(
    context: Context,
    private val notifier: AppointmentNotifier,
    private val settings: SettingsRepository,
) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    suspend fun onBooked(appointment: Appointment) {
        notifier.notifyConfirmed(appointment)
        scheduleReminder(appointment)
    }

    suspend fun onRescheduled(appointment: Appointment) = onBooked(appointment)

    fun onClosed(appointmentId: Long) {
        workManager.cancelUniqueWork(workName(appointmentId))
    }

    private suspend fun scheduleReminder(appointment: Appointment) {
        val template = settings.findActiveTemplate(TemplateKind.REMINDER)
        val now = LocalDateTime.now()
        val remindAt = template?.let {
            BusinessRules.reminderTime(appointment.startDateTime, BusinessRules.reminderHoursFrom(it.timing), now)
        }
        if (remindAt == null) {
            onClosed(appointment.id)
            return
        }
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(Duration.between(now, remindAt))
            .setInputData(workDataOf(ReminderWorker.KEY_APPOINTMENT_ID to appointment.id))
            .build()
        workManager.enqueueUniqueWork(workName(appointment.id), ExistingWorkPolicy.REPLACE, request)
    }

    private fun workName(appointmentId: Long) = "reminder-$appointmentId"
}
