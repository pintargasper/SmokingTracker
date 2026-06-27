package com.gasperpintar.smokingtracker.ui.dialog

import android.annotation.SuppressLint
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.Adapter
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.model.CostEntry
import com.gasperpintar.smokingtracker.model.HistoryEntry
import com.gasperpintar.smokingtracker.repository.CostsRepository
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

                checkboxSystem.setOnClickListener {
                    selectAndClose(0)
                }

                checkboxLightTheme.setOnClickListener {
                    selectAndClose(1)
                }

                checkboxDarkTheme.setOnClickListener {
                    selectAndClose(2)
                }
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

                fun selectAndClose(language: Int) {
                    onLanguageSelected(language)
                    dialog.dismiss()
                }

                for ((index, checkboxId) in languageCheckboxes) {
                    val checkbox: CheckBox = dialogView.findViewById(checkboxId)
                    checkbox.isChecked = selectedLanguage == index
                    checkbox.setOnClickListener {
                        selectAndClose(language = index)
                    }
                }
            }
        }
        dialogInstance.show()
    }

    fun showNotificationsDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        notificationsSettings: NotificationsSettingsEntity,
        onSettingsSelected: (SettingsEntity) -> Unit,
        onNotificationSettingsSelected: (NotificationsSettingsEntity) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.notifications_popup) {
            override fun setup() {
                val checkboxSystem: CheckBox = dialogView.findViewById(R.id.checkbox_system)
                val checkboxProgress: CheckBox = dialogView.findViewById(R.id.checkbox_progress)
                val checkboxAchievements: CheckBox = dialogView.findViewById(R.id.checkbox_achievements)
                val frequency: AutoCompleteTextView = dialogView.findViewById(R.id.spinner_progress_frequency)

                var currentNotificationSettings = notificationsSettings.copy()
                var currentSettings = settings.copy()

                checkboxSystem.isChecked = currentNotificationSettings.system
                checkboxProgress.isChecked = currentNotificationSettings.progress
                checkboxAchievements.isChecked = currentNotificationSettings.achievements

                frequency.setText(
                    context.resources.getStringArray(R.array.frequency_options)[currentSettings.frequency],
                    false
                )

                checkboxSystem.setOnCheckedChangeListener { _, isChecked ->
                    currentNotificationSettings = currentNotificationSettings.copy(system = isChecked)
                    onNotificationSettingsSelected(currentNotificationSettings)
                }

                checkboxProgress.setOnCheckedChangeListener { _, isChecked ->
                    currentNotificationSettings = currentNotificationSettings.copy(progress = isChecked)
                    onNotificationSettingsSelected(currentNotificationSettings)
                }

                checkboxAchievements.setOnCheckedChangeListener { _, isChecked ->
                    currentNotificationSettings = currentNotificationSettings.copy(achievements = isChecked)
                    onNotificationSettingsSelected(currentNotificationSettings)
                }

                frequency.setOnItemClickListener { _, _, position, _ ->
                    currentSettings = currentSettings.copy(frequency = position)
                    onSettingsSelected(currentSettings)
                }
            }
        }
        dialogInstance.show()
    }

    fun showCurrencyDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onCurrencySelected: (String, String) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.currency_popup) {
            override fun setup() {
                val languageCheckboxes = listOf(
                    0 to R.id.checkbox_euro,
                    1 to R.id.checkbox_dollar,
                    2 to R.id.checkbox_pound,
                    3 to R.id.checkbox_custom
                )

                val currencyValues: Map<Int, String> = mapOf(
                    0 to "€",
                    1 to "$",
                    2 to "£"
                )

                val customInput: EditText = dialogView.findViewById(R.id.input_custom_currency)
                val errorTextView: TextView = dialogView.findViewById(R.id.edit_text_error)

                fun selectAndClose(currencyValue: String) {
                    onCurrencySelected(currencyValue, customInput.text.toString())
                    dialog.dismiss()
                }

                val currencyToIndex: Map<String, Int> = currencyValues.entries.associate {
                    it.value to it.key
                }

                for ((index, checkboxId) in languageCheckboxes) {
                    val checkbox: CheckBox = dialogView.findViewById(checkboxId)
                    checkbox.isChecked = index == (currencyToIndex[settings.currency] ?: 3)
                    customInput.setText(settings.customCurrency)

                    checkbox.setOnClickListener {
                        errorTextView.visibility = View.GONE
                        if (index == 3) {
                            val customValue = customInput.text.toString().trim()
                            if (customValue.isNotEmpty()) {
                                selectAndClose(currencyValue = customValue)
                            } else {
                                checkbox.isChecked = false
                                errorTextView.visibility = View.VISIBLE
                                customInput.requestFocus()
                            }
                        } else {
                            selectAndClose(currencyValue = currencyValues[index] ?: "€")
                        }
                    }
                }

                customInput.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        if (!editable.isNullOrBlank()) {
                            errorTextView.visibility = View.GONE
                        }
                    }
                    override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                val customCheckbox: CheckBox = dialogView.findViewById(R.id.checkbox_custom)
                customInput.setOnFocusChangeListener { _, _ ->
                    if (customInput.text.toString().trim().isNotEmpty()) {
                        customCheckbox.isChecked = true
                        errorTextView.visibility = View.GONE
                    }
                }
            }
        }
        dialogInstance.show()
    }

    @SuppressLint(value = ["DefaultLocale"])
    fun showCostsDialog(
        context: FragmentActivity,
        costsRepository: CostsRepository,
        currency: String
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.costs_popup) {
            override fun setup() {
                val packPrice: EditText = dialogView.findViewById(R.id.input_pack_price)
                val inputStartDate: EditText = dialogView.findViewById(R.id.input_start_date)
                val inputEndDate: EditText = dialogView.findViewById(R.id.input_end_date)
                val buttonAddPeriod: Button = dialogView.findViewById(R.id.button_add_period)
                val costPeriods: RecyclerView = dialogView.findViewById(R.id.recyclerview_cost_periods)

                var startDate: Calendar? = null
                var endDate: Calendar? = null

                val decimalFormat = DecimalFormat("0.00#")

                lateinit var adapter: Adapter<CostEntry>

                suspend fun loadData() {
                    val refreshed = costsRepository.getAll().map(transform = CostEntry::fromEntity)
                    adapter.submitList(refreshed)
                }

                adapter = Adapter(
                    layoutId = R.layout.cost_container,
                    onBind = { itemView, costEntry ->
                        val textPeriod: TextView = itemView.findViewById(R.id.date_label)
                        val price: TextView = itemView.findViewById(R.id.price_label)
                        val delete: ImageButton = itemView.findViewById(R.id.delete)

                        textPeriod.text = String.format(
                            "%s - %s",
                            when (val startDate = costEntry.startDate.toLocalDate()) {
                                LocalDate.now() -> itemView.context.getString(R.string.day_today)
                                else -> LocalizationHelper.formatDate(startDate)
                            },
                            when (val endDate = costEntry.endDate.toLocalDate()) {
                                LocalDate.now() -> itemView.context.getString(R.string.day_today)
                                else -> LocalizationHelper.formatDate(endDate)
                            }
                        )

                        price.text = itemView.context.getString(
                            R.string.cost_container_price,
                            decimalFormat.format(costEntry.price),
                            currency
                        )

                        delete.setOnClickListener {
                            context.lifecycleScope.launch {
                                costsRepository.delete(entry = costEntry.toEntity())
                                loadData()
                            }
                        }
                    },
                    diffCallback = object : DiffUtil.ItemCallback<CostEntry>() {
                        override fun areItemsTheSame(oldItem: CostEntry, newItem: CostEntry) = oldItem.id == newItem.id
                        override fun areContentsTheSame(oldItem: CostEntry, newItem: CostEntry) = oldItem == newItem
                    }
                )

                costPeriods.layoutManager = LinearLayoutManager(context)
                costPeriods.adapter = adapter
                context.lifecycleScope.launch {
                    loadData()
                }

                inputStartDate.setOnClickListener {
                    showDatePickerDialog(context) { date ->
                        val (start, end, text) = TimeHelper.applySelectedDate(startDate, endDate, date, true)
                        startDate = start
                        endDate = end
                        inputStartDate.setText(text)
                    }
                }

                inputEndDate.setOnClickListener {
                    showDatePickerDialog(context) { date ->
                        val (start, end, text) = TimeHelper.applySelectedDate(startDate, endDate, date, false)
                        startDate = start
                        endDate = end
                        inputEndDate.setText(text)
                    }
                }

                buttonAddPeriod.setOnClickListener {
                    context.lifecycleScope.launch {
                        val safeStart = startDate ?: Calendar.getInstance()
                        val safeEnd = endDate ?: Calendar.getInstance()

                        costsRepository.insert(
                            entry = CostEntity(
                                id = 0L,
                                startDate = TimeHelper.toLocalDateTime(calendar = safeStart),
                                endDate = TimeHelper.toLocalDateTime(calendar = safeEnd),
                                price = packPrice.text.toString().toDoubleOrNull() ?: 0.0
                            )
                        )
                        loadData()
                    }
                }
            }
        }
        dialogInstance.show()
    }

    fun showBackupDialog(
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

    fun showRestoreDialog(
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

                dialog.setOnDismissListener {
                    onDismiss()
                }
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

    fun showVersionDialog(
        context: FragmentActivity,
        onLinkClicked: (LinearLayout, String) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.version_popup) {
            override fun setup() {
                val links = listOf(
                    Triple(R.id.github_website_layout, R.id.github_website_service_url, "https://github.com/pintargasper/SmokingTracker/releases/latest"),
                    Triple(R.id.f_droid_website_layout, R.id.f_droid_website_service_url, "https://f-droid.org/packages/com.gasperpintar.smokingtracker"),
                    Triple(R.id.izzy_on_droid_website_layout, R.id.izzy_on_droid_website_service_url, "https://apt.izzysoft.de/fdroid/index/apk/com.gasperpintar.smokingtracker"),
                    Triple(R.id.open_apk_website_layout, R.id.open_apk_website_service_url, "https://www.openapk.net/smoking-tracker/com.gasperpintar.smokingtracker/")
                )

                for ((layoutId, textViewId, url) in links) {
                    val layout: LinearLayout = dialogView.findViewById(layoutId)
                    val textView: TextView = dialogView.findViewById(textViewId)
                    textView.text = url
                    layout.setOnClickListener {
                        onLinkClicked(layout, url)
                    }
                }
            }
        }
        dialogInstance.show()
    }

    fun showContributorsDialog(
        context: FragmentActivity,
        onLinkClicked: (LinearLayout, String) -> Unit
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.contributors_popup) {
            override fun setup() {
                val links = listOf(
                    Pair(R.id.contributor_1_layout, "https://github.com/pintargasper"),
                    Pair(R.id.contributor_2_layout, "https://github.com/mrtaxi"),
                    Pair(R.id.contributor_3_layout, "https://github.com/jocixlinux-sys"),
                    Pair(R.id.contributor_4_layout, "https://github.com/iaanneed"),
                    Pair(R.id.contributor_5_layout, "https://github.com/ywnzzl")
                )

                for ((layoutId, url) in links) {
                    val layout: LinearLayout = dialogView.findViewById(layoutId)
                    layout.setOnClickListener {
                        onLinkClicked(layout, url)
                    }
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

                val averageCostPerCigarette = if (totalCigarettes > 0) totalCost / totalCigarettes else 0.0
                val totalHours = totalTimeMinutes / 60.0
                val averageCostPerHour = if (totalHours > 0) totalCost / totalHours else 0.0

                tvTotalCosts.text = String.format("%.2f %s", totalCost, currencyUnit)
                tvCostPerCigarette.text = String.format("%.3f %s", averageCostPerCigarette, currencyUnit)
                tvAverageCostPerHour.text = String.format("%.2f %s", averageCostPerHour, currencyUnit)
                tvTimeSpent.text = formatTime(totalTimeMinutes)
            }
        }
        dialogInstance.show()
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
    ) {
        val dialogInstance = object : BaseDialog(context, R.layout.save_note_popup) {
            override fun setup() {
                val buttonConfirm: Button = dialogView.findViewById(R.id.button_save)
                val buttonClose: Button = dialogView.findViewById(R.id.button_close)

                buttonConfirm.setOnClickListener {
                    dialog.dismiss()
                    onSave()
                }

                buttonClose.setOnClickListener {
                    dialog.dismiss()
                    onClose()
                }
            }
        }
        dialogInstance.show()
    }
}