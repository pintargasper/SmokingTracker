package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.NoteEntity

@Dao
interface NotesDao: Base<NoteEntity> {

    @Query(value = "DELETE FROM notes")
    suspend fun deleteAll()

    @Query(value = "DELETE FROM sqlite_sequence WHERE name = 'notes'")
    suspend fun resetAutoIncrement()

    @Query(value = "SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAll(): List<NoteEntity>

    @Query(value = "SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity
}