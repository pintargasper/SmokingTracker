package com.gasperpintar.smokingtracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class Adapter<T>(
    private val layoutId: Int,
    private val onBind: (itemView: View, item: T) -> Unit,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, Adapter.GenericViewHolder>(diffCallback) {

    class GenericViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return GenericViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        onBind(holder.itemView, getItem(position))
    }
}
