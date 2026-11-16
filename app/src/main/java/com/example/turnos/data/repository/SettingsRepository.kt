package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.SettingsDao
import com.example.turnos.data.local.entity.BusinessProfileEntity
import com.example.turnos.data.local.entity.toEntity
import com.example.turnos.data.model.BusinessProfile
import com.example.turnos.data.model.MessageTemplate
import com.example.turnos.data.model.NotificationPref
import com.example.turnos.data.model.PaymentMethod
import com.example.turnos.data.model.TemplateKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Configuración del negocio: perfil, pagos, notificaciones y plantillas.
 * La app atiende a un solo local: su perfil es la única fila de business_profile.
 * Al guardar, id = 0 inserta y cualquier otro id actualiza.
 */

class SettingsRepository(private val dao: SettingsDao) {

    fun observeBusinessProfile(): Flow<BusinessProfile> =
        dao.observeBusinessProfile().map { it?.toModel() ?: DEFAULT_PROFILE }

    suspend fun saveBusinessProfile(profile: BusinessProfile) {
        dao.upsertBusinessProfile(
            BusinessProfileEntity(name = profile.name, address = profile.address, phone = profile.phone, rating = profile.rating),
        )
    }

    suspend fun findActiveTemplate(kind: TemplateKind): MessageTemplate? = dao.findActiveTemplate(kind.name)?.toModel()

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

    companion object {
        val DEFAULT_PROFILE = BusinessProfile(name = "Mi local", address = "", phone = "", rating = 0.0)
    }
}
