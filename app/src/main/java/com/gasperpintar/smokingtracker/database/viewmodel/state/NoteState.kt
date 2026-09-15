package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.NoteEntry

data class NoteState(
    val notes: List<NoteEntry> = emptyList()
)