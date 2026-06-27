package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.adapter.Adapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.databinding.ActivityNotesBinding
import com.gasperpintar.smokingtracker.model.NoteEntry
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.NoteFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding

    lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var adapter: Adapter<NoteEntry>

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonAddNote.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment_container, NoteFragment())
                .addToBackStack("AddNote")
                .commit()
        }
        setupRecyclerView()
        loadNotes()
    }

    override fun attachBaseContext(
        context: Context
    ) {
        database = Provider.getDatabase(context = context.applicationContext)
        settingsRepository = SettingsRepository(
            settingsDao = database.settingsDao()
        )

        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = settingsRepository
            )
        )
    }

    private fun setupRecyclerView() {
        adapter = Adapter(
            layoutId = R.layout.note_container,
            onBind = { itemView, noteEntry ->
                val title = itemView.findViewById<TextView>(R.id.title_label)
                val content = itemView.findViewById<TextView>(R.id.content_label)
                val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)

                title.text = noteEntry.title
                content.text = noteEntry.content

                itemView.setOnClickListener {
                    val fragment = NoteFragment().apply {
                        arguments = Bundle().apply {
                            putLong("note_id", noteEntry.id)
                        }
                    }
                    (itemView.context as AppCompatActivity)
                        .supportFragmentManager
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .add(R.id.fragment_container, fragment)
                        .addToBackStack("EditNote")
                        .commit()
                }

                deleteButton.setOnClickListener {
                    DialogManager.showDeleteDialog(
                        context = this,
                        onConfirm = {
                            lifecycleScope.launch {
                                database.notesDao().delete(entity = noteEntry.toEntity())
                                loadNotes()
                            }
                        }
                    )
                }
            },
            diffCallback = object : DiffUtil.ItemCallback<NoteEntry>() {
                override fun areItemsTheSame(oldItem: NoteEntry, newItem: NoteEntry) = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: NoteEntry, newItem: NoteEntry) = oldItem == newItem
            }
        )
        binding.recyclerviewNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewNotes.adapter = adapter
    }

    fun loadNotes() {
        lifecycleScope.launch {
            val notes: List<NoteEntity> = database.notesDao().getAll()
            val notesList: List<NoteEntry> = notes.map(transform = NoteEntry::fromEntity)
            adapter.submitList(notesList)
        }
    }
}