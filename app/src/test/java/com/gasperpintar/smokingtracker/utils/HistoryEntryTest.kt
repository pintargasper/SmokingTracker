package com.gasperpintar.smokingtracker.utils

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class HistoryEntryTest {

    @Test
    fun historyEntityToHistoryEntryConversionIsCorrect() {
        val historyEntity = HistoryEntity(
            id = 1L,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            lent = 1
        )

        val expectedHistoryEntry = HistoryEntry(
            id = 1L,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            timerLabel = "10:30:00"
        )

        val result: HistoryEntry = HistoryEntry.fromEntity(entity = historyEntity)
        assertEquals(expectedHistoryEntry, result)
    }

    @Test
    fun historyEntryToHistoryEntityConversionIsCorrect() {
        val historyEntry = HistoryEntry(
            id = 1L,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            timerLabel = "10:30:00"
        )

        val expectedHistoryEntity = HistoryEntity(
            id = 1L,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            lent = 1
        )

        val result: HistoryEntity = historyEntry.toEntity()
        assertEquals(expectedHistoryEntity, result)
    }
}