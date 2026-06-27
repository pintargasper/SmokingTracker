package com.gasperpintar.smokingtracker.repository

import com.gasperpintar.smokingtracker.database.dao.NotesDao
import com.gasperpintar.smokingtracker.database.entity.NoteEntity

class NotesRepository(
    private val notesDao: NotesDao
) {
    suspend fun insert(
        entry: NoteEntity
    ) {
        notesDao.insert(entity = entry)
    }

    suspend fun insertAll(
        entries: List<NoteEntity>
    ) {
        notesDao.insertAll(entities = entries)
    }

    suspend fun upsert(
        entry: NoteEntity
    ) {
        notesDao.upsert(entity = entry)
    }

    suspend fun deleteAll() {
        notesDao.deleteAll()
        notesDao.resetAutoIncrement()
    }

    suspend fun getAll(): List<NoteEntity> {
        return notesDao.getAll()
    }
}
