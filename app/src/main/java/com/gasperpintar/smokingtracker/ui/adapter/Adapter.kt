package com.gasperpintar.smokingtracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker._interface.Identifiable

class Adapter<T : Identifiable, B : ViewBinding>(
    private val bindingFactory: (LayoutInflater, ViewGroup, Boolean) -> B,
    private val onBind: B.(T) -> Unit
) : ListAdapter<T, ViewHolder<B>>(Callback()) {

    @Override
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<B> {
        return ViewHolder(
            binding = bindingFactory(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @Override
    override fun onBindViewHolder(
        holder: ViewHolder<B>,
        position: Int
    ) {
        holder.binding.onBind(getItem(position))
    }
}