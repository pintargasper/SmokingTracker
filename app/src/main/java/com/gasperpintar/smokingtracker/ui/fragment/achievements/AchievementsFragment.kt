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
import androidx.recyclerview.widget.GridLayoutManager
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.Adapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.DialogManager
import kotlinx.coroutines.launch

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: Lazy<AppDatabase>
    private lateinit var adapter: Adapter<AchievementEntry>
    private lateinit var achievementType: AchievementCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val typeOrdinal = arguments?.getInt(ARG_ACHIEVEMENT_TYPE)
        achievementType = typeOrdinal?.let { AchievementCategory.entries[it] } ?: AchievementCategory.SMOKE_FREE_TIME
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = lazy { (requireActivity() as AchievementsActivity).database }

        setupRecyclerView()

        lifecycleScope.launch {
            val achievementDao = database.value.achievementDao()
            val achievements = achievementDao.getAllAchievements().map {
                AchievementEntry(
                    id = it.id,
                    image = it.image,
                    value = it.value,
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

    companion object {
        private const val ARG_ACHIEVEMENT_TYPE = "achievement_type"
        fun newInstance(type: AchievementCategory): AchievementsFragment {
            return AchievementsFragment().apply {
                arguments = Bundle().apply { putInt(ARG_ACHIEVEMENT_TYPE, type.ordinal) }
            }
        }
    }

    private fun setupRecyclerView() {
        val adapter = Adapter(
            layoutId = R.layout.achievements_container,
            onBind = { itemView, achievementEntry ->
                val imageAchievement = itemView.findViewById<ImageView>(R.id.image_achievement)
                val textAchievement = itemView.findViewById<TextView>(R.id.text_achievement)
                val container = itemView.findViewById<View>(R.id.achievement_container)

                imageAchievement.setImageResource(achievementEntry.image)
                textAchievement.text = achievementEntry.getDisplayText(itemView.context)

                container.setOnClickListener {
                    DialogManager.showAchievementsDialog(
                        context = requireActivity(),
                        layoutInflater = layoutInflater,
                        entry = achievementEntry
                    )
                }
            },
            diffCallback = object : DiffUtil.ItemCallback<AchievementEntry>() {
                override fun areItemsTheSame(oldItem: AchievementEntry, newItem: AchievementEntry) = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: AchievementEntry, newItem: AchievementEntry) = oldItem == newItem
            }
        )
        binding.recyclerviewAchievements.layoutManager = GridLayoutManager(requireContext(), calculateGridSpanCount())
        binding.recyclerviewAchievements.adapter = adapter
        this.adapter = adapter
    }

    private fun loadAchievements(achievementEntries: List<AchievementEntry>) {
        adapter.submitList(achievementEntries) {
            binding.recyclerviewAchievements.scrollToPosition(0)
        }
    }

    private fun calculateGridSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp: Float = displayMetrics.widthPixels / displayMetrics.density

        return when {
            screenWidthDp >= 900 -> 4
            screenWidthDp >= 600 -> 3
            else -> 2
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}