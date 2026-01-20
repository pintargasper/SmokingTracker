package com.gasperpintar.smokingtracker.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.gasperpintar.smokingtracker._interface.Identifiable

class DiffCallback<T : Identifiable> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}
