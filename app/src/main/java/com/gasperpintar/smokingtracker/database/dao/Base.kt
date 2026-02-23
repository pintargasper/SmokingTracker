package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Upsert

interface Base<T> {

    @Upsert
    suspend fun upsertAll(entities: List<T>)

    @Insert
    suspend fun insert(entity: T)

    @Insert
    suspend fun insertAll(entities: List<T>)

    @Update
    suspend fun update(entity: T)

    @Delete
    suspend fun delete(entity: T)
}
