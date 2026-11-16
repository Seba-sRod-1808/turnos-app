package com.example.turnos.domain

import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BusinessRulesTest {

    private val day = LocalDate.of(2026, 11, 18)
    private val morning = day.atTime(8, 0)
    private val blocks = listOf(TimeBlock(1, 9 * 60, 12 * 60))

    private fun appointment(
        start: Int,
        duration: Int = 30,
        status: AppointmentStatus = AppointmentStatus.CONFIRMED,
        id: Long = 1,
        date: LocalDate = day,
    ) = Appointment(id, 1, "Corte", 1, "Alex", 1, "Ana", date, start, duration, 100, status)

    @Test
    fun `slots dentro del bloque y sin citas estan disponibles`() {
        val slots = BusinessRules.computeSlots(day, 30, blocks, emptyList(), emptySet(), morning)
        assertEquals(6, slots.size)
        assertTrue(slots.all { it.state == SlotState.AVAILABLE })
    }

    @Test
    fun `el servicio debe caber completo dentro del bloque`() {
        val slots = BusinessRules.computeSlots(day, 70, blocks, emptyList(), emptySet(), morning)
        assertEquals(listOf(540, 570, 600, 630), slots.map { it.startMinutes })
    }

    @Test
    fun `una cita activa ocupa todos los slots que se traslapan`() {
        val busy = listOf(appointment(start = 10 * 60, duration = 60))
        val slots = BusinessRules.computeSlots(day, 30, blocks, busy, emptySet(), morning).associateBy { it.startMinutes }
        assertEquals(SlotState.OCCUPIED, slots.getValue(600).state)
        assertEquals(SlotState.OCCUPIED, slots.getValue(630).state)
        assertEquals(SlotState.AVAILABLE, slots.getValue(660).state)
    }

    @Test
    fun `una cita cancelada no ocupa agenda`() {
        val busy = listOf(appointment(start = 10 * 60, status = AppointmentStatus.CANCELLED))
        val slots = BusinessRules.computeSlots(day, 30, blocks, busy, emptySet(), morning)
        assertTrue(slots.all { it.state == SlotState.AVAILABLE })
    }

    @Test
    fun `un bloqueo manual marca el slot como bloqueado`() {
        val slots = BusinessRules.computeSlots(day, 30, blocks, emptyList(), setOf(9 * 60), morning)
        assertEquals(SlotState.BLOCKED, slots.first().state)
    }

    @Test
    fun `no se ofrecen horas que ya pasaron`() {
        val slots = BusinessRules.computeSlots(day, 30, blocks, emptyList(), emptySet(), day.atTime(10, 15))
        assertEquals(listOf(630, 660, 690), slots.map { it.startMinutes })
    }

    @Test
    fun `al reprogramar la propia cita no bloquea su horario`() {
        val busy = listOf(appointment(start = 9 * 60, id = 7))
        val slots = BusinessRules.computeSlots(day, 30, blocks, busy, emptySet(), morning, ignoreAppointmentId = 7)
        assertEquals(SlotState.AVAILABLE, slots.first().state)
    }

    @Test
    fun `un cliente no puede tener dos citas traslapadas`() {
        val mine = listOf(appointment(start = 10 * 60, duration = 45))
        assertTrue(BusinessRules.clientHasOverlap(mine, day, 10 * 60 + 30, 30))
        assertFalse(BusinessRules.clientHasOverlap(mine, day, 10 * 60 + 45, 30))
    }

    @Test
    fun `cancelar con menos de dos horas es tardio y ya no se puede reprogramar`() {
        val a = appointment(start = 10 * 60)
        val now = day.atTime(8, 30)
        assertTrue(BusinessRules.isLateCancellation(a, now))
        assertFalse(BusinessRules.canReschedule(a, now))
        assertFalse(BusinessRules.isLateCancellation(a, day.atTime(7, 59)))
    }

    @Test
    fun `solo se cierra una cita cuando ya llego su hora`() {
        val a = appointment(start = 10 * 60)
        assertFalse(BusinessRules.canClose(a, day.atTime(9, 59)))
        assertTrue(BusinessRules.canClose(a, day.atTime(10, 0)))
        assertFalse(BusinessRules.canClose(a.copy(status = AppointmentStatus.COMPLETED), day.atTime(11, 0)))
    }

    @Test
    fun `las horas del recordatorio se leen de la plantilla`() {
        assertEquals(24L, BusinessRules.reminderHoursFrom("24 h antes"))
        assertEquals(BusinessRules.DEFAULT_REMINDER_HOURS, BusinessRules.reminderHoursFrom("Al reservar"))
    }

    @Test
    fun `el recordatorio se adelanta si la cita se reservo con poca anticipacion`() {
        val start = day.atTime(14, 0)
        assertEquals(day.atTime(12, 0), BusinessRules.reminderTime(start, 2, day.atTime(9, 0)))
        assertEquals(day.atTime(13, 45), BusinessRules.reminderTime(start, 2, day.atTime(13, 0)))
        assertNull(BusinessRules.reminderTime(start, 2, day.atTime(13, 50)))
    }

    @Test
    fun `la plantilla reemplaza las variables con los datos de la cita`() {
        val text = TemplateRenderer.render("{cliente}: {servicio} con {barbero} a las {hora}", appointment(start = 14 * 60))
        assertEquals("Ana: Corte con Alex a las 02:00 PM", text)
    }
}
