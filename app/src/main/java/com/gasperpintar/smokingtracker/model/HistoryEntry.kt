package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class HistoryEntry (
    override val id: Long,
    val isLent: Boolean,
    val createdAt: LocalDateTime,
    val timerLabel: String
): Identifiable {

    fun toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            createdAt = createdAt,
            lent = if (isLent) {
                1
            } else {
                0
            }
        )
    }

    companion object {

        fun fromEntity(entity: HistoryEntity): HistoryEntry {
            return HistoryEntry(
                id = entity.id,
                isLent = entity.lent > 0,
                createdAt = entity.createdAt,
                timerLabel = entity.createdAt.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
        }
    }
}
