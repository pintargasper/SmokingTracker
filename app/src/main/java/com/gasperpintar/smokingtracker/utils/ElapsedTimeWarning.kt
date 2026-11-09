package com.gasperpintar.smokingtracker.utils

import java.time.Duration

object ElapsedTimeWarning {

    const val DEFAULT_MINUTES = 60
    const val MIN_MINUTES = 0
    const val MAX_MINUTES = 10_080

    fun isValidMinutes(minutes: Int): Boolean {
        return minutes in MIN_MINUTES..MAX_MINUTES
    }

    fun shouldHighlight(duration: Duration, warningMinutes: Int): Boolean {
        return warningMinutes > MIN_MINUTES &&
            isValidMinutes(warningMinutes) &&
            !duration.isNegative &&
            duration < Duration.ofMinutes(warningMinutes.toLong())
    }
}
