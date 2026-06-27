package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import java.time.LocalDateTime

data class NoteEntry(
    override val id: Long,
    val title: String,
    val content: String,
    val mood: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
): Identifiable {

    companion object {

        fun fromEntity(
            entity: NoteEntity
        ): NoteEntry {
            return NoteEntry(
                id = entity.id,
                title = entity.title,
                content = entity.content,
                mood = entity.mood,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    fun toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            mood = mood,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
