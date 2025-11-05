package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.gasperpintar.smokingtracker.R
import android.view.View

class RoundedAlertDialog(context: Context) : AlertDialog(context) {

    fun showChained(): RoundedAlertDialog {
        super.show()
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.dialog_background)
        )
        return this
    }

    fun setViewChained(view: View): RoundedAlertDialog {
        super.setView(view)
        return this
    }
}