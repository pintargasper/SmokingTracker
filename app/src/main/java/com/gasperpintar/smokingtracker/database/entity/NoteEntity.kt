package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notes")
data class NoteEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val content: String,
    val mood: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)