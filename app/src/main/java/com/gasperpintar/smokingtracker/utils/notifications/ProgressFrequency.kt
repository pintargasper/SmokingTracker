package com.gasperpintar.smokingtracker.utils.notifications

enum class ProgressFrequency(val value: Int) {

    HOURLY(value = 0),
    DAILY(value = 1),
    WEEKLY(value = 2);

    companion object {

        fun fromValue(value: Int?): ProgressFrequency {
            return entries.find { entry: ProgressFrequency ->
                entry.value == value
            } ?: HOURLY
        }
    }
}