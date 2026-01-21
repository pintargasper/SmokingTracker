package com.gasperpintar.smokingtracker.database.converter

import androidx.room.TypeConverter
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit

class AchievementEnumConverter {

    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toAchievementCategory(value: String?): AchievementCategory? {
        return value?.let { AchievementCategory.valueOf(it) }
    }

    @TypeConverter
    fun fromAchievementUnit(unit: AchievementUnit?): String? {
        return unit?.name
    }

    @TypeConverter
    fun toAchievementUnit(value: String?): AchievementUnit? {
        return value?.let { AchievementUnit.valueOf(it) }
    }
}