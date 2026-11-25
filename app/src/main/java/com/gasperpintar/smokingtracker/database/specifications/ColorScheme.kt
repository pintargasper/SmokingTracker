package com.gasperpintar.smokingtracker.database.specifications

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

class ColorScheme : AutoMigrationSpec {

    @Override
    override fun onPostMigrate(
        db: SupportSQLiteDatabase
    ) {
        db.execSQL("UPDATE settings SET colorScheme = 'LEGACY'")
    }
}