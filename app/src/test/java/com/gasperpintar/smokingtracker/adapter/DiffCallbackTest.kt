package com.gasperpintar.smokingtracker.adapter

import com.gasperpintar.smokingtracker.model.HistoryEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class DiffCallbackTest {

    private lateinit var diffCallback: DiffCallback<HistoryEntry>

    @Before
    fun setup() {
        diffCallback = DiffCallback()
    }

    @Test
    fun areItemsTheSameReturnsTrueWhenIdsAreEqual() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 1, timerLabel = "01:00:00")
        assertTrue(diffCallback.areItemsTheSame(oldItem, newItem))
    }

    @Test
    fun areItemsTheSameReturnsFalseWhenIdsAreDifferent() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 2, timerLabel = "00:00:00")
        assertFalse(diffCallback.areItemsTheSame(oldItem, newItem))
    }

    @Test
    fun areContentsTheSameReturnsTrueWhenObjectsAreEqual() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        assertTrue(diffCallback.areContentsTheSame(oldItem, newItem))
    }

    @Test
    fun areContentsTheSameReturnsFalseWhenObjectsDiffer() {
        val oldItem = createHistoryEntry(id = 1, timerLabel = "00:00:00")
        val newItem = createHistoryEntry(id = 1, timerLabel = "01:00:00")
        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem))
    }

    private fun createHistoryEntry(
        id: Long,
        timerLabel: String
    ): HistoryEntry {
        return HistoryEntry(
            id = id,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 10),
            timerLabel = timerLabel
        )
    }
}