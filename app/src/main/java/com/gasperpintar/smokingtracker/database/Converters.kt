package com.gasperpintar.smokingtracker.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ProvidedTypeConverter
class Converters {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun fromLocalDateTime(
        date: LocalDateTime
    ): String {
        return date.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(
        dateString: String
    ): LocalDateTime {
        return LocalDateTime.parse(dateString, formatter)
    }

    @TypeConverter
    fun fromAchievementCategory(
        category: AchievementCategory?
    ): String? {
        return category?.name
    }

    @TypeConverter
    fun toAchievementCategory(
        value: String?
    ): AchievementCategory? {
        return value?.let {
            AchievementCategory.valueOf(value = it)
        }
    }

    @TypeConverter
    fun fromAchievementUnit(
        unit: AchievementUnit?
    ): String? {
        return unit?.name
    }

    @TypeConverter
    fun toAchievementUnit(
        value: String?
    ): AchievementUnit? {
        return value?.let {
            AchievementUnit.valueOf(value = it)
        }
    }
}