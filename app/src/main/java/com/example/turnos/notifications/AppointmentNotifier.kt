package com.example.turnos.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.turnos.MainActivity
import com.example.turnos.R
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.TemplateKind
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.domain.TemplateRenderer

/**
 * Publica las notificaciones locales de citas usando las plantillas que configura el negocio.
 * Si la plantilla está pausada o el usuario no dio permiso, no se muestra nada.
 */

class AppointmentNotifier(
    private val context: Context,
    private val settings: SettingsRepository,
) {
    fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Citas", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Confirmaciones y recordatorios de tus citas"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    suspend fun notifyConfirmed(appointment: Appointment) =
        notifyFromTemplate(TemplateKind.CONFIRMATION, appointment, title = "Cita confirmada")

    suspend fun notifyReminder(appointment: Appointment) =
        notifyFromTemplate(TemplateKind.REMINDER, appointment, title = "Recordatorio de cita")

    fun notifyTest(): Boolean = post(
        id = TEST_NOTIFICATION_ID,
        title = "Notificación de prueba",
        body = "Así verán tus clientes las confirmaciones y recordatorios de Turnos.",
    )

    private suspend fun notifyFromTemplate(kind: TemplateKind, appointment: Appointment, title: String): Boolean {
        val template = settings.findActiveTemplate(kind) ?: return false
        return post(
            id = appointment.id.toInt() * NOTIFICATIONS_PER_APPOINTMENT + kind.ordinal,
            title = title,
            body = TemplateRenderer.render(template.body, appointment),
        )
    }

    private fun post(id: Int, title: String, body: String): Boolean {
        if (!canNotify()) return false
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_orange))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        return try {
            NotificationManagerCompat.from(context).notify(id, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private companion object {
        const val CHANNEL_ID = "appointments"
        const val TEST_NOTIFICATION_ID = 1
        const val NOTIFICATIONS_PER_APPOINTMENT = 10
    }
}
