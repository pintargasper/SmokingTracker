package com.gasperpintar.smokingtracker.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val customCurrency: String
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
                customCurrency = ""
            )
        }
    }
}