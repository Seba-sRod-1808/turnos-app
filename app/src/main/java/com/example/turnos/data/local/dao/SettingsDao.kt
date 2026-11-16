package com.example.turnos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.turnos.data.local.entity.BusinessProfileEntity
import com.example.turnos.data.local.entity.MessageTemplateEntity
import com.example.turnos.data.local.entity.NotificationPrefEntity
import com.example.turnos.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

/**
 * Configuración del negocio: perfil, métodos de pago, notificaciones y plantillas.
 */

@Dao
interface SettingsDao {
    @Query("SELECT * FROM business_profile WHERE id = 1")
    fun observeBusinessProfile(): Flow<BusinessProfileEntity?>

    @Upsert
    suspend fun upsertBusinessProfile(profile: BusinessProfileEntity)

    @Query("SELECT * FROM payment_methods")
    fun observePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("UPDATE payment_methods SET enabled = :enabled WHERE id = :id")
    suspend fun setPaymentEnabled(id: String, enabled: Boolean)

    @Insert
    suspend fun insertPaymentMethods(methods: List<PaymentMethodEntity>)

    @Query("SELECT * FROM notification_prefs")
    fun observeNotificationPrefs(): Flow<List<NotificationPrefEntity>>

    @Query("UPDATE notification_prefs SET enabled = :enabled WHERE id = :id")
    suspend fun setPrefEnabled(id: String, enabled: Boolean)

    @Insert
    suspend fun insertNotificationPrefs(prefs: List<NotificationPrefEntity>)

    @Query("SELECT * FROM message_templates ORDER BY id")
    fun observeTemplates(): Flow<List<MessageTemplateEntity>>

    @Query("SELECT * FROM message_templates WHERE kind = :kind AND active = 1 LIMIT 1")
    suspend fun findActiveTemplate(kind: String): MessageTemplateEntity?

    @Upsert
    suspend fun upsertTemplate(template: MessageTemplateEntity): Long

    @Insert
    suspend fun insertTemplates(templates: List<MessageTemplateEntity>)
}
