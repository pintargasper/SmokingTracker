package com.gasperpintar.smokingtracker.database

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromLocalDateTimeUsesDatabaseFormat() {
        assertEquals(
            "2026-01-05 07:08:09",
            converters.fromLocalDateTime(date = LocalDateTime.of(2026, 1, 5, 7, 8, 9))
        )
    }

    @Test
    fun localDateTimeRoundTripDropsSubSecondPrecision() {
        val date = LocalDateTime.of(2026, 1, 5, 7, 8, 9, 123_456_789)
        val actual = converters.toLocalDateTime(dateString = converters.fromLocalDateTime(date = date))

        assertEquals(date.withNano(0), actual)
    }

    @Test
    fun formattedDatesSortInChronologicalOrder() {
        val formatted = listOf(
            LocalDateTime.of(2025, 12, 31, 23, 59, 59),
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 1, 9, 0),
            LocalDateTime.of(2026, 1, 1, 10, 0),
            LocalDateTime.of(2026, 10, 1, 0, 0)
        ).map { converters.fromLocalDateTime(date = it) }

        assertEquals(formatted, formatted.sorted())
    }
}