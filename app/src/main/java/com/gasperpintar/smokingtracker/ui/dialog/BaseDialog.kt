package com.gasperpintar.smokingtracker.ui.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker.databinding.DialogBaseBinding

abstract class BaseDialog(
    protected val activity: FragmentActivity
) {
    internal val binding: DialogBaseBinding = DialogBaseBinding.inflate(activity.layoutInflater)
    internal val dialog = RoundedDialog(context = activity).apply {
        setView(binding.root)
    }

    init {
        binding.close.setOnClickListener {
            dismiss()
        }
    }

    internal abstract fun setup()

    open fun show() {
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
        internal inline fun show(
            context: FragmentActivity,
            crossinline block: BaseDialog.() -> Unit
        ) {
            object : BaseDialog(activity = context) {
                @Override
                override fun setup() {
                    block()
                }
            }.show()
        }

        internal inline fun <B : ViewBinding> show(
            context: FragmentActivity,
            noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> B,
            crossinline block: BaseDialog.(B) -> Unit
        ) {
            object : BaseDialog(activity = context) {
                private val content: B = inflate(context.layoutInflater, binding.dialogContent, true)

                @Override
                override fun setup() {
                    block(content)
                }
            }.show()
        }
    }
}