package com.gasperpintar.smokingtracker.database.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeConverter {

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
}
