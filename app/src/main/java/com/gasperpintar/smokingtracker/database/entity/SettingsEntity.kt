package com.gasperpintar.smokingtracker.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val theme: Int,
    val language: Int,
    val frequency: Int,

    @ColumnInfo(defaultValue = "€")
    val currency: String,

    @ColumnInfo(defaultValue = "")
    val customCurrency: String
) {
    companion object {
        fun default(language: Int): SettingsEntity {
            return SettingsEntity(
                id = 1,
                theme = 0,
                language = language,
                frequency = 0,
                currency = "€",
                customCurrency = ""
            )
        }
    }
}