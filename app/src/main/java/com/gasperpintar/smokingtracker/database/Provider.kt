package com.gasperpintar.smokingtracker.database

import android.content.Context
import androidx.room.Room
import com.gasperpintar.smokingtracker.database.specifications.Migrations
import kotlin.jvm.java

object Provider {

    @Volatile
    private var databaseInstance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(lock = this) {
            val instance = Room.databaseBuilder(
                        context = context.applicationContext,
                        klass = AppDatabase::class.java,
                        name = "smoking_tracker"
                    )
                .addTypeConverter(Converters())
                .addMigrations(*Migrations.migrationList)
                .build()
            databaseInstance = instance
            instance
        }
    }
}