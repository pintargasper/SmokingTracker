package com.gasperpintar.smokingtracker.ui.dialog

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.gasperpintar.smokingtracker.R

class RoundedDialog(context: Context) : AlertDialog(context) {

    internal fun showChained(): RoundedDialog {
        super.show()
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.dialog_background)
        )
        return this
    }

    internal fun setViewChained(
        view: View
    ): RoundedDialog {
        super.setView(view)
        return this
    }
}