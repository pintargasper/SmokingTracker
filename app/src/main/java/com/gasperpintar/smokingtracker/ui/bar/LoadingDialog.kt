package com.gasperpintar.smokingtracker.ui.bar

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.DialogContentLoadingBinding
import com.gasperpintar.smokingtracker.ui.dialog.BaseDialog

class LoadingDialog(
    context: FragmentActivity
) : BaseDialog(activity = context) {

    private val content = DialogContentLoadingBinding.inflate(
        context.layoutInflater,
        binding.dialogContent,
        true
    )

    @Override
    override fun setup() {
        setCancelable(false)
        binding.dialogButtons.isVisible = false
        content.popupProgressBar.max = 100
        updateProgress(progress = 0)
    }

    fun updateProgress(
        progress: Int
    ) {
        return activity.runOnUiThread {
            content.popupProgressBar.progress = progress
            content.percentage.text = activity.getString(R.string.loading_bar_progress_percentage, progress)
            binding.message.setText(
                when {
                    progress >= 100 -> R.string.loading_bar_encourage_100
                    progress >= 70 -> R.string.loading_bar_encourage_70
                    progress >= 50 -> R.string.loading_bar_encourage_50
                    progress >= 15 -> R.string.loading_bar_encourage_15
                    else -> R.string.loading_bar_encourage_0
                }
            )
        }
    }

    fun setProgressType(
        type: ProgressType
    ) {
        return binding.title.setText(
            if (type == ProgressType.BACKUP) R.string.loading_bar_backup
            else R.string.loading_bar_restore
        )
    }
}