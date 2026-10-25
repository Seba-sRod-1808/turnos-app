package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.HolidayDao
import com.example.turnos.data.local.entity.HolidayEntity
import com.example.turnos.data.model.Holiday
import com.example.turnos.data.remote.HolidaysApi
import com.example.turnos.domain.BusinessRules
import kotlinx.coroutines.CancellationException

/**
 * Estrategia "network first, cache fallback":
 *  1. Intenta descargar los feriados de la API.
 *  2. Si funciona, los guarda en Room.
 *  3. Si falla (sin internet), devuelve lo último guardado.
 * Un 204 de la API (país sin datos) se trata como lista vacía.
 */

data class HolidaysResult(
    val holidays: List<Holiday>,
    val fromCache: Boolean,
)

class HolidayRepository(
    private val api: HolidaysApi,
    private val dao: HolidayDao,
    private val countryCode: String = BusinessRules.COUNTRY_CODE,
) {
    suspend fun getHolidays(year: Int): Result<HolidaysResult> {
        return try {
            val response = api.getPublicHolidays(year, countryCode)
            if (!response.isSuccessful) error("HTTP ${response.code()}")
            val remote = response.body().orEmpty()
            dao.upsertAll(remote.map { HolidayEntity(it.date, it.localName ?: it.name, year, countryCode) })
            Result.success(HolidaysResult(loadCached(year), fromCache = false))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val cached = loadCached(year)
            if (cached.isNotEmpty()) Result.success(HolidaysResult(cached, fromCache = true))
            else Result.failure(e)
        }
    }

    private suspend fun loadCached(year: Int) = dao.getByYear(year, countryCode).map { it.toModel() }
}
