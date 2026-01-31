package com.gasperpintar.smokingtracker.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.utils.RoundedAlertDialog

abstract class BaseDialog(
    context: FragmentActivity,
    layoutResource: Int
) {
    protected val dialogView: View = LayoutInflater.from(context).inflate(layoutResource, null)
    protected val dialog: RoundedAlertDialog = RoundedAlertDialog(context).setViewChained(dialogView)
    protected val buttonClose: Button? = dialogView.findViewById(R.id.button_close)

    init {
        buttonClose?.setOnClickListener { dialog.dismiss() }
    }

    abstract fun setup()

    fun show() {
        setup()
        dialog.showChained()
    }
}