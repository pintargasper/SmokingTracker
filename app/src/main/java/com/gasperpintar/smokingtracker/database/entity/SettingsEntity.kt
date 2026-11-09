package com.gasperpintar.smokingtracker.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gasperpintar.smokingtracker.type.ColorScheme
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning

@Entity(tableName = "settings")
data class SettingsEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val theme: Int,
    val language: String,
    val frequency: Int,

    @ColumnInfo(defaultValue = "€")
    val currency: String,

    @ColumnInfo(defaultValue = "")
    val customCurrency: String,

    @ColumnInfo(defaultValue = "0")
    val dayEndMinutes: Int,

    @ColumnInfo(defaultValue = "60")
    val elapsedTimeWarningMinutes: Int,

    @ColumnInfo(defaultValue = "SYSTEM")
    val colorScheme: String
) {

    companion object {
        fun default(
            language: String = "system",
            currency: String = "€"
        ): SettingsEntity {
            return SettingsEntity(
                id = 0L,
                theme = 0,
                language = language,
                frequency = 0,
                currency = currency,
                customCurrency = "",
                dayEndMinutes = 0,
                colorScheme = ColorScheme.SYSTEM.name,
                elapsedTimeWarningMinutes = ElapsedTimeWarning.DEFAULT_MINUTES
            )
        }
    }
}