package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    companion object {

        private const val ARG_STATISTICS_TYPE = "statistics_type"
        fun newInstance(type: Int): StatisticsFragment {
            return StatisticsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_STATISTICS_TYPE, type)
                }
            }
        }
    }

    private var type: Int = 0

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(ARG_STATISTICS_TYPE, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}