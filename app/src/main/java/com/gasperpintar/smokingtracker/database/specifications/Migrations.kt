package com.gasperpintar.smokingtracker.database.specifications

import androidx.room.migration.Migration

object Migrations {

    val migrationList = arrayOf(
        Migration(startVersion = 2, endVersion = 3) { database ->
            database.execSQL("ALTER TABLE `settings` ADD COLUMN `frequency` INTEGER NOT NULL DEFAULT 0")
            if (!database.query("PRAGMA table_info(notifications_settings)")
                .use { cursor -> (0 until cursor.count).any {
                    cursor.moveToPosition(it) && cursor.getString(1) == "progress" }
                }) {
                database.execSQL("ALTER TABLE notifications_settings ADD COLUMN progress INTEGER NOT NULL DEFAULT 1")
            }
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
        },
        Migration(startVersion = 6, endVersion = 7) { database ->
            database.execSQL("""
                CREATE TABLE settings_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    theme INTEGER NOT NULL,
                    language TEXT NOT NULL,
                    frequency INTEGER NOT NULL,
                    currency TEXT NOT NULL DEFAULT '€',
                    customCurrency TEXT NOT NULL DEFAULT ''
                )
            """)
            database.execSQL("""
                INSERT INTO settings_new (id, theme, language, frequency, currency, customCurrency)
                SELECT id, theme, CASE language WHEN 0 THEN 'system' WHEN 1 THEN 'en' WHEN 2 THEN 'sl' WHEN 3 THEN 'uk'
                        WHEN 4 THEN 'de' WHEN 5 THEN 'fr' WHEN 6 THEN 'sr' WHEN 7 THEN 'sr-Latn' WHEN 8 THEN 'zh-Hans' ELSE 'system' END,
                    frequency, currency, customCurrency FROM settings
            """)
            database.execSQL("DROP TABLE settings")
            database.execSQL("ALTER TABLE settings_new RENAME TO settings")
        }
    )
}