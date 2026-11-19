package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class ColorScheme(
    val themeResourceId: Int
) {
    SYSTEM(themeResourceId = R.style.Theme_SmokingTracker_System),
    LEGACY(themeResourceId = R.style.Theme_SmokingTracker_Legacy),
    SKY(themeResourceId = R.style.Theme_SmokingTracker_Sky);

    companion object {
        fun fromName(name: String?): ColorScheme {
            return entries.firstOrNull {
                it.name.equals(other = name, ignoreCase = true)
            } ?: SYSTEM
        }
    }
}