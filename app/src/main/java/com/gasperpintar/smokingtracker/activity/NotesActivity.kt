package com.gasperpintar.smokingtracker.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.database.viewmodel.NotesViewModel
import com.gasperpintar.smokingtracker.databinding.ActivityNotesBinding
import com.gasperpintar.smokingtracker.databinding.ContainerContentNoteBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.NoteFragment
import kotlinx.coroutines.launch

class NotesActivity : Base<ActivityNotesBinding>(
    bindingInflater = ActivityNotesBinding::inflate
) {

    private val appContainer by lazy { (application as Application).container }
    private val viewModel: NotesViewModel by viewModels {
        ModelFactory(container = appContainer)
    }

    private lateinit var adapter: Adapter<NoteEntry, ContainerContentNoteBinding>

    @Override
    override fun initialize() = binding.apply {
        buttonAddNote.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment_container, NoteFragment())
                .addToBackStack("AddNote")
                .commit()
        }

        buttonBack.setOnClickListener {
            finish()
        }
        setupAdapter()

        lifecycleScope.launch {
            loadNotes()
        }
    }

    private fun setupAdapter() = binding.apply {
        adapter = ContainerManager.createNoteAdapter(
            context = this@NotesActivity,
            onOpen = { entry -> openNote(noteId = entry.id) },
            onDelete = { entry ->
                DialogManager.showDeleteDialog(context = this@NotesActivity) {
                    lifecycleScope.launch {
                        viewModel.delete(note = entry)
                        loadNotes()
                    }
                }
            }
        )
        recyclerviewNotes.layoutManager = LinearLayoutManager(this@NotesActivity)
        recyclerviewNotes.adapter = adapter
    }

    suspend fun loadNotes() = binding.apply {
        val state = viewModel.getState()
        state.notes.let {
            recyclerviewNotes.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            layoutEmptyNotes.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE

            adapter.submitList(state.notes) {
                recyclerviewNotes.scrollToPosition(0)
            }
        }
    }

    private fun openNote(noteId: Long) {
        val fragment = NoteFragment().apply {
            arguments = Bundle().apply {
                putLong("note_id", noteId)
            }
        }

        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .add(R.id.fragment_container, fragment)
            .addToBackStack("EditNote")
            .commit()
    }
}