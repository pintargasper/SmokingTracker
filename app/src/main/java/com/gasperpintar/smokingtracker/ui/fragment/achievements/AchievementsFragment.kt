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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var adapter: Adapter<AchievementEntry>
    private lateinit var achievementType: AchievementCategory

    companion object {

        private const val ARG_ACHIEVEMENT_TYPE = "achievement_type"
        fun newInstance(type: AchievementCategory): AchievementsFragment {
            return AchievementsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ACHIEVEMENT_TYPE, type.ordinal)
                }
            }
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        database = (requireActivity() as AchievementsActivity).database
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        val typeOrdinal = arguments?.getInt(ARG_ACHIEVEMENT_TYPE)
        achievementType = typeOrdinal?.let {
            AchievementCategory.entries[it]
        } ?: AchievementCategory.SMOKE_FREE_TIME
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        lifecycleScope.launch {
            val achievements = achievementRepository.getAll().map {
                AchievementEntry(
                    id = it.id,
                    image = it.image,
                    value = it.value,
                    title = it.title,
                    message = it.message,
                    times = it.times,
                    lastAchieved = it.lastAchieved,
                    reset = it.reset,
                    notify = it.notify,
                    category = it.category,
                    unit = it.unit
                )
            }.filter { it.category == achievementType }
            loadAchievements(achievementEntries = achievements)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
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

                textLastAchieved.text = achievementEntry.lastAchieved
                    ?.toLocalDate()
                    ?.let { localDate: LocalDate ->
                        getString(
                            R.string.achievement_last,
                            LocalizationHelper.formatDate(date = localDate)
                        )
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

                imageAchievement.setImageResource(
                    AchievementIcon.valueOf(achievementEntry.image).drawableResource
                )

                if (achievementEntry.times == 0L) {
                    imageAchievement.colorFilter = ColorMatrixColorFilter(
                        ColorMatrix().apply {
                            setSaturation(0f)
                        }
                    )
                    imageAchievement.alpha = 0.4f
                } else {
                    imageAchievement.clearColorFilter()
                    imageAchievement.alpha = 1f
                }
            }
        )
        binding.recyclerviewAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewAchievements.adapter = adapter
    }

    private fun loadAchievements(
        achievementEntries: List<AchievementEntry>
    ) {
        adapter.submitList(achievementEntries) {
            binding.recyclerviewAchievements.scrollToPosition(0)
        }
    }
}