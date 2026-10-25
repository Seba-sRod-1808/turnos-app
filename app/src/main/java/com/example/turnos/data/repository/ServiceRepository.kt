package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.ServiceDao
import com.example.turnos.data.local.entity.toEntity
import com.example.turnos.data.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepository(private val dao: ServiceDao) {

    fun observeAll(): Flow<List<Service>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    /** Lo que ve el cliente: solo servicios activos. */
    fun observeActive(): Flow<List<Service>> = dao.observeActive().map { list -> list.map { it.toModel() } }

    suspend fun getById(id: Long): Service? = dao.getById(id)?.toModel()

    /** id = 0 inserta; cualquier otro id actualiza. */
    suspend fun save(service: Service) {
        dao.upsert(service.toEntity())
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
