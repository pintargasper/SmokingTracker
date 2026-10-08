package com.gasperpintar.smokingtracker.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker.ui.container.BaseContainer

class ViewHolder<B : ViewBinding>(
    val binding: B,
    val container: BaseContainer
) : RecyclerView.ViewHolder(container.binding.root)