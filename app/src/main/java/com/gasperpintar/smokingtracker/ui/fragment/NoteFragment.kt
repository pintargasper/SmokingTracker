package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.NotesActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.database.viewmodel.NotesViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentNoteBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding: FragmentNoteBinding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    private var noteId: Long = -1L
    private var existingNote: NoteEntity? = null

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() = binding.apply {
        noteId = arguments?.getLong("note_id") ?: -1L

        val closeAction = { saveNote(close = true) }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { closeAction() }

        close.setOnClickListener { closeAction() }
        save.setOnClickListener { saveNote() }

        val isEditing = noteId != -1L
        titleHeader.setText(if (isEditing) R.string.notes_edit else R.string.notes_add)

        if (isEditing) loadNote()
        sliderEmotion.setLabelFormatter { value ->
            when (value.toInt()) {
                1 -> getString(R.string.notes_emotions_very_bad)
                2 -> getString(R.string.notes_emotions_bad)
                3 -> getString(R.string.notes_emotions_neutral)
                4 -> getString(R.string.notes_emotions_good)
                5 -> getString(R.string.notes_emotions_very_good)
                else -> ""
            }
        }
    }

    private fun loadNote() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            existingNote = viewModel.getById(id = noteId)?.also { note ->
                inputTitle.setText(note.title)
                inputContent.setText(note.content)
                sliderEmotion.value = note.mood.toFloat()
            }
        }
    }

    private fun saveNote(close: Boolean = false) = binding.apply {
        DialogManager.showSaveNoteDialog(
            context = requireActivity(),
            onSave = {
                val title = inputTitle.text.toString().trim()
                val content = inputContent.text.toString().trim()
                val mood = sliderEmotion.value.toInt()

                viewLifecycleOwner.lifecycleScope.launch {
                    val now = LocalDateTime.now()
                    viewModel.save(NoteEntity(
                        id = existingNote?.id ?: 0L,
                        title = title,
                        content = content,
                        mood = mood,
                        createdAt = existingNote?.createdAt ?: now,
                        updatedAt = now
                    ))
                    (requireActivity() as? NotesActivity)?.loadNotes()
                    parentFragmentManager.popBackStack()
                }
            },
            onClose = { if (close) parentFragmentManager.popBackStack() }
        )
    }
}