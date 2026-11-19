package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.NoteState
import com.gasperpintar.smokingtracker.database.repository.NotesRepository

class NotesViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    suspend fun getState(): NoteState {
        return NoteState(notes = notesRepository.getAll())
    }

    suspend fun getById(
        id: Long
    ): NoteEntry? {
        return notesRepository.getById(id = id)
    }

    suspend fun save(
        note: NoteEntry
    ) {
        notesRepository.upsert(entry = note)
    }

    suspend fun delete(
        note: NoteEntry
    ) {
        notesRepository.delete(entry = note)
    }
}