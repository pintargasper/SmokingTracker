package com.gasperpintar.smokingtracker.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gasperpintar.smokingtracker.database.converter.AchievementEnumConverter
import com.gasperpintar.smokingtracker.database.converter.LocalDateTimeConverter
import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

@Database(
    entities = [
        AchievementEntity::class,
        HistoryEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
@TypeConverters(value = [
    LocalDateTimeConverter::class,
    AchievementEnumConverter::class
])
abstract class AppDatabase : RoomDatabase() {

    abstract fun achievementDao(): AchievementDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao
}
