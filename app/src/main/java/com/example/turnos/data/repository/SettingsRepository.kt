package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.SettingsDao
import com.example.turnos.data.local.entity.toEntity
import com.example.turnos.data.model.BusinessProfile
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.model.NotificationPref
import com.example.turnos.data.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Configuración del negocio: perfil, pagos, notificaciones y plantillas.
 * El perfil del local es fijo para esta fase (un solo negocio).
 * Al guardar, id = 0 inserta y cualquier otro id actualiza.
 */

class SettingsRepository(private val dao: SettingsDao) {

    val businessProfile = BusinessProfile(
        name = "Barbería El Clásico",
        address = "5ta Avenida 12-45, Zona 1",
        rating = 4.5,
    )

    fun observePaymentMethods(): Flow<List<PaymentMethod>> =
        dao.observePaymentMethods().map { list -> list.map { it.toModel() } }

    suspend fun setPaymentEnabled(id: String, enabled: Boolean) = dao.setPaymentEnabled(id, enabled)

    fun observeNotificationPrefs(): Flow<List<NotificationPref>> =
        dao.observeNotificationPrefs().map { list -> list.map { it.toModel() } }

    suspend fun setPrefEnabled(id: String, enabled: Boolean) = dao.setPrefEnabled(id, enabled)

    fun observeTemplates(): Flow<List<MessageTemplate>> =
        dao.observeTemplates().map { list -> list.map { it.toModel() } }

    suspend fun saveTemplate(template: MessageTemplate) {
        dao.upsertTemplate(template.toEntity())
    }
}
