package com.gasperpintar.smokingtracker.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R

abstract class BaseDialog(
    protected val activity: FragmentActivity,
    layoutResource: Int
) {
    internal val dialogView: View = LayoutInflater.from(activity).inflate(layoutResource, null)
    internal val dialog: RoundedDialog = RoundedDialog(activity).setViewChained(dialogView)
    internal val buttonClose: Button? = dialogView.findViewById(R.id.button_close)

    init {
        buttonClose?.setOnClickListener {
            dialog.dismiss()
        }
    }

    internal abstract fun setup()

    internal fun show() {
        setup()
        dialog.showChained()
    }

    internal fun dismiss() {
        dialog.dismiss()
    }

    internal fun setCancelable(cancelable: Boolean) {
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelable)
    }
}