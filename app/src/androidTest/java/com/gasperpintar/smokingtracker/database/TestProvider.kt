package com.gasperpintar.smokingtracker.database

import android.content.Context
import androidx.room.Room
import com.gasperpintar.smokingtracker.database.specifications.Migrations

object TestProvider {

    private var database: AppDatabase? = null

    fun getInMemoryDatabase(context: Context): AppDatabase {
        return database ?: synchronized(lock = this) {
            Room.inMemoryDatabaseBuilder(
                context = context.applicationContext,
                klass = AppDatabase::class.java
            ).addMigrations(*Migrations.migrationList)
                .addTypeConverter(Converters())
                .build()
                .also { database = it }
        }
    }

    fun closeDatabase() {
        database?.close()
        database = null
    }
}
