package com.gasperpintar.smokingtracker.adapter.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.DiffCallback
import com.gasperpintar.smokingtracker.model.HistoryEntry

class HistoryAdapter(
    private val onEditClick: (HistoryEntry) -> Unit,
    private val onDeleteClick: (HistoryEntry) -> Unit
) : ListAdapter<HistoryEntry, HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_container, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyEntry = getItem(position)

        holder.timerLabel.text = historyEntry.timerLabel
        holder.lentButton.visibility = if (historyEntry.isLent) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.editButton.setOnClickListener { onEditClick(historyEntry) }
        holder.deleteButton.setOnClickListener { onDeleteClick(historyEntry) }
    }
}