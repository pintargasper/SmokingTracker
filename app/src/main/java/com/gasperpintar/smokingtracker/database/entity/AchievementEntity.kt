package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gasperpintar.smokingtracker.model.AchievementJson
import java.time.LocalDateTime

@Entity(tableName = "achievements")
data class AchievementEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val image: String,
    val value: Int,
    val title: String,
    val message: String,
    val times: Long,
    val lastAchieved: LocalDateTime?,
    val reset: Boolean,
    val notify: Boolean,
    val category: AchievementJson.Category,
    val unit: AchievementJson.Unit
)