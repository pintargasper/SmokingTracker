package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "history")
data class HistoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val lent: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun default(isLent: Boolean) = HistoryEntity(
            id = 0,
            lent = if (isLent) 1 else 0,
            createdAt = LocalDateTime.now()
        )
    }
}