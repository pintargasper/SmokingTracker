package com.gasperpintar.smokingtracker.database

import android.content.Context
import androidx.room.Room
import com.gasperpintar.smokingtracker.database.specifications.Migrations

object TestProvider {

    private var databaseInstance: AppDatabase? = null

    fun getInMemoryDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(lock = this) {
            val instance = Room.inMemoryDatabaseBuilder(
                context = context.applicationContext,
                klass = AppDatabase::class.java
            ).addMigrations(*Migrations.migrationList)
                .addTypeConverter(Converters())
                .build()
            databaseInstance = instance
            instance
        }
    }

    fun closeDatabase() {
        databaseInstance?.close()
        databaseInstance = null
    }
}
