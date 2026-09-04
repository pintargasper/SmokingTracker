package com.gasperpintar.smokingtracker.ui.dialog

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.ui.bar.LoadingDialog
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

object DialogManager {

    fun showInsertDialog(
        context: FragmentActivity,
        onConfirm: (isLent: Boolean) -> Unit
    ) = showDialog(context, layout = R.layout.insert_popup) {
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val lentCheckbox: CheckBox = dialogView.findViewById(R.id.lent_checkbox)

        buttonConfirm.setOnClickListener {
            onConfirm(lentCheckbox.isChecked)
            dismiss()
        }
    }

    fun showEditDialog(
        context: FragmentActivity,
        entry: HistoryEntry,
        onConfirm: (newDateTime: LocalDateTime, isLent: Boolean) -> Unit
    ) = showDialog(context, layout = R.layout.edit_popup) {
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val lentCheckbox: CheckBox = dialogView.findViewById(R.id.lent_checkbox)
        val datePicker: DatePicker = dialogView.findViewById(R.id.date_picker)
        val timePicker: TimePicker = dialogView.findViewById(R.id.time_picker)

        lentCheckbox.isChecked = entry.isLent
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))

        entry.createdAt.let { dateTime ->
            datePicker.updateDate(
                dateTime.year,
                dateTime.monthValue - 1,
                dateTime.dayOfMonth
            )
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

            onConfirm(
                selectedDateTime,
                lentCheckbox.isChecked
            )
            dismiss()
        }
    }

    fun showDeleteDialog(
        context: FragmentActivity,
        onConfirm: () -> Unit
    ) = showDialog(context, layout = R.layout.delete_popup) {

        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)

        buttonConfirm.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }

    fun showThemeDialog(
        context: FragmentActivity,
        selectedTheme: Int,
        onThemeSelected: (Int) -> Unit
    ) = showDialog(
        context,
        layout = R.layout.theme_popup
    ) {

        val themeCheckboxes = listOf(
            0 to R.id.checkbox_system,
            1 to R.id.checkbox_light_theme,
            2 to R.id.checkbox_dark_theme
        )

        themeCheckboxes.forEach { (index, checkboxId) ->
            val checkbox: CheckBox = dialogView.findViewById(checkboxId)
            checkbox.isChecked = selectedTheme == index
            checkbox.setOnClickListener {
                onThemeSelected(index)
                dismiss()
            }
        }
    }

    fun showLanguageDialog(
        context: FragmentActivity,
        selectedLanguage: Int,
        onLanguageSelected: (Int) -> Unit
    ) = showDialog(context, layout = R.layout.language_popup) {

        val languageCheckboxes = listOf(
            0 to R.id.checkbox_system,
            1 to R.id.checkbox_english,
            2 to R.id.checkbox_slovenian,
            3 to R.id.checkbox_ukrainian,
            4 to R.id.checkbox_german,
            5 to R.id.checkbox_french,
            6 to R.id.checkbox_serbian_cyrillic_script,
            7 to R.id.checkbox_serbian_latin_script,
            8 to R.id.checkbox_chinese_simplified
        )

        languageCheckboxes.forEach { (index, checkboxId) ->
            val checkbox: CheckBox = dialogView.findViewById(checkboxId)
            checkbox.isChecked = selectedLanguage == index
            checkbox.setOnClickListener {
                onLanguageSelected(index)
                dismiss()
            }
        }
    }

    fun showNotificationsDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        notificationsSettings: NotificationsSettingsEntity,
        onSettingsSelected: (SettingsEntity) -> Unit,
        onNotificationSettingsSelected: (NotificationsSettingsEntity) -> Unit
    ) = showDialog(context, layout = R.layout.notifications_popup) {

        val notificationCheckboxes = listOf(
            0 to R.id.checkbox_system,
            1 to R.id.checkbox_progress,
            2 to R.id.checkbox_achievements
        )

        val frequency: AutoCompleteTextView = dialogView.findViewById(R.id.spinner_progress_frequency)

        var currentNotificationSettings = notificationsSettings.copy()
        var currentSettings = settings.copy()

        fun updateNotificationSettings(
            update: (NotificationsSettingsEntity) -> NotificationsSettingsEntity
        ) {
            currentNotificationSettings = update(currentNotificationSettings)
            onNotificationSettingsSelected(currentNotificationSettings)
        }

        notificationCheckboxes.forEach { (index, checkboxId) ->
            val checkbox: CheckBox = dialogView.findViewById(checkboxId)

            checkbox.isChecked = when (index) {
                0 -> currentNotificationSettings.system
                1 -> currentNotificationSettings.progress
                2 -> currentNotificationSettings.achievements
                else -> false
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                updateNotificationSettings {
                    when (index) {
                        0 -> it.copy(system = isChecked)
                        1 -> it.copy(progress = isChecked)
                        2 -> it.copy(achievements = isChecked)
                        else -> it
                    }
                }
            }
        }

        frequency.setText(
            context.resources.getStringArray(R.array.frequency_options)[currentSettings.frequency],
            false
        )

        frequency.setOnItemClickListener { _, _, position, _ ->
            currentSettings = currentSettings.copy(frequency = position)
            onSettingsSelected(currentSettings)
        }
    }

    fun showCurrencyDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onCurrencySelected: (String, String) -> Unit
    ) = showDialog(context, R.layout.currency_popup) {

        val currencyCheckboxes = listOf(
            0 to R.id.checkbox_euro,
            1 to R.id.checkbox_dollar,
            2 to R.id.checkbox_pound,
            3 to R.id.checkbox_custom
        )

        val currencyValues = mapOf(
            0 to "€",
            1 to "$",
            2 to "£"
        )

        val customInput: EditText = dialogView.findViewById(R.id.input_custom_currency)
        val errorTextView: TextView = dialogView.findViewById(R.id.edit_text_error)
        val customCheckbox: CheckBox = dialogView.findViewById(R.id.checkbox_custom)

        customInput.setText(settings.customCurrency)

        fun selectAndClose(currencyValue: String) {
            onCurrencySelected(currencyValue, customInput.text.toString())
            dismiss()
        }

        val selectedCurrencyIndex = currencyValues.entries
            .firstOrNull {
                it.value == settings.currency
            }?.key ?: 3

        currencyCheckboxes.forEach { (index, checkboxId) ->
            val checkbox: CheckBox = dialogView.findViewById(checkboxId)

            checkbox.isChecked = index == selectedCurrencyIndex
            checkbox.setOnClickListener {
                errorTextView.visibility = View.GONE

                when (index) {
                    3 -> {
                        customInput.text.toString().trim().takeIf {
                            it.isNotEmpty()
                        } ?.let(block = ::selectAndClose) ?: run {
                            checkbox.isChecked = false
                            errorTextView.visibility = View.VISIBLE
                            customInput.requestFocus()
                        }
                    }

                    else -> {
                        selectAndClose(currencyValues[index] ?: "€")
                    }
                }
            }
        }

        customInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (!editable.isNullOrBlank()) {
                    errorTextView.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        customInput.setOnFocusChangeListener { _, _ ->
            customInput.text.toString().trim().takeIf {
                it.isNotEmpty()
            } ?.let {
                customCheckbox.isChecked = true
                errorTextView.visibility = View.GONE
            }
        }
    }

    @SuppressLint(value = ["DefaultLocale"])
    fun showCostsDialog(
        context: FragmentActivity,
        costs: List<CostEntry>,
        currency: String,
        onDelete: suspend (CostEntry) -> Unit,
        onCostAdded: suspend (CostEntity) -> Unit,
        onRefresh: suspend () -> List<CostEntry>
    ) = showDialog(context, layout = R.layout.costs_popup) {
        val packPrice: EditText = dialogView.findViewById(R.id.input_pack_price)
        val inputStartDate: EditText = dialogView.findViewById(R.id.input_start_date)
        val inputEndDate: EditText = dialogView.findViewById(R.id.input_end_date)
        val buttonAddPeriod: Button = dialogView.findViewById(R.id.button_add_period)
        val costPeriods: RecyclerView = dialogView.findViewById(R.id.recyclerview_cost_periods)

        var startDate: Calendar? = null
        var endDate: Calendar? = null

        val decimalFormat = DecimalFormat("0.00#")

        lateinit var adapter: Adapter<CostEntry>

        fun formatDate(date: LocalDate): String {
            return when (date) {
                LocalDate.now() -> context.getString(R.string.day_today)
                else -> LocalizationHelper.formatDate(date)
            }
        }

        suspend fun refreshAdapter() {
            adapter.submitList(onRefresh())
            costPeriods.scrollToPosition(0)
        }

        adapter = Adapter(
            layoutId = R.layout.cost_container,
            onBind = { itemView, costEntry ->
                val textPeriod: TextView = itemView.findViewById(R.id.date_label)
                val price: TextView = itemView.findViewById(R.id.price_label)
                val delete: ImageButton = itemView.findViewById(R.id.delete)

                textPeriod.text = buildString {
                    append(formatDate(costEntry.startDate.toLocalDate()))
                    append(" - ")
                    append(formatDate(costEntry.endDate.toLocalDate()))
                }

                price.text = itemView.context.getString(
                    R.string.cost_price,
                    decimalFormat.format(costEntry.price),
                    currency
                )

                delete.setOnClickListener {
                    context.lifecycleScope.launch {
                        onDelete(costEntry)
                        refreshAdapter()
                    }
                }
            }
        )

        costPeriods.apply {
            layoutManager = LinearLayoutManager(context)
            this@apply.adapter = adapter
        }

        adapter.submitList(costs)

        inputStartDate.setOnClickListener {
            showDatePickerDialog(context) { date ->
                TimeHelper.applySelectedDate(
                    startDate,
                    endDate,
                    selectedDate = date,
                    isStartDate = true
                ).let { (start, end, text) ->
                    startDate = start
                    endDate = end
                    inputStartDate.setText(text)
                }
            }
        }

        inputEndDate.setOnClickListener {
            showDatePickerDialog(context) { date ->
                TimeHelper.applySelectedDate(
                    startDate,
                    endDate,
                    selectedDate = date,
                    isStartDate = false
                ).let { (start, end, text) ->
                    startDate = start
                    endDate = end
                    inputEndDate.setText(text)
                }
            }
        }

        buttonAddPeriod.setOnClickListener {
            val start = TimeHelper.toLocalDateTime(calendar = startDate ?: Calendar.getInstance())
            val end = TimeHelper.toLocalDateTime(calendar = endDate ?: Calendar.getInstance())

            val adjustedEnd = end.takeIf {
                it.toLocalDate() != LocalDate.now()
            } ?: end.toLocalDate().atTime(23, 59, 59)

            context.lifecycleScope.launch {
                onCostAdded(
                    CostEntity(
                        id = 0L,
                        startDate = start,
                        endDate = adjustedEnd,
                        price = packPrice.text.toString().toDoubleOrNull() ?: 0.0
                    )
                )
                refreshAdapter()

                startDate = null
                endDate = null
                inputStartDate.text.clear()
                inputEndDate.text.clear()
                packPrice.text.clear()
            }
        }
    }

    fun showBackupDialog(
        context: FragmentActivity,
        onDownload: () -> Unit
    ) = showDialog(context, layout = R.layout.download_popup) {
        val buttonDownload: Button = dialogView.findViewById(R.id.button_download)
        buttonDownload.setOnClickListener {
            onDownload()
            dismiss()
        }
    }

    fun showRestoreDialog(
        context: FragmentActivity,
        onOpenFile: () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        onViewCreated: (TextView) -> Unit
    ) = showDialog(context, layout = R.layout.upload_popup) {
        val textViewSelectedFile: TextView = dialogView.findViewById(R.id.text_selected_file)
        val buttonOpenFile: Button = dialogView.findViewById(R.id.button_open_file)
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)

        onViewCreated(textViewSelectedFile)

        textViewSelectedFile.text = context.getString(R.string.restore_popup_file, context.getString(R.string.restore_popup_file_none))

        buttonOpenFile.setOnClickListener {
            onOpenFile()
        }

        buttonConfirm.setOnClickListener {
            val selectedUri = textViewSelectedFile.tag as? Uri
            if (selectedUri != null) {
                onConfirm()
                dismiss()
            }
        }

        dialog.setOnDismissListener {
            onDismiss()
        }
    }

    fun showDatePickerDialog(
        context: FragmentActivity,
        onDateSelected: (Calendar) -> Unit
    ) = showDialog(context, layout = R.layout.dialog_date_picker) {
        val selectedDate = Calendar.getInstance()
        val calendarView: CalendarView = dialogView.findViewById(R.id.customCalendarView)
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)

        calendarView.date = selectedDate.timeInMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        }

        buttonConfirm.setOnClickListener {
            onDateSelected(selectedDate)
            dismiss()
        }
    }

    @SuppressLint(value = ["DefaultLocale"])
    fun showResultDialog(
        context: FragmentActivity,
        totalCost: Double,
        totalTimeMinutes: Int,
        totalCigarettes: Int,
        currencyUnit: String,
        formatTime: (Int) -> String
    ) = showDialog(context, layout = R.layout.calculator_result_popup) {
        val tvTotalCosts: TextView = dialogView.findViewById(R.id.popup_result_total_costs)
        val tvCostPerCigarette: TextView = dialogView.findViewById(R.id.popup_result_cost_per_cigarette)
        val tvAverageCostPerHour: TextView = dialogView.findViewById(R.id.popup_result_average_cost_per_hour)
        val tvTimeSpent: TextView = dialogView.findViewById(R.id.popup_result_time_spent)

        val averageCostPerCigarette = totalCigarettes.takeIf { it > 0 } ?.let {
            totalCost / it
        } ?: 0.0

        val totalHours = totalTimeMinutes / 60.0

        val averageCostPerHour = totalHours.takeIf { it > 0 } ?.let {
            totalCost / it
        } ?: 0.0

        tvTotalCosts.text = String.format("%.2f %s", totalCost, currencyUnit)
        tvCostPerCigarette.text = String.format("%.3f %s", averageCostPerCigarette, currencyUnit)
        tvAverageCostPerHour.text = String.format("%.2f %s", averageCostPerHour, currencyUnit)
        tvTimeSpent.text = formatTime(totalTimeMinutes)
    }

    fun showLoadingDialog(
        context: FragmentActivity
    ): LoadingDialog {
        val dialog = LoadingDialog(context)
        dialog.show()
        return dialog
    }

    fun showSaveNoteDialog(
        context: FragmentActivity,
        onSave: () -> Unit,
        onClose: () -> Unit = {}
    ) = showDialog(context, layout = R.layout.save_note_popup) {
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_save)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)

        buttonConfirm.setOnClickListener {
            dismiss()
            onSave()
        }

        buttonClose.setOnClickListener {
            dismiss()
            onClose()
        }
    }

    private inline fun showDialog(
        context: FragmentActivity,
        layout: Int,
        crossinline set: BaseDialog.() -> Unit
    ) {
        object : BaseDialog(activity = context, layoutResource = layout) {
            override fun setup() = set()
        }.show()
    }
}