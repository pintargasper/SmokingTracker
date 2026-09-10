package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.viewmodel.AchievementViewModel
import com.gasperpintar.smokingtracker.databinding.AchievementsContainerBinding
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatLocalized
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
    private lateinit var adapter: Adapter<AchievementEntry, AchievementsContainerBinding>

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

    private fun setupAdapter() = binding.apply {
        adapter = Adapter(
            bindingFactory = AchievementsContainerBinding::inflate,
            onBind = { achievementEntry ->
                achievementTitle.text = getString(AchievementTitle.valueOf(achievementEntry.title).stringResource)
                achievementMessage.text = getString(AchievementMessage.valueOf(achievementEntry.message).stringResource)
                lastAchieved.text = achievementEntry.lastAchieved?.toLocalDate()?.let { localDate: LocalDate ->
                    getString(R.string.achievement_last, localDate.formatLocalized())
                } ?: getString(R.string.achievement_last, "/")

                val achievedTimesText: String = requireContext().resources.getQuantityString(
                    R.plurals.achievement_achieved_times,
                    achievementEntry.times.toInt(),
                    achievementEntry.times
                )

                achievedCount.text = getString(
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

    private fun loadAchievements() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getAchievements(category = achievementType)
            adapter.submitList(state.achievements) {
                recyclerviewAchievements.scrollToPosition(0)
            }
        }
    }
}