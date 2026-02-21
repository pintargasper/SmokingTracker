package com.gasperpintar.smokingtracker.database.specifications

import androidx.room.migration.Migration

object Migrations {

    val migrationList = arrayOf(
        Migration(startVersion = 2, endVersion = 3) { database ->
            database.execSQL("ALTER TABLE `settings` ADD COLUMN `frequency` INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE `notifications_settings` ADD COLUMN `progress` INTEGER NOT NULL DEFAULT 1")
        },
        Migration(startVersion = 3, endVersion = 4) { _ -> }
    )
}