package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class ColorScheme(
    val themeResId: Int
) {
    SYSTEM(themeResId = R.style.Theme_SmokingTracker_System),
    LEGACY(themeResId = R.style.Theme_SmokingTracker_Legacy),
    SKY(themeResId = R.style.Theme_SmokingTracker_Sky);

    companion object {
        fun fromName(name: String?): ColorScheme {
            return entries.firstOrNull {
                it.name.equals(other = name, ignoreCase = true)
            } ?: SYSTEM
        }
    }
}