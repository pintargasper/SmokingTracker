package com.gasperpintar.smokingtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gasperpintar.smokingtracker.database.converter.Converter
import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
}
