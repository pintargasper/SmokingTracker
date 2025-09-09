package com.gasperpintar.smokingtracker.database

import android.content.Context
import androidx.room.Room
import kotlin.jvm.java

object Provider {

    private var databaseInstance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "smoking_tracker_db"
                    ).build()
            databaseInstance = instance
            instance
        }
    }
}