package com.gasperpintar.smokingtracker.ui.bar

import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.LoadingPopupBinding
import com.gasperpintar.smokingtracker.ui.dialog.BaseDialog

class LoadingDialog(
    context: FragmentActivity
) : BaseDialog<LoadingPopupBinding>(
    activity = context,
    bindingInflater = LoadingPopupBinding::inflate
) {

    @Override
    override fun setup() {
        setCancelable(false)
        binding.popupProgressBar.max = 100
        updateProgress(progress = 0)
    }

    fun updateProgress(
        progress: Int
    ) = activity.runOnUiThread {
        binding.popupProgressBar.progress = progress
        binding.popupMessagePercentage.text = activity.getString(R.string.loading_bar_progress_percentage, progress)
        binding.popupMessageInfo.setText(
            when {
                progress >= 100 -> R.string.loading_bar_encourage_100
                progress >= 70 -> R.string.loading_bar_encourage_70
                progress >= 50 -> R.string.loading_bar_encourage_50
                progress >= 15 -> R.string.loading_bar_encourage_15
                else -> R.string.loading_bar_encourage_0
            }
        )
    }

    fun setProgressType(
        type: ProgressType
    ) = binding.popupMessage.setText(
        if (type == ProgressType.BACKUP) R.string.loading_bar_backup else R.string.loading_bar_restore
    )
}