package com.gasperpintar.smokingtracker.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gasperpintar.smokingtracker.model.TriggerJson
import com.gasperpintar.smokingtracker.type.Appearance

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
    val colorScheme: Appearance.ColorScheme,

    @ColumnInfo(defaultValue = "HOME")
    val startPage: Appearance.StartPage,

    @ColumnInfo(defaultValue = "NO_REASON")
    val defaultTrigger: TriggerJson.Type
)