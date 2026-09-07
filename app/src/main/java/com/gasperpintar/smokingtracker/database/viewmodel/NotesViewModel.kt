package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.NoteState
import com.gasperpintar.smokingtracker.repository.NotesRepository

class NotesViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    suspend fun getNotes(): NoteState {
        return NoteState(notes = notesRepository.getAll().map(transform = NoteEntry::fromEntity))
    }

    suspend fun getById(id: Long): NoteEntity? {
        return notesRepository.getById(id = id)
    }

    suspend fun save(note: NoteEntity) {
        notesRepository.upsert(entry = note)
    }

    suspend fun delete(note: NoteEntry) {
        notesRepository.delete(entry = note.toEntity())
    }
}