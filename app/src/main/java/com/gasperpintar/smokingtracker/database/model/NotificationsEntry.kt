package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.database.entity.NotificationsEntity

data class NotificationsEntry(
    @get:Override
    override val id: Long,
    val system: Boolean,
    val achievements: Boolean,
    val progress: Boolean
) : IIdentifiable {
    companion object {
        fun default(
            achievements : Boolean = true
        ): NotificationsEntry {
            return NotificationsEntry(
                id = 0L,
                system = true,
                achievements = achievements,
                progress = true
            )
        }

        fun fromEntity(
            entity: NotificationsEntity
        ): NotificationsEntry {
            return NotificationsEntry(
                id = entity.id,
                system = entity.system,
                achievements = entity.achievements,
                progress = entity.progress
            )
        }
    }

    fun toEntity(): NotificationsEntity {
        return NotificationsEntity(
            id = id,
            system = system,
            achievements = achievements,
            progress = progress
        )
    }
}