package com.gasperpintar.smokingtracker.database.specifications

import androidx.room.migration.Migration

object Migrations {

    val migrationList = arrayOf(
        Migration(startVersion = 2, endVersion = 3) { database ->
            database.execSQL("ALTER TABLE `settings` ADD COLUMN `frequency` INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE `notifications_settings` ADD COLUMN `progress` INTEGER NOT NULL DEFAULT 1")
        },
        Migration(startVersion = 3, endVersion = 4) { _ -> },
        Migration(startVersion = 4, endVersion = 5) { database ->
            database.execSQL("""
                CREATE TABLE achievements_new (
                    id INTEGER PRIMARY KEY NOT NULL,
                    image TEXT NOT NULL,
                    value INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    times INTEGER NOT NULL,
                    lastAchieved TEXT,
                    reset INTEGER NOT NULL,
                    notify INTEGER NOT NULL,
                    category TEXT NOT NULL,
                    unit TEXT NOT NULL
                )
            """)
            database.execSQL("""
                INSERT INTO achievements_new (id, image, value, title, message, times, lastAchieved, reset, notify, category, unit)
                SELECT id, CAST(image AS TEXT), value, CAST(title AS TEXT), CAST(message AS TEXT), times, lastAchieved, reset, notify, category, unit FROM achievements
            """)
            database.execSQL("DROP TABLE achievements")
            database.execSQL("ALTER TABLE achievements_new RENAME TO achievements")
        }
    )
}