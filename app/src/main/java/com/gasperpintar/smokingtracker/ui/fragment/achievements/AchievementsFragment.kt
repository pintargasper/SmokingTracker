package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.viewmodel.AchievementViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

class AchievementsFragment: Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AchievementViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    private lateinit var achievementType: AchievementCategory
    private lateinit var adapter: Adapter<AchievementEntry>

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        achievementType = AchievementCategory.valueOf(
            requireArguments().getString("achievement_type")!!
        )
    }

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        setupAdapter()
        loadAchievements()
    }

    private fun setupAdapter() = with(receiver = binding) {
        adapter = Adapter(
            layoutId = R.layout.achievements_container,
            onBind = { itemView, achievementEntry ->
                val imageAchievement = itemView.findViewById<ImageView>(R.id.image_achievement)
                val textAchievementTitle = itemView.findViewById<TextView>(R.id.text_achievement_title)
                val textAchievementMessage = itemView.findViewById<TextView>(R.id.text_achievement_message)
                val textLastAchieved = itemView.findViewById<TextView>(R.id.text_last_achieved_label)
                val textLastAchievedCountValue = itemView.findViewById<TextView>(R.id.text_achieved_count_label)

                textAchievementTitle.text = getString(AchievementTitle.valueOf(achievementEntry.title).stringResource)
                textAchievementMessage.text = getString(AchievementMessage.valueOf(achievementEntry.message).stringResource)

                textLastAchieved.text = achievementEntry.lastAchieved?.toLocalDate()?.let { localDate: LocalDate ->
                    getString(R.string.achievement_last, LocalizationHelper.formatDate(date = localDate))
                } ?: getString(R.string.achievement_last, "/")

                val achievedTimesText: String = requireContext().resources.getQuantityString(
                    R.plurals.achievement_achieved_times,
                    achievementEntry.times.toInt(),
                    achievementEntry.times
                )

                textLastAchievedCountValue.text = getString(
                    R.string.achievement_achieved,
                    achievedTimesText
                )

                imageAchievement.setImageResource(AchievementIcon.valueOf(achievementEntry.image).drawableResource)
                when (achievementEntry.times) {
                    0L -> {
                        imageAchievement.colorFilter = ColorMatrixColorFilter(
                        ColorMatrix().apply { setSaturation(0f) })
                        imageAchievement.alpha = 0.4f
                    }
                    else -> {
                        imageAchievement.clearColorFilter()
                        imageAchievement.alpha = 1f
                    }
                }
            }
        )
        recyclerviewAchievements.layoutManager = LinearLayoutManager(requireContext())
        recyclerviewAchievements.adapter = adapter
    }

    private fun loadAchievements() = with(receiver = binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getAchievements(category = achievementType)
            adapter.submitList(state.achievements) {
                recyclerviewAchievements.scrollToPosition(0)
            }
        }
    }
}