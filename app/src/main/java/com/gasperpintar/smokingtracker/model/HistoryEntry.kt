package com.gasperpintar.smokingtracker.model

import java.time.LocalDateTime

data class HistoryEntry (
    val id: Long,
    val isLent: Boolean,
    val createdAt: LocalDateTime,
    val timerLabel: String
)
