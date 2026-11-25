package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.model.TriggerJson
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import java.time.LocalDateTime

data class HistoryEntry(
    @get:Override
    override val id: Long,
    val isLent: Boolean,
    val createdAt: LocalDateTime,
    val timerLabel: String = "",
    val trigger: TriggerJson.Type,
): IIdentifiable {
    companion object {
        fun default(
            isLent: Boolean,
            trigger: TriggerJson.Type
        ): HistoryEntry {
            return HistoryEntry(id = 0L, isLent = isLent, createdAt = LocalDateTime.now(), trigger = trigger)
        }

        fun fromEntity(
            entity: HistoryEntity
        ): HistoryEntry {
            return HistoryEntry(
                id = entity.id,
                isLent = entity.lent > 0,
                createdAt = entity.createdAt,
                timerLabel = LocalizationHelper.formatTime(time = entity.createdAt.toLocalTime()),
                trigger = entity.trigger
            )
        }
    }

    fun toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            createdAt = createdAt,
            lent = if (isLent) 1 else 0,
            trigger = trigger
        )
    }
}