package com.gasperpintar.smokingtracker.ui.fragment.note

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.database.viewmodel.NotesViewModel
import com.gasperpintar.smokingtracker.databinding.ContainerContentNoteBinding
import com.gasperpintar.smokingtracker.databinding.FragmentNotesBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch

class NotesFragment : Base<FragmentNotesBinding>(
    bindingInflater = FragmentNotesBinding::inflate
) {

    private val appContainer by lazy {
        (requireActivity().application as Application).container
    }

    private val viewModel: NotesViewModel by viewModels {
        ModelFactory(container = appContainer)
    }

    private lateinit var adapter: Adapter<NoteEntry, ContainerContentNoteBinding>

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        updateFragmentContainerVisibility()
    }

    @Override
    override fun initialize() = binding.apply {
        buttonAddNote.setOnClickListener {
            openNote()
        }

        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        childFragmentManager.addOnBackStackChangedListener(backStackListener)
        updateFragmentContainerVisibility()

        setupAdapter()
        setupResultListener()

        viewLifecycleOwner.lifecycleScope.launch {
            loadNotes()
            showContent()
        }
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        childFragmentManager.removeOnBackStackChangedListener(backStackListener)
    }

    private fun setupAdapter() = binding.apply {
        adapter = ContainerManager.createNoteAdapter(
            context = requireActivity(),
            onOpen = { entry ->
                openNote(noteId = entry.id)
            },
            onDelete = { entry ->
                DialogManager.showDeleteDialog(context = requireActivity()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete(note = entry)
                        loadNotes()
                    }
                }
            }
        )
        recyclerviewNotes.layoutManager = LinearLayoutManager(requireContext())
        recyclerviewNotes.adapter = adapter
    }

    private fun setupResultListener() {
        childFragmentManager.setFragmentResultListener(
            NoteFragment.REQUEST_NOTE_SAVED,
            viewLifecycleOwner
        ) { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                loadNotes()
            }
        }
    }

    private suspend fun loadNotes() {
        val state = viewModel.getState()
        val notes = state.notes

        if (!isAdded || view == null) return
        binding.apply {
            recyclerviewNotes.isVisible = notes.isNotEmpty()
            layoutEmptyNotes.isVisible = notes.isEmpty()
            adapter.submitList(notes) {
                if (notes.isNotEmpty()) recyclerviewNotes.scrollToPosition(0)
            }
        }
    }

    private fun openNote(noteId: Long? = null) {
        val fragment = NoteFragment.newInstance(noteId = noteId ?: -1L)
        childFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(if (noteId == null) "AddNote" else "EditNote")
            .commit()
    }

    private fun updateFragmentContainerVisibility() {
        if (!isAdded || view == null) return
        binding.fragmentContainer.isVisible = childFragmentManager.backStackEntryCount > 0
    }
}