package com.gasperpintar.smokingtracker.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gasperpintar.smokingtracker._interface.Identifiable

class Adapter<T : Identifiable>(
    private val layoutId: Int,
    private val onBind: (itemView: View, item: T) -> Unit
) : ListAdapter<T, Adapter.GenericViewHolder>(DiffCallback()) {

    class GenericViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)

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
    ): GenericViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return GenericViewHolder(view)
    }

    @Override
    override fun onBindViewHolder(
        holder: GenericViewHolder,
        position: Int
    ) {
        onBind(holder.itemView, getItem(position))
    }
}