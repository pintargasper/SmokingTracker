package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

data class SettingsEntry (
    override val id: Long,
    val theme: Int,
    val language: String,
    val frequency: Int,
    val currency: String,
    val customCurrency: String,
    val dayEndMinutes: Int
): Identifiable {
    companion object {
        fun fromEntity(
            entity: SettingsEntity
        ): SettingsEntry {
            return SettingsEntry(
                id = entity.id,
                theme = entity.theme,
                language = entity.language,
                frequency = entity.frequency,
                currency = entity.currency,
                customCurrency = entity.customCurrency,
                dayEndMinutes = entity.dayEndMinutes
            )
        }
    }
}