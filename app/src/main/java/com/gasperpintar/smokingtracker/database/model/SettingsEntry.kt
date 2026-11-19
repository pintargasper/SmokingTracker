package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.type.Appearance
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning

data class SettingsEntry(
    @get:Override
    override val id: Long,
    val theme: Int,
    val language: String,
    val frequency: Int,
    val currency: String,
    val customCurrency: String,
    val dayEndMinutes: Int,
    val elapsedTimeWarningMinutes: Int,
    val colorScheme: Appearance.ColorScheme,
    val startPage: Appearance.StartPage
) : IIdentifiable {
    companion object {
        fun default(
            language: String = "system",
            currency: String = "€"
        ): SettingsEntry {
            return SettingsEntry(
                id = 0L,
                theme = 0,
                language = language,
                frequency = 0,
                currency = currency,
                customCurrency = "",
                dayEndMinutes = 0,
                elapsedTimeWarningMinutes = ElapsedTimeWarning.DEFAULT_MINUTES,
                colorScheme = Appearance.ColorScheme.SYSTEM,
                startPage = Appearance.StartPage.HOME
            )
        }

        fun fromEntity(entity: SettingsEntity): SettingsEntry {
            return SettingsEntry(
                id = entity.id,
                theme = entity.theme,
                language = entity.language,
                frequency = entity.frequency,
                currency = entity.currency,
                customCurrency = entity.customCurrency,
                dayEndMinutes = entity.dayEndMinutes,
                elapsedTimeWarningMinutes = entity.elapsedTimeWarningMinutes,
                colorScheme = entity.colorScheme,
                startPage = entity.startPage
            )
        }
    }

    fun toEntity(): SettingsEntity {
        return SettingsEntity(
            id = id,
            theme = theme,
            language = language,
            frequency = frequency,
            currency = currency,
            customCurrency = customCurrency,
            dayEndMinutes = dayEndMinutes,
            elapsedTimeWarningMinutes = elapsedTimeWarningMinutes,
            colorScheme = colorScheme,
            startPage = startPage
        )
    }
}