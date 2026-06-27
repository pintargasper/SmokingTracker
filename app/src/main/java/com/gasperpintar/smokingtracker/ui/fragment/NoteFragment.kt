package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.NotesActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.databinding.FragmentNoteBinding
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding: FragmentNoteBinding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var notesRepository: NotesRepository

    private var noteId: Long = -1L
    private var existingNote: NoteEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        database = (requireActivity() as NotesActivity).database
        notesRepository = NotesRepository(notesDao = database.notesDao())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteId = arguments?.getLong("note_id") ?: -1L

        view.setOnTouchListener { view, _ ->
            view.performClick()
            true
        }

        binding.buttonClose.setOnClickListener {
            saveNote(close = true)
        }

        binding.buttonSave.setOnClickListener {
            saveNote()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            saveNote(close = true)
        }

        if (noteId != -1L) {
            lifecycleScope.launch {
                existingNote = database.notesDao().getById(noteId)
                existingNote?.let { note ->
                    binding.inputTitle.setText(note.title)
                    binding.inputContent.setText(note.content)
                    binding.sliderEmotion.value = note.mood.toFloat()
                }
            }
        }

        binding.sliderEmotion.setLabelFormatter { value ->
            when (value.toInt()) {
                1 -> getString(R.string.analytics_notes_add_content_emotions_very_bad)
                2 -> getString(R.string.analytics_notes_add_content_emotions_bad)
                3 -> getString(R.string.analytics_notes_add_content_emotions_neutral)
                4 -> getString(R.string.analytics_notes_add_content_emotions_good)
                5 -> getString(R.string.analytics_notes_add_content_emotions_very_good)
                else -> ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveNote(close: Boolean = false) {
        DialogManager.showSaveNoteDialog(
            context = requireActivity(),
            onSave = {
                lifecycleScope.launch {
                    val note = NoteEntity(
                        id = if (noteId == -1L) 0 else noteId,
                        title = binding.inputTitle.text.toString().trim(),
                        content = binding.inputContent.text.toString().trim(),
                        mood = binding.sliderEmotion.value.toInt(),
                        createdAt = existingNote?.createdAt ?: LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    notesRepository.upsert(entry = note)
                    (requireActivity() as NotesActivity).loadNotes()
                    parentFragmentManager.popBackStack()
                }
            },
            onClose = {
                if (close) {
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }
}