package com.gasperpintar.smokingtracker.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.ui.container.BaseContainer

class Adapter<T : IIdentifiable, B : ViewBinding>(
    private val createViewHolder: (LayoutInflater, ViewGroup) -> ViewHolder<B>,
    private val onBind: BaseContainer.(B, T) -> Unit
) : ListAdapter<T, Adapter.ViewHolder<B>>(Callback()) {

    class ViewHolder<B : ViewBinding>(
        val binding: B,
        val container: BaseContainer
    ) : RecyclerView.ViewHolder(container.binding.root)

    private class Callback<T : IIdentifiable> : DiffUtil.ItemCallback<T>() {

        @Override
        override fun areItemsTheSame(
            oldItem: T,
            newItem: T
        ): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        override fun areContentsTheSame(
            oldItem: T,
            newItem: T
        ): Boolean {
            return oldItem == newItem
        }
    }

    @Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> {
        return createViewHolder(LayoutInflater.from(parent.context), parent)
    }

    @Override
    override fun onBindViewHolder(
        holder: ViewHolder<B>,
        position: Int
    ) {
        holder.container.onBind(holder.binding, getItem(position))
    }
}