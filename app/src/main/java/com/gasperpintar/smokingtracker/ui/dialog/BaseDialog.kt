package com.gasperpintar.smokingtracker.ui.dialog

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker.R

abstract class BaseDialog<B : ViewBinding>(
    protected val activity: FragmentActivity,
    bindingInflater: (LayoutInflater) -> B
) {
    internal val binding: B = bindingInflater(activity.layoutInflater)
    internal val dialog = RoundedDialog(context = activity).apply { setView(binding.root) }

    init {
        binding.root.findViewById<View>(R.id.close)?.setOnClickListener { dismiss() }
    }

    internal abstract fun setup()

    internal open fun show() {
        setup()
        dialog.show()
    }

    internal fun dismiss() {
        dialog.dismiss()
    }

    internal fun setCancelable(cancelable: Boolean) = dialog.run {
        setCancelable(cancelable)
        setCanceledOnTouchOutside(cancelable)
    }

    companion object {
        internal inline fun <B : ViewBinding> show(
            context: FragmentActivity,
            noinline bindingInflater: (LayoutInflater) -> B,
            crossinline block: BaseDialog<B>.() -> Unit
        ) {
            object : BaseDialog<B>(activity = context, bindingInflater) {
                override fun setup() = block()
            }.apply { show() }
        }
    }
}