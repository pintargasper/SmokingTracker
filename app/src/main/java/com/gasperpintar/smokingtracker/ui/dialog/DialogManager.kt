package com.gasperpintar.smokingtracker.ui.dialog

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.model.HistoryEntry
import java.time.LocalDateTime
import java.util.Calendar

object DialogManager {

    fun showInsertDialog(
        context: FragmentActivity,
        onConfirm: (isLent: Boolean) -> Unit
    ) {
        val dialog = object : BaseDialog(context, R.layout.insert_popup) {
            override fun setup() {
                val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
                val lentCheckbox: CheckBox = dialogView.findViewById(R.id.lent_checkbox)

                buttonConfirm.setOnClickListener {
                    onConfirm(lentCheckbox.isChecked)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    fun showEditDialog(
        context: FragmentActivity,
        entry: HistoryEntry,
        onConfirm: (newDateTime: LocalDateTime, isLent: Boolean) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.edit_popup) {
            override fun setup() {
                val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
                val lentCheckbox: CheckBox = dialogView.findViewById(R.id.lent_checkbox)
                val datePicker: DatePicker = dialogView.findViewById(R.id.date_picker)
                val timePicker: TimePicker = dialogView.findViewById(R.id.time_picker)

                lentCheckbox.isChecked = entry.isLent

                timePicker.setIs24HourView(DateFormat.is24HourFormat(context))

                entry.createdAt.let { dateTime ->
                    datePicker.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
                    timePicker.hour = dateTime.hour
                    timePicker.minute = dateTime.minute
                }

                buttonConfirm.setOnClickListener {
                    val selectedDateTime = LocalDateTime.of(
                        datePicker.year,
                        datePicker.month + 1,
                        datePicker.dayOfMonth,
                        timePicker.hour,
                        timePicker.minute,
                        LocalDateTime.now().second
                    )
                    onConfirm(selectedDateTime, lentCheckbox.isChecked)
                    dialog.dismiss()
                }
            }
        }
        dialogInstance.show()
    }

    fun showDeleteDialog(
        context: FragmentActivity,
        onConfirm: () -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.delete_popup) {
            override fun setup() {
                val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)

                buttonConfirm.setOnClickListener {
                    onConfirm()
                    dialog.dismiss()
                }
            }
        }
        dialogInstance.show()
    }

    fun showThemeDialog(
        context: FragmentActivity,
        selectedTheme: Int,
        onThemeSelected: (Int) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.theme_popup) {
            override fun setup() {
                val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)
                val checkboxLightTheme: CheckBox = dialogView.findViewById(R.id.checkbox_light_theme)
                val checkboxDarkTheme: CheckBox = dialogView.findViewById(R.id.checkbox_dark_theme)

                checkboxSystem.isChecked = selectedTheme == 0
                checkboxLightTheme.isChecked = selectedTheme == 1
                checkboxDarkTheme.isChecked = selectedTheme == 2

                fun selectAndClose(
                    theme: Int
                ) {
                    onThemeSelected(theme)
                    this.dialog.dismiss()
                }
                checkboxSystem.setOnClickListener { selectAndClose(0) }
                checkboxLightTheme.setOnClickListener { selectAndClose(1) }
                checkboxDarkTheme.setOnClickListener { selectAndClose(2) }
            }
        }
        dialogInstance.show()
    }

    fun showLanguageDialog(
        context: FragmentActivity,
        selectedLanguage: Int,
        onLanguageSelected: (Int) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.language_popup) {
            override fun setup() {
                val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)
                val checkboxEnglish: CheckBox = dialogView.findViewById(R.id.checkbox_english)
                val checkboxSlovenian: CheckBox = dialogView.findViewById(R.id.checkbox_slovenian)

                checkboxSystem.isChecked = selectedLanguage == 0
                checkboxEnglish.isChecked = selectedLanguage == 1
                checkboxSlovenian.isChecked = selectedLanguage == 2

                fun selectAndClose(
                    language: Int
                ) {
                    onLanguageSelected(language)
                    this.dialog.dismiss()
                }
                checkboxSystem.setOnClickListener { selectAndClose(0) }
                checkboxEnglish.setOnClickListener { selectAndClose(1) }
                checkboxSlovenian.setOnClickListener { selectAndClose(2) }
            }
        }
        dialogInstance.show()
    }

    fun showNotificationsDialog(
        context: FragmentActivity,
        notificationsSettings: NotificationsSettingsEntity,
        onNotificationSettingsSelected: (NotificationsSettingsEntity) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.notifications_popup) {
            override fun setup() {
                val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)
                val checkboxAchievements: CheckBox = dialogView.findViewById(R.id.checkbox_achievements)

                var currentSettings = notificationsSettings.copy()

                checkboxSystem.isChecked = currentSettings.system
                checkboxAchievements.isChecked = currentSettings.achievements

                checkboxSystem.setOnCheckedChangeListener { _, isChecked ->
                    currentSettings = currentSettings.copy(system = isChecked)
                    onNotificationSettingsSelected(currentSettings)
                }

                checkboxAchievements.setOnCheckedChangeListener { _, isChecked ->
                    currentSettings = currentSettings.copy(achievements = isChecked)
                    onNotificationSettingsSelected(currentSettings)
                }
            }
        }
        dialogInstance.show()
    }

    fun showDownloadDialog(
        context: FragmentActivity,
        onDownload: () -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.download_popup) {
            override fun setup() {
                val buttonDownload: Button = dialogView.findViewById(R.id.button_download)

                buttonDownload.setOnClickListener {
                    onDownload()
                    dialog.dismiss()
                }
            }
        }
        dialogInstance.show()
    }

    fun showUploadDialog(
        context: FragmentActivity,
        onOpenFile: () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        onViewCreated: (TextView) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.upload_popup) {
            override fun setup() {
                val textViewSelectedFile: TextView = dialogView.findViewById(R.id.text_selected_file)
                val buttonOpenFile: Button = dialogView.findViewById(R.id.button_open_file)
                val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)

                onViewCreated(textViewSelectedFile)

                textViewSelectedFile.text = String.format(
                    $$"%1$s: %2$s",
                    context.getString(R.string.upload_popup_file),
                    context.getString(R.string.upload_popup_file_none)
                )

                buttonOpenFile.setOnClickListener {
                    onOpenFile()
                }

                buttonConfirm.setOnClickListener {
                    onConfirm()
                    dialog.dismiss()
                }
                dialog.setOnDismissListener { onDismiss() }
            }
        }
        dialogInstance.show()
    }

    fun showAchievementsDialog(
        context: FragmentActivity,
        entry: AchievementEntry
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.achievement_popup) {
            override fun setup() {
                val receivedDate: TextView = dialogView.findViewById(R.id.achievement_achieved_times)
                val achievementImage: ImageView = dialogView.findViewById(R.id.achievement_image)
                val achievementTitle: TextView = dialogView.findViewById(R.id.achievement_title)
                val achievementMessage: TextView = dialogView.findViewById(R.id.achievement_message)

                receivedDate.text = context.resources.getQuantityString(
                    R.plurals.achievement_achieved_times,
                    entry.times.toInt(),
                    entry.times
                )
                achievementImage.setImageResource(entry.image)
                achievementTitle.text = entry.getDisplayText(context)
                achievementMessage.text = entry.message
            }
        }
        dialogInstance.show()
    }

    fun showDatePickerDialog(
        context: FragmentActivity,
        onDateSelected: (Calendar) -> Unit
    ) {
        val selectedDate = Calendar.getInstance()
        val dialogInstance = object : BaseDialog(context, R.layout.dialog_date_picker) {
            override fun setup() {
                val calendarView: CalendarView = dialogView.findViewById(R.id.customCalendarView)
                val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)

                calendarView.date = selectedDate.timeInMillis

                calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                }

                buttonConfirm.setOnClickListener {
                    onDateSelected(selectedDate)
                    dialog.dismiss()
                }
            }
        }
        dialogInstance.show()
    }

    @SuppressLint(value = ["DefaultLocale"])
    fun showResultDialog(
        context: FragmentActivity,
        totalCost: Double,
        totalTimeMinutes: Int,
        totalCigarettes: Int,
        currencyUnit: String,
        formatTime: (Int) -> String
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.calculator_result_popup) {
            override fun setup() {
                val tvTotalCosts: TextView = dialogView.findViewById(R.id.popup_result_total_costs)
                val tvCostPerCigarette: TextView = dialogView.findViewById(R.id.popup_result_cost_per_cigarette)
                val tvAverageCostPerHour: TextView = dialogView.findViewById(R.id.popup_result_average_cost_per_hour)
                val tvTimeSpent: TextView = dialogView.findViewById(R.id.popup_result_time_spent)
                val btnClose: Button = dialogView.findViewById(R.id.button_close_result)

                val averageCostPerCigarette = if (totalCigarettes > 0) totalCost / totalCigarettes else 0.0
                val totalHours = totalTimeMinutes / 60.0
                val averageCostPerHour = if (totalHours > 0) totalCost / totalHours else 0.0

                tvTotalCosts.text = String.format("%.2f %s", totalCost, currencyUnit)
                tvCostPerCigarette.text = String.format("%.3f %s", averageCostPerCigarette, currencyUnit)
                tvAverageCostPerHour.text = String.format("%.2f %s", averageCostPerHour, currencyUnit)
                tvTimeSpent.text = formatTime(totalTimeMinutes)

                btnClose.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
        dialogInstance.show()
    }
}