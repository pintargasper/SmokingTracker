package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.Adapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.launch

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
        val root: View = binding.root

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
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val adapter = Adapter(
            layoutId = R.layout.achievements_container,
            onBind = { itemView, achievementEntry ->
                val imageAchievement = itemView.findViewById<ImageView>(R.id.image_achievement)
                val textAchievementTitle = itemView.findViewById<TextView>(R.id.text_achievement_title)
                val textAchievementMessage = itemView.findViewById<TextView>(R.id.text_achievement_message)
                val textLastAchievedValue = itemView.findViewById<TextView>(R.id.text_last_achieved_value)
                val textLastAchievedCountValue = itemView.findViewById<TextView>(R.id.text_achieved_count_value)

                imageAchievement.setImageResource(achievementEntry.image)
                textAchievementTitle.text = getString(achievementEntry.title)
                textAchievementMessage.text = getString(achievementEntry.message)
                textLastAchievedValue.text = achievementEntry.lastAchieved?.toLocalDate()?.let { LocalizationHelper.formatDate(date = it) } ?: "-"
                textLastAchievedCountValue.text = requireContext().resources.getQuantityString(
                    R.plurals.achievement_achieved_times,
                    achievementEntry.times.toInt(),
                    achievementEntry.times
                )
            },
            diffCallback = object : DiffUtil.ItemCallback<AchievementEntry>() {
                override fun areItemsTheSame(oldItem: AchievementEntry, newItem: AchievementEntry) = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: AchievementEntry, newItem: AchievementEntry) = oldItem == newItem
            }
        )
        binding.recyclerviewAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewAchievements.adapter = adapter
        this.adapter = adapter
    }

    private fun loadAchievements(
        achievementEntries: List<AchievementEntry>
    ) {
        adapter.submitList(achievementEntries) {
            binding.recyclerviewAchievements.scrollToPosition(0)
        }
    }
}