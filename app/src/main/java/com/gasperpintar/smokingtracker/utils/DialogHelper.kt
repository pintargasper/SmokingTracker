package com.gasperpintar.smokingtracker.utils

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity

object DialogHelper {

    fun create(
        activity: FragmentActivity,
        layoutResourceId: Int,
        onBindView: (View, RoundedAlertDialog) -> Unit
    ): RoundedAlertDialog {
        val dialogView: View = LayoutInflater
            .from(activity)
            .inflate(layoutResourceId, null)

        val dialog: RoundedAlertDialog = RoundedAlertDialog(context = activity)
            .setViewChained(dialogView)
            .showChained()

        onBindView(dialogView, dialog)

        return dialog
    }
}
