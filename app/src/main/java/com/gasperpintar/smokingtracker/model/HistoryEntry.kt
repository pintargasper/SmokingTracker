package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import java.time.LocalDateTime

data class HistoryEntry (
    override val id: Long,
    val isLent: Boolean,
    val createdAt: LocalDateTime,
    val timerLabel: String
): Identifiable
