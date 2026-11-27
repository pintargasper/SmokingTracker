package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gasperpintar.smokingtracker.database.entity.NoteEntity

@Dao
interface NotesDao: Base<NoteEntity> {

    @Query(value = """
        SELECT * 
        FROM notes 
        ORDER BY createdAt DESC, id DESC
    """)
    suspend fun all(): List<NoteEntity>

    @Query(value = """
        SELECT * 
        FROM notes 
        WHERE id = :id
    """)
    suspend fun byId(
        id: Long
    ): NoteEntity?

    @Transaction
    suspend fun deleteAll() {
        delete()
        resetAutoIncrement()
    }

    @Query("DELETE FROM notes")
    suspend fun delete()

    @Query("""
        DELETE FROM sqlite_sequence
        WHERE name = 'notes'
    """)
    suspend fun resetAutoIncrement()
}