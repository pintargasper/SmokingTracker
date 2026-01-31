package com.gasperpintar.smokingtracker.type

enum class AchievementUnit {
    YEARS,
    MONTHS,
    WEEKS,
    HOURS,
    DAYS,
    CIGARETTES;

    fun toSeconds(
        value: Int
    ): Long? = when (this) {
        HOURS -> value * 3600L
        DAYS -> value * 86400L
        WEEKS -> value * 604800L
        MONTHS -> value * 2592000L
        YEARS -> value * 31536000L
        CIGARETTES -> null
    }
}