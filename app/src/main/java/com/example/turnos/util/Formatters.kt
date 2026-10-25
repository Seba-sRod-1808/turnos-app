package com.example.turnos.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Spanish: Locale = Locale.forLanguageTag("es-GT")
private val shortDay = DateTimeFormatter.ofPattern("EEE d", Spanish)
private val longDate = DateTimeFormatter.ofPattern("d 'de' MMMM", Spanish)
private val fullDate = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Spanish)
private val monthName = DateTimeFormatter.ofPattern("MMMM", Spanish)

fun formatTime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    val suffix = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.US, "%02d:%02d %s", h12, m, suffix)
}

fun formatDayChip(date: LocalDate): String = shortDay.format(date).replace(".", "").capitalizeFirst()

fun formatDateLong(date: LocalDate): String = longDate.format(date)

fun formatDateFull(date: LocalDate): String = fullDate.format(date).capitalizeFirst()

fun formatMonth(date: LocalDate): String = monthName.format(date)

fun quetzales(amount: Int): String = "Q" + String.format(Locale.US, "%,d", amount)

private fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Spanish) else it.toString() }
