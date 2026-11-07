package com.gasperpintar.smokingtracker.utils

import android.content.Context
import java.time.Duration

object ElapsedTimeSettings {

    const val DEFAULT_WARNING_MINUTES = 60
    const val MIN_WARNING_MINUTES = 1
    const val MAX_WARNING_MINUTES = 10_080

    private const val KEY_WARNING_MINUTES = "elapsed_time_warning_minutes"

    fun getWarningMinutes(context: Context): Int {
        val value = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt(KEY_WARNING_MINUTES, DEFAULT_WARNING_MINUTES)
        return value.takeIf { it in MIN_WARNING_MINUTES..MAX_WARNING_MINUTES }
            ?: DEFAULT_WARNING_MINUTES
    }

    fun setWarningMinutes(context: Context, minutes: Int): Boolean {
        if (minutes !in MIN_WARNING_MINUTES..MAX_WARNING_MINUTES) return false
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_WARNING_MINUTES, minutes)
            .apply()
        return true
    }

    fun isWarningElapsed(duration: Duration, warningMinutes: Int): Boolean {
        return warningMinutes in MIN_WARNING_MINUTES..MAX_WARNING_MINUTES &&
            !duration.isNegative &&
            duration >= Duration.ofMinutes(warningMinutes.toLong())
    }
}
