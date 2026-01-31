package com.gasperpintar.smokingtracker.database.specifications

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn.Entries(
    value = [
        DeleteColumn(
            tableName = "settings",
            columnName = "notifications"
        )
    ]
)
class SettingsDeleteColumn : AutoMigrationSpec
