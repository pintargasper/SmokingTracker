package com.gasperpintar.smokingtracker.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gasperpintar.smokingtracker.model.TriggerJson
import java.time.LocalDateTime

@Entity(tableName = "history")
data class HistoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val lent: Int,
    val createdAt: LocalDateTime,

    @ColumnInfo(defaultValue = "NO_REASON")
    val trigger: TriggerJson.Type
)