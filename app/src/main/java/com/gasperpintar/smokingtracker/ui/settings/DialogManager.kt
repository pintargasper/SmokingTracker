package com.gasperpintar.smokingtracker.ui.settings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.utils.Manager
import kotlinx.coroutines.launch

object DialogManager {

    fun showDownloadDialog(activity: FragmentActivity, database: AppDatabase) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.download_popup, null)
        val dialog = android.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()
        dialog.show()

        val buttonDownload: Button = dialogView.findViewById(R.id.button_download)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)

        buttonDownload.setOnClickListener {
            activity.lifecycleScope.launch {
                Manager.downloadFile(activity, database)
            }
            dialog.dismiss()
        }
        buttonClose.setOnClickListener { dialog.dismiss() }
    }

    fun showUploadDialog(
        activity: FragmentActivity,
        database: AppDatabase,
        filePickerLauncher: ActivityResultLauncher<Intent>,
        selectedFileSetter: (TextView) -> Unit,
        getSelectedFileUri: () -> Uri?,
        clearSelectedFile: () -> Unit,
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.upload_popup, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()

        dialog.show()

        val textViewSelectedFile: TextView = dialogView.findViewById(R.id.text_selected_file)
        val buttonOpenFile: Button = dialogView.findViewById(R.id.button_open_file)
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val buttonBack: Button = dialogView.findViewById(R.id.button_back)

        selectedFileSetter(textViewSelectedFile)
        textViewSelectedFile.text = activity.getString(
            R.string.upload_popup_file_status,
            activity.getString(R.string.upload_popup_file),
            activity.getString(R.string.upload_popup_file_none)
        )

        dialog.setOnDismissListener { clearSelectedFile() }

        buttonOpenFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            filePickerLauncher.launch(intent)
        }

        buttonConfirm.setOnClickListener {
            val fileUri = getSelectedFileUri() ?: return@setOnClickListener

            activity.lifecycleScope.launch {
                dialog.dismiss()
                Manager.uploadFile(
                    context = activity,
                    fileUri = fileUri,
                    database = database
                )
                activity.recreate()
            }
        }
        buttonBack.setOnClickListener { dialog.dismiss() }
    }
}
