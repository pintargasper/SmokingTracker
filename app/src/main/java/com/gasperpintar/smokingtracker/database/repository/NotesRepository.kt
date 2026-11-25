package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.NotesDao
import com.gasperpintar.smokingtracker.database.model.NoteEntry

class NotesRepository(
    private val notesDao: NotesDao
) {
    suspend fun insert(
        entry: NoteEntry
    ) {
        notesDao.insert(entity = entry.toEntity())
    }

    suspend fun insertAll(
        entries: List<NoteEntry>
    ) {
        notesDao.insertAll(entities = entries.map(transform = NoteEntry::toEntity))
    }

    suspend fun upsert(
        entry: NoteEntry
    ) {
        notesDao.upsert(entity = entry.toEntity())
    }

    suspend fun delete(
        entry: NoteEntry
    ) {
        notesDao.delete(entity = entry.toEntity())
    }

    suspend fun deleteAll() {
        notesDao.deleteAll()
        notesDao.resetAutoIncrement()
    }

    suspend fun getAll(): List<NoteEntry> {
        return notesDao.getAll().map(transform = NoteEntry::fromEntity)
    }

    suspend fun getById(
        id: Long
    ): NoteEntry? {
        return notesDao.getById(id = id)?.let(block = NoteEntry::fromEntity)
    }
}