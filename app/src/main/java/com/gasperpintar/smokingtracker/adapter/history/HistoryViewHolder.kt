package com.gasperpintar.smokingtracker.adapter.history

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gasperpintar.smokingtracker.R

class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val timerLabel: TextView = itemView.findViewById(R.id.timer_label)
    val lentButton: ImageButton = itemView.findViewById(R.id.lent)
    val editButton: ImageButton = itemView.findViewById(R.id.image_button_edit)
    val deleteButton: ImageButton = itemView.findViewById(R.id.delete)
}
