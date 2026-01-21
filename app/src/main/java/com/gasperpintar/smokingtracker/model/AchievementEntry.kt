package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit

data class AchievementEntry(
    override val id: Long,
    val image: Int,
    val value: Int,
    val message: String,
    val times: Long,
    val category: AchievementCategory,
    val unit: AchievementUnit
): Identifiable