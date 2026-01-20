package com.gasperpintar.smokingtracker.adapter.achievements

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gasperpintar.smokingtracker.R

class AchievementsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val achievementContainer : View = itemView.findViewById(R.id.achievement_container)
    var imageAchievement: ImageView = itemView.findViewById(R.id.image_achievement)
    val textAchievement: TextView = itemView.findViewById(R.id.text_achievement)
}