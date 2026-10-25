package com.example.turnos.domain

import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.data.model.TimeSlot
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Reglas de negocio del documento del proyecto, en Kotlin puro (sin Android),
 * para que sean fáciles de leer y de probar con tests unitarios.
 */
object BusinessRules {

    /** Cada slot de la agenda dura 30 minutos. */
    const val SLOT_STEP_MIN = 30

    /** Cancelar con menos de 2 horas se marca como "cancelación tardía". */
    const val LATE_CANCELLATION_HOURS = 2L

    /** País para la API de feriados. */
    const val COUNTRY_CODE = "GT"

    /** Días que el cliente puede ver hacia adelante para reservar. */
    const val BOOKING_WINDOW_DAYS = 14

    /**
     * Regla: un slot está disponible si
     *  1. cae dentro de un bloque de horario configurado (y el servicio cabe completo),
     *  2. no se traslapa con otra cita activa,
     *  3. no cae en un bloqueo manual,
     *  4. no está en el pasado.
     * La duración del servicio determina cuántos slots consecutivos ocupa.
     */
    fun computeSlots(
        date: LocalDate,
        durationMin: Int,
        blocks: List<TimeBlock>,
        busy: List<Appointment>,
        blockedStarts: Set<Int>,
        now: LocalDateTime = LocalDateTime.now(),
        ignoreAppointmentId: Long? = null,
    ): List<TimeSlot> {
        val nowMinutes = if (date == now.toLocalDate()) now.hour * 60 + now.minute else -1
        if (date.isBefore(now.toLocalDate())) return emptyList()

        val active = busy.filter { it.isActive && it.id != ignoreAppointmentId }
        return blocks.sortedBy { it.startMinutes }.flatMap { block ->
            (block.startMinutes until block.endMinutes step SLOT_STEP_MIN)
                .filter { start -> start + durationMin <= block.endMinutes } // el servicio cabe en el bloque
                .filter { start -> start > nowMinutes }                       // no ofrecer horas pasadas
                .map { start ->
                    val end = start + durationMin
                    val state = when {
                        start in blockedStarts -> SlotState.BLOCKED
                        blockedStarts.any { it in start until end } -> SlotState.OCCUPIED
                        active.any { overlaps(start, end, it.startMinutes, it.endMinutes) } -> SlotState.OCCUPIED
                        else -> SlotState.AVAILABLE
                    }
                    TimeSlot(start, state)
                }
        }
    }

    /** Regla: un cliente no puede tener dos citas activas que se traslapen. */
    fun clientHasOverlap(
        clientAppointments: List<Appointment>,
        date: LocalDate,
        start: Int,
        durationMin: Int,
        ignoreAppointmentId: Long? = null,
    ): Boolean = clientAppointments.any {
        it.isActive && it.id != ignoreAppointmentId && it.date == date &&
            overlaps(start, start + durationMin, it.startMinutes, it.endMinutes)
    }

    /** Regla: cancelación con menos del mínimo de anticipación = tardía. */
    fun isLateCancellation(appointment: Appointment, now: LocalDateTime = LocalDateTime.now()): Boolean =
        Duration.between(now, appointment.startDateTime).toHours() < LATE_CANCELLATION_HOURS

    private fun overlaps(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int) = aStart < bEnd && bStart < aEnd
}
