package com.gasperpintar.smokingtracker.type

import androidx.appcompat.app.AppCompatDelegate
import com.gasperpintar.smokingtracker.R

object Appearance {

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

    enum class ColorScheme(
        val themeResourceId: Int
    ) {
        SYSTEM(themeResourceId = R.style.Theme_SmokingTracker_System),
        LEGACY(themeResourceId = R.style.Theme_SmokingTracker_Legacy),
        SKY(themeResourceId = R.style.Theme_SmokingTracker_Sky);
    }

    enum class StartPage {
        HOME,
        ADVANCED,
        GRAPH;
    }
}