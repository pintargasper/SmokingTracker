package com.gasperpintar.smokingtracker.adapter.history

import com.gasperpintar.smokingtracker.model.HistoryEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class HistoryDiffCallbackTest {

    private lateinit var historyDiffCallback: HistoryDiffCallback

    @Before
    fun setup() {
        historyDiffCallback = HistoryDiffCallback()
    }

    @Test
    fun areItemsTheSamePositive() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 1, timerLabel = "01:00:00")
        assertTrue(historyDiffCallback.areItemsTheSame(oldItem, newItem))
    }

    @Test
    fun areItemsTheSameNegative() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 2, timerLabel = "00:00:00")
        assertFalse(historyDiffCallback.areItemsTheSame(oldItem, newItem))
    }

    @Test
    fun areContentsTheSamePositive() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        assertTrue(historyDiffCallback.areContentsTheSame(oldItem, newItem))
    }

    @Test
    fun areContentsTheSameNegative() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 1, timerLabel = "01:00:00")
        assertFalse(historyDiffCallback.areContentsTheSame(oldItem, newItem))
    }

    private fun createHistoryEntry(id: Long, timerLabel: String): HistoryEntry {
        return HistoryEntry(
            id = id,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 10),
            timerLabel = timerLabel
        )
    }
}