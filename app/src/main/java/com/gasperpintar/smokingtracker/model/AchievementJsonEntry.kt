package com.gasperpintar.smokingtracker.model

data class AchievementJsonEntry(
        val id: Long,
        val icon: String,
        val value: Int,
        val description: String,
        val unit: String
)