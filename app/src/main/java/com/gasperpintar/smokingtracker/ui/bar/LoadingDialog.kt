package com.gasperpintar.smokingtracker.ui.bar

import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.ui.dialog.BaseDialog

class LoadingDialog(
    context: FragmentActivity
) : BaseDialog(activity = context, R.layout.loading_popup) {

    private lateinit var progressTextMessage: TextView
    private lateinit var progressTextInfo: TextView
    private lateinit var progressTextPercentage: TextView
    private lateinit var progressBar: ProgressBar

    override fun setup() {
        setCancelable(false)
        progressTextMessage = dialogView.findViewById(R.id.popup_message)
        progressTextInfo = dialogView.findViewById(R.id.popup_message_info)
        progressTextPercentage = dialogView.findViewById(R.id.popup_message_percentage)
        progressBar = dialogView.findViewById(R.id.popup_progress_bar)

        progressTextInfo.text = activity.getString(R.string.loading_bar_encourage_0)
        progressTextPercentage.text = activity.getString(R.string.loading_bar_progress_percentage, 0)

        progressBar.isIndeterminate = false
        progressBar.max = 100
        progressBar.progress = 0
    }

    fun updateProgress(progress: Int) {
        activity.runOnUiThread {
            progressBar.progress = progress
            progressTextPercentage.text = activity.getString(R.string.loading_bar_progress_percentage, progress)

            progressTextInfo.text = when {
                progress >= 100 -> activity.getString(R.string.loading_bar_encourage_100)
                progress >= 70 -> activity.getString(R.string.loading_bar_encourage_70)
                progress >= 50 -> activity.getString(R.string.loading_bar_encourage_50)
                progress >= 15 -> activity.getString(R.string.loading_bar_encourage_15)
                progress >= 0 -> activity.getString(R.string.loading_bar_encourage_0)
                else -> null
            }
        }
    }

    fun setProgressType(type: ProgressType) {
        when (type) {
            ProgressType.BACKUP -> progressTextMessage.text = activity.getString(R.string.loading_bar_message_backup)
            ProgressType.RESTORE -> progressTextMessage.text = activity.getString(R.string.loading_bar_message_restore)
        }
    }
}