package com.gasperpintar.smokingtracker.adapter.achievements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.DiffCallback
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.utils.Helper.getDisplayText

class AchievementsAdapter(
    private val onItemClick: (AchievementEntry) -> Unit
) : ListAdapter<AchievementEntry, AchievementsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.achievements_container, parent, false)
        return AchievementsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AchievementsViewHolder, position: Int) {
        val achievementEntry: AchievementEntry = getItem(position)

        holder.imageAchievement.setImageResource(achievementEntry.image)
        holder.textAchievement.text = achievementEntry.getDisplayText(holder.itemView.context)

        holder.achievementContainer.setOnClickListener {
            onItemClick(achievementEntry)
        }
    }
}