package com.gasperpintar.smokingtracker.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.utils.Helper.toHistoryEntity
import com.gasperpintar.smokingtracker.utils.Manager
import com.gasperpintar.smokingtracker.utils.RoundedAlertDialog
import kotlinx.coroutines.launch

@SuppressLint("InflateParams")
object DialogManager {

    fun showInsertDialog(
        context: FragmentActivity,
        layoutInflater: LayoutInflater,
        database: AppDatabase,
        lifecycleScope: LifecycleCoroutineScope,
        refreshUI: () -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.insert_popup, null)
        val dialog = RoundedAlertDialog(context = context)
            .setViewChained(dialogView)
            .showChained()

        val buttonConfirm: Button = dialogView.findViewById(R.id.button_insert)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)
        val lentCheckbox: CheckBox = dialogView.findViewById(R.id.lent_checkbox)

        buttonConfirm.setOnClickListener {
            val entry = com.gasperpintar.smokingtracker.database.entity.HistoryEntity(
                id = 0,
                lent = if (lentCheckbox.isChecked) {
                    1
                } else {
                    0
                },
                createdAt = java.time.LocalDateTime.now()
            )
            lifecycleScope.launch {
                database.historyDao().insert(history = entry)
                refreshUI()
            }
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showLanguageDialog(
        activity: FragmentActivity,
        selectedLanguage: Int,
        onLanguageSelected: (Int) -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.language_popup, null)
        val dialog = RoundedAlertDialog(context = activity)
            .setViewChained(dialogView)
            .showChained()

        val buttonCancel: Button = dialogView.findViewById(R.id.button_close)

        val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)
        val checkboxEnglish: CheckBox = dialogView.findViewById(R.id.checkbox_english)
        val checkboxSlovenian: CheckBox = dialogView.findViewById(R.id.checkbox_slovenian)

        fun applySelectedCheck() {
            checkboxSystem.isChecked = selectedLanguage == 0
            checkboxEnglish.isChecked = selectedLanguage == 1
            checkboxSlovenian.isChecked = selectedLanguage == 2
        }

        fun selectLanguage(language: Int, checkbox: CheckBox) {
            checkboxSystem.isChecked = checkbox == checkboxSystem
            checkboxEnglish.isChecked = checkbox == checkboxEnglish
            checkboxSlovenian.isChecked = checkbox == checkboxSlovenian

            onLanguageSelected(language)
            dialog.dismiss()
        }

        applySelectedCheck()

        checkboxSystem.setOnClickListener { selectLanguage(0, checkboxSystem) }
        checkboxEnglish.setOnClickListener { selectLanguage(1, checkboxEnglish) }
        checkboxSlovenian.setOnClickListener { selectLanguage(2, checkboxSlovenian) }
        buttonCancel.setOnClickListener { dialog.dismiss() }
    }

    fun showThemeDialog(
        activity: FragmentActivity,
        selectedTheme: Int,
        onThemeSelected: (Int) -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.theme_popup, null)
        val dialog = RoundedAlertDialog(context = activity)
            .setViewChained(dialogView)
            .showChained()

        val buttonCancel: Button = dialogView.findViewById(R.id.button_close)

        val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)
        val checkboxLightTheme: CheckBox = dialogView.findViewById(R.id.checkbox_light_theme)
        val checkboxDarkTheme: CheckBox = dialogView.findViewById(R.id.checkbox_dark_theme)

        fun applySelectedCheck() {
            checkboxSystem.isChecked = selectedTheme == 0
            checkboxLightTheme.isChecked = selectedTheme == 1
            checkboxDarkTheme.isChecked = selectedTheme == 2
        }

        fun selectLanguage(theme: Int, checkbox: CheckBox) {
            checkboxSystem.isChecked = checkbox == checkboxSystem
            checkboxLightTheme.isChecked = checkbox == checkboxLightTheme
            checkboxDarkTheme.isChecked = checkbox == checkboxDarkTheme

            onThemeSelected(theme)
            dialog.dismiss()
        }

        applySelectedCheck()

        checkboxSystem.setOnClickListener { selectLanguage(0, checkboxSystem) }
        checkboxLightTheme.setOnClickListener { selectLanguage(1, checkboxLightTheme) }
        checkboxDarkTheme.setOnClickListener { selectLanguage(2, checkboxDarkTheme) }
        buttonCancel.setOnClickListener { dialog.dismiss() }
    }

    fun showNotificationsDialog(
        activity: FragmentActivity,
        selectedNotificationOption: Int,
        onNotificationOptionSelected: (Int) -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.notifications_popup, null)
        val dialog = RoundedAlertDialog(context = activity)
            .setViewChained(dialogView)
            .showChained()

        val buttonCancel: Button = dialogView.findViewById(R.id.button_close)
        val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)

        checkboxSystem.isChecked = selectedNotificationOption == 1

        checkboxSystem.setOnCheckedChangeListener { _, isChecked ->
            onNotificationOptionSelected(if (isChecked) 1 else 0)
        }
        buttonCancel.setOnClickListener { dialog.dismiss() }
    }

    fun showDownloadDialog(activity: FragmentActivity, database: AppDatabase) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.download_popup, null)
        val dialog = RoundedAlertDialog(context = activity)
            .setViewChained(dialogView)
            .showChained()

        val buttonDownload: Button = dialogView.findViewById(R.id.button_download)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)

        buttonDownload.setOnClickListener {
            activity.lifecycleScope.launch {
                Manager.downloadFile(context = activity, database = database)
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
        val dialog = RoundedAlertDialog(context = activity)
            .setViewChained(dialogView)
            .showChained()

        val textViewSelectedFile: TextView = dialogView.findViewById(R.id.text_selected_file)
        val buttonOpenFile: Button = dialogView.findViewById(R.id.button_open_file)
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)

        selectedFileSetter(textViewSelectedFile)
        textViewSelectedFile.text = String.format(
            $$"%1$s: %2$s",
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
        buttonClose.setOnClickListener { dialog.dismiss() }
    }

    fun showEditDialog(
        context: FragmentActivity,
        layoutInflater: LayoutInflater,
        database: AppDatabase,
        lifecycleScope: LifecycleCoroutineScope,
        entry: com.gasperpintar.smokingtracker.model.HistoryEntry,
        refreshUI: () -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.edit_popup, null)
        val dialog = RoundedAlertDialog(context = context)
            .setViewChained(dialogView)
            .showChained()

        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)
        val lentCheckbox: CheckBox = dialogView.findViewById(R.id.lent_checkbox)
        val datePicker: android.widget.DatePicker = dialogView.findViewById(R.id.date_picker)
        val timePicker: TimePicker = dialogView.findViewById(R.id.time_picker)

        lentCheckbox.isChecked = entry.isLent
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))

        entry.createdAt.let { dateTime ->
            datePicker.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
            timePicker.hour = dateTime.hour
            timePicker.minute = dateTime.minute
        }

        buttonConfirm.setOnClickListener {
            val updatedEntry = entry.copy(
                createdAt = entry.createdAt.withYear(datePicker.year)
                    .withMonth(datePicker.month + 1)
                    .withDayOfMonth(datePicker.dayOfMonth)
                    .withHour(timePicker.hour)
                    .withMinute(timePicker.minute),
                isLent = lentCheckbox.isChecked
            )
            lifecycleScope.launch {
                database.historyDao().update(history = updatedEntry.toHistoryEntity())
                refreshUI()
            }
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showDeleteDialog(
        context: FragmentActivity,
        layoutInflater: LayoutInflater,
        database: AppDatabase,
        lifecycleScope: LifecycleCoroutineScope,
        entry: com.gasperpintar.smokingtracker.model.HistoryEntry,
        refreshUI: () -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.delete_popup, null)
        val dialog = RoundedAlertDialog(context = context)
            .setViewChained(dialogView)
            .showChained()

        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)

        buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                database.historyDao().delete(history = entry.toHistoryEntity())
                refreshUI()
            }
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}