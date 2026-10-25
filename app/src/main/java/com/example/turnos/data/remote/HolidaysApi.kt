package com.example.turnos.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Nager.Date: API pública y gratuita de feriados (no requiere API key).
 * Documentación: https://github.com/nager/Nager.Date
 */
interface HolidaysApi {

    /**
     * Devuelve 200 con la lista, o 204 sin cuerpo si el país no está soportado.
     * Por eso se usa Response<…> en lugar de la lista directa.
     */
    @GET("v3/publicholidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String,
    ): Response<List<HolidayDto>>

    companion object {
        const val BASE_URL = "https://nagerholidays.com/api/"
    }
}

/** DTO: solo los campos que usamos; el resto se ignora (ignoreUnknownKeys). */
@Serializable
data class HolidayDto(
    val date: String,
    val name: String,
    val localName: String? = null,
    val countryCode: String? = null,
)
