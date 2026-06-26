package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentNoteBinding

class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding: FragmentNoteBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { v, _ ->
            v.performClick()
            true
        }

        binding.buttonClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

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
}