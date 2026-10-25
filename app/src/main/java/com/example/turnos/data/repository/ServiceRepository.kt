package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.ServiceDao
import com.example.turnos.data.local.entity.toEntity
import com.example.turnos.data.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Lo que ve el cliente son solo los servicios activos.
 * Al guardar, id = 0 inserta y cualquier otro id actualiza.
 */

class ServiceRepository(private val dao: ServiceDao) {

    fun observeAll(): Flow<List<Service>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeActive(): Flow<List<Service>> = dao.observeActive().map { list -> list.map { it.toModel() } }

    suspend fun getById(id: Long): Service? = dao.getById(id)?.toModel()

    suspend fun save(service: Service) {
        dao.upsert(service.toEntity())
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
