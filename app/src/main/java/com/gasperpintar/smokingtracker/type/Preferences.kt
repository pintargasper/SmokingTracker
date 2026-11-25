package com.gasperpintar.smokingtracker.type

object Preferences {

    enum class Name(
        val value: String
    ) {
        SETTINGS(value = "settings");
    }

    enum class Key(
        val value: String
    ) {
        FIRST_RUN(value = "first_run"),
        LAST_PROGRESS_NOTIFICATION_TIME(value = "last_progress_notification_time"),
        LAST_VERSION_NAME(value = "last_version_name");
    }
}