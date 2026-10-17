package com.gasperpintar.smokingtracker.ui.container

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.databinding.ContainerBaseBinding
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.adapter.ViewHolder

abstract class BaseContainer(
    protected val activity: FragmentActivity,
    parent: ViewGroup
) {
    internal val binding: ContainerBaseBinding = ContainerBaseBinding.inflate(activity.layoutInflater, parent, false)

    companion object {
        internal inline fun <T : Identifiable, B : ViewBinding> create(
            context: FragmentActivity,
            crossinline inflate: (LayoutInflater, ViewGroup, Boolean) -> B,
            noinline onBind: BaseContainer.(B, T) -> Unit
        ): Adapter<T, B> {
            return Adapter(
                createViewHolder = { inflater, parent ->
                    val container = object : BaseContainer(activity = context, parent = parent) {}
                    val content = inflate(inflater, container.binding.containerContent, true)
                    ViewHolder(binding = content, container = container)
                },
                onBind = onBind
            )
        }
    }
}