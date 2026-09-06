package com.gasperpintar.smokingtracker.ui.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.gasperpintar.smokingtracker._interface.Identifiable

class Callback<T : Identifiable> : DiffUtil.ItemCallback<T>() {

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
        oldItem: T, newItem: T
    ): Boolean {
        return oldItem == newItem
    }
}