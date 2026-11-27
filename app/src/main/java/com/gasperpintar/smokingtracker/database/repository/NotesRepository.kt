package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.NotesDao
import com.gasperpintar.smokingtracker.database.model.NoteEntry

class NotesRepository(
    private val notesDao: NotesDao
) {

    suspend fun getAll(): List<NoteEntry> {
        return notesDao.all().map(transform = NoteEntry::fromEntity)
    }

    suspend fun getById(
        id: Long
    ): NoteEntry? {
        return notesDao.byId(id = id)?.let(block = NoteEntry::fromEntity)
    }

    suspend fun insert(
        entries: List<NoteEntry>
    ) {
        notesDao.insertAll(entities = entries.map(transform = NoteEntry::toEntity))
    }

    suspend fun upsert(
        entry: NoteEntry
    ) {
        notesDao.upsert(entity = entry.toEntity())
    }

    suspend fun delete(entry: NoteEntry? = null) {
        entry?.let {
            notesDao.delete(entity = it.toEntity())
        } ?: notesDao.deleteAll()
    }
}