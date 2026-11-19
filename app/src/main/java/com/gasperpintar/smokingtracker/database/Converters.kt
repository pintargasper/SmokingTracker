package com.gasperpintar.smokingtracker.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.gasperpintar.smokingtracker.type.Achievement
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
        category: Achievement.Category?
    ): String? {
        return category?.name
    }

    @TypeConverter
    fun toAchievementCategory(
        value: String?
    ): Achievement.Category? {
        return value?.let {
            Achievement.Category.valueOf(value = it)
        }
    }

    @TypeConverter
    fun fromAchievementUnit(
        unit: Achievement.Unit?
    ): String? {
        return unit?.name
    }

    @TypeConverter
    fun toAchievementUnit(
        value: String?
    ): Achievement.Unit? {
        return value?.let {
            Achievement.Unit.valueOf(value = it)
        }
    }
}