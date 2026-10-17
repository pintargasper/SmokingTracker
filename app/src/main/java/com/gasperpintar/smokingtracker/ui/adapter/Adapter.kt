package com.gasperpintar.smokingtracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.ui.container.BaseContainer

class Adapter<T : Identifiable, B : ViewBinding>(
    private val createViewHolder: (LayoutInflater, ViewGroup) -> ViewHolder<B>,
    private val onBind: BaseContainer.(B, T) -> Unit
) : ListAdapter<T, ViewHolder<B>>(Callback()) {

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