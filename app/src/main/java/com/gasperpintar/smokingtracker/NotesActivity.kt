package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.database.viewmodel.NotesViewModel
import com.gasperpintar.smokingtracker.databinding.ActivityNotesBinding
import com.gasperpintar.smokingtracker.databinding.NoteContainerBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.NoteFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding

    private val appContainer by lazy { (application as Application).container }
    private val viewModel: NotesViewModel by viewModels {
        ModelFactory(application = application as Application, container = appContainer)
    }

    private lateinit var adapter: Adapter<NoteEntry, NoteContainerBinding>

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)

        initialize()

        setContentView(binding.root)
    }

    @Override
    override fun attachBaseContext(
        context: Context
    ) {
        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = (context.applicationContext as Application).container.settingsRepository
            )
        )
    }

    private fun initialize() = binding.apply {
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
        loadNotes()
    }

    private fun setupAdapter() = binding.apply {
        adapter = Adapter(
            bindingFactory = NoteContainerBinding::inflate,
            onBind = { noteEntry ->
                emotionIcon.setImageResource(noteEntry.moodIcon)
                titleLabel.text = noteEntry.title
                contentLabel.text = noteEntry.content

                root.setOnClickListener {
                    openNote(noteId = noteEntry.id)
                }

                delete.setOnClickListener {
                    DialogManager.showDeleteDialog(
                        context = this@NotesActivity,
                        onConfirm = {
                            lifecycleScope.launch {
                                viewModel.delete(noteEntry)
                                loadNotes()
                            }
                        }
                    )
                }
            }
        )
        recyclerviewNotes.layoutManager = LinearLayoutManager(this@NotesActivity)
        recyclerviewNotes.adapter = adapter
    }

    fun loadNotes() = binding.apply {
        lifecycleScope.launch {
            adapter.submitList(viewModel.getNotes().notes) {
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