package com.gasperpintar.smokingtracker.adapter.history

import androidx.recyclerview.widget.DiffUtil
import com.gasperpintar.smokingtracker.model.HistoryEntry

class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryEntry>() {

    override fun areItemsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry): Boolean {
        return oldItem == newItem
    }
}
