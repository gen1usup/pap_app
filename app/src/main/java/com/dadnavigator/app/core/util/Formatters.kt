package com.dadnavigator.app.core.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Formats durations into compact human-readable strings.
 */
fun Duration.toReadableDuration(): String {
    val totalSeconds = this.seconds.coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Formats instant for timeline display.
 */
fun Instant.toReadableDateTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return formatter.format(atZone(zoneId))
}

/**
 * Formats local dates for settings and dashboard.
 */
fun LocalDate.toReadableDate(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return formatter.format(this)
}
