package com.gasperpintar.smokingtracker.utils

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.util.Locale

class HistoryEntryTest {

    private val createdAt = LocalDateTime.of(2025, 12, 31, 10, 30)

    @Test
    fun historyEntityToHistoryEntryConversionIsCorrect() {
        withLocale {
            val entity = HistoryEntity(id = 1L, createdAt = createdAt, lent = 1)
            val expected = HistoryEntry(id = 1L, isLent = true, createdAt = createdAt, timerLabel = "10:30\u202fAM")

            assertEquals(expected, HistoryEntry.fromEntity(entity))
        }
    }

    @Test
    fun historyEntryToHistoryEntityConversionIsCorrect() {
        val entry = HistoryEntry(id = 1L, isLent = true, createdAt = createdAt, timerLabel = "10:30:00")
        val expected = HistoryEntity(id = 1L, createdAt = createdAt, lent = 1)

        assertEquals(expected, entry.toEntity())
    }

    private fun withLocale(block: () -> Unit) {
        try {
            Locale.setDefault(Locale.US)
            block()
        } finally {
            Locale.setDefault(Locale.getDefault())
        }
    }
}