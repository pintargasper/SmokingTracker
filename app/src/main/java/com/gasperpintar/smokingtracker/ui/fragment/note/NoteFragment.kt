package com.gasperpintar.smokingtracker.ui.fragment.note

import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.database.viewmodel.NotesViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentNoteBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NoteFragment @JvmOverloads constructor(
    private val initialNoteId: Long = -1L
) : Base<FragmentNoteBinding>(
    bindingInflater = FragmentNoteBinding::inflate
) {

    private val viewModel: NotesViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    private var noteId: Long = -1L
    private var existingNote: NoteEntity? = null
    private var isSaving = false

    @Override
    override fun initialize() = binding.apply {
        noteId = arguments?.getLong(ARG_NOTE_ID, initialNoteId) ?: initialNoteId

        setupListeners()
        setupEmotionSlider()

        val isEditing = noteId != -1L

        titleHeader.setText(if (isEditing) R.string.notes_edit else R.string.notes_add)
        if (isEditing) loadNote()
        else showContent()
    }

    private fun setupListeners() = binding.apply {
        val closeAction = {
            saveNote(close = true)
        }

        requireActivity().onBackPressedDispatcher.addCallback(owner = viewLifecycleOwner) {
            closeAction()
        }

        close.setOnClickListener {
            closeAction()
        }

        save.setOnClickListener {
            saveNote()
        }
    }

    private fun setupEmotionSlider() = binding.apply {
        sliderEmotion.setLabelFormatter { value ->
            when (value.toInt()) {
                1 -> getString(R.string.notes_emotions_very_bad)
                2 -> getString(R.string.notes_emotions_bad)
                3 -> getString(R.string.notes_emotions_neutral)
                4 -> getString(R.string.notes_emotions_good)
                else -> getString(R.string.notes_emotions_very_good)
            }
        }
    }

    private fun loadNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            val note = viewModel.getById(id = noteId)
            if (!isAdded || view == null) return@launch
            existingNote = note

            note?.let {
                binding.apply {
                    inputTitle.setText(it.title)
                    inputContent.setText(it.content)
                    sliderEmotion.value = it.mood.toFloat()
                }
            }
            showContent()
        }
    }

    private fun saveNote(close: Boolean = false) {
        if (isSaving) return
        DialogManager.showSaveNoteDialog(context = requireActivity()) { save, closed ->
            if (!isAdded || view == null) return@showSaveNoteDialog
            if (save) {
                isSaving = true
                val title = binding.inputTitle.text.toString().trim()
                val content = binding.inputContent.text.toString().trim()
                val mood = binding.sliderEmotion.value.toInt()

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val now = LocalDateTime.now()
                        viewModel.save(
                            NoteEntity(
                                id = existingNote?.id ?: 0L,
                                title = title,
                                content = content,
                                mood = mood,
                                createdAt = existingNote?.createdAt ?: now,
                                updatedAt = now
                            )
                        )
                        setFragmentResult(requestKey = REQUEST_NOTE_SAVED, result = Bundle.EMPTY)
                        parentFragmentManager.popBackStack()
                    } finally {
                        isSaving = false
                    }
                }
            }

            if (closed && close) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        const val ARG_NOTE_ID = "note_id"
        const val REQUEST_NOTE_SAVED = "request_note_saved"

        @JvmStatic
        @JvmOverloads
        fun newInstance(noteId: Long = -1L) = NoteFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_NOTE_ID, noteId)
            }
        }
    }
}