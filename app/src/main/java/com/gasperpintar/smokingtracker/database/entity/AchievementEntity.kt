package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import java.time.LocalDateTime

@Entity(tableName = "achievements")
data class AchievementEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val image: Int,
    val value: Int,
    val message: String,
    val times: Long,
    val lastAchieved: LocalDateTime?,
    val reset: Boolean,
    val notify: Boolean,
    val category: AchievementCategory,
    val unit: AchievementUnit
)