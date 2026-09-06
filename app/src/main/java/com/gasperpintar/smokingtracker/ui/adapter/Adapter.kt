package com.gasperpintar.smokingtracker.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.gasperpintar.smokingtracker._interface.Identifiable

class Adapter<T : Identifiable>(
    private val layoutId: Int,
    private val onBind: (itemView: View, item: T) -> Unit
) : ListAdapter<T, ViewHolder>(DiffCallback()) {

    private class DiffCallback<T : Identifiable> : DiffUtil.ItemCallback<T>() {

        @Override
        override fun areItemsTheSame(
            oldItem: T,
            newItem: T
        ): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint(value = ["DiffUtilEquals"])
        @Override
        override fun areContentsTheSame(
            oldItem: T,
            newItem: T
        ): Boolean {
            return oldItem == newItem
        }
    }

    @Override
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    @Override
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        onBind(holder.itemView, getItem(position))
    }
}