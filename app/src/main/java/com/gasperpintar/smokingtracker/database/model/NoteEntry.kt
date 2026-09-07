package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker.R
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
    val moodIcon: Int
        get() = when (mood) {
            1 -> R.drawable.sentiment_frustrated_48px
            2 -> R.drawable.sentiment_dissatisfied_48px
            3 -> R.drawable.sentiment_neutral_48px
            4 -> R.drawable.sentiment_satisfied_48px
            5 -> R.drawable.sentiment_excited_48px
            else -> R.drawable.sentiment_neutral_48px
        }

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