package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import java.time.LocalDateTime

data class HistoryEntry(
    override val id: Long,
    val isLent: Boolean,
    val createdAt: LocalDateTime,
    val timerLabel: String
): Identifiable {
    companion object {
        fun fromEntity(
            entity: HistoryEntity
        ): HistoryEntry {
            return HistoryEntry(
                id = entity.id,
                isLent = entity.lent > 0,
                createdAt = entity.createdAt,
                timerLabel = LocalizationHelper.formatTime(time = entity.createdAt.toLocalTime())
            )
        }
    }

    fun toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            createdAt = createdAt,
            lent = if (isLent) 1 else 0
        )
    }
}