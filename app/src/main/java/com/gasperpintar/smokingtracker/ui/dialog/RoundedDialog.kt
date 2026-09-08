package com.gasperpintar.smokingtracker.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.gasperpintar.smokingtracker.R

class RoundedDialog(context: Context) : AlertDialog(context) {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.dialog_background)
        )
    }
}