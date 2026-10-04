package com.gasperpintar.smokingtracker.ui.dialog

import androidx.fragment.app.FragmentActivity
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
        inline fun show(
            context: FragmentActivity,
            crossinline block: BaseDialog.() -> Unit
        ) {
            object : BaseDialog(activity = context) {
                override fun setup() = block()
            }.show()
        }
    }
}