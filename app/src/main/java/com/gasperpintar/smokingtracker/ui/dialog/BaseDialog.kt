package com.gasperpintar.smokingtracker.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.utils.RoundedAlertDialog

abstract class BaseDialog(
    protected val activity: FragmentActivity,
    layoutResource: Int
) {
    protected val dialogView: View = LayoutInflater.from(activity).inflate(layoutResource, null)
    protected val dialog: RoundedAlertDialog = RoundedAlertDialog(activity).setViewChained(dialogView)
    protected val buttonClose: Button? = dialogView.findViewById(R.id.button_close)

    init {
        buttonClose?.setOnClickListener {
            dialog.dismiss()
        }
    }

    abstract fun setup()

    fun show() {
        setup()
        dialog.showChained()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun setCancelable(cancelable: Boolean) {
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelable)
    }
}