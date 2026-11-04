package com.gasperpintar.smokingtracker.type

import androidx.appcompat.app.AppCompatDelegate

enum class Theme(
    val themeId: Int
) {
    SYSTEM(themeId = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(themeId = AppCompatDelegate.MODE_NIGHT_NO),
    DARK(themeId = AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        fun fromValue(value: Int):Theme {
            return entries.firstOrNull {
                it.ordinal == value
            } ?: SYSTEM
        }
    }
}