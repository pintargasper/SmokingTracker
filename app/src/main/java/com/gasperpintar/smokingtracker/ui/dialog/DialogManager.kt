package com.gasperpintar.smokingtracker.ui.dialog

import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.databinding.CalculatorResultPopupBinding
import com.gasperpintar.smokingtracker.databinding.CostContainerBinding
import com.gasperpintar.smokingtracker.databinding.CostsPopupBinding
import com.gasperpintar.smokingtracker.databinding.CurrencyPopupBinding
import com.gasperpintar.smokingtracker.databinding.DeletePopupBinding
import com.gasperpintar.smokingtracker.databinding.DialogDatePickerBinding
import com.gasperpintar.smokingtracker.databinding.DownloadPopupBinding
import com.gasperpintar.smokingtracker.databinding.EditPopupBinding
import com.gasperpintar.smokingtracker.databinding.InsertPopupBinding
import com.gasperpintar.smokingtracker.databinding.LanguagePopupBinding
import com.gasperpintar.smokingtracker.databinding.NotificationsPopupBinding
import com.gasperpintar.smokingtracker.databinding.SaveNotePopupBinding
import com.gasperpintar.smokingtracker.databinding.ThemePopupBinding
import com.gasperpintar.smokingtracker.databinding.UploadPopupBinding
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.bar.LoadingDialog
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatLocalized
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

object DialogManager {

    var TimePicker.is24Hour: Boolean
        get() = is24HourView
        set(value) { setIs24HourView(value) }

    fun showInsertDialog(
        context: FragmentActivity,
        onConfirm: (isLent: Boolean) -> Unit
    ) = BaseDialog.show(context, bindingInflater = InsertPopupBinding::inflate) {
        binding.buttonConfirm.setOnClickListener {
            onConfirm(binding.lentCheckbox.isChecked)
            dismiss()
        }
    }

    fun showEditDialog(
        context: FragmentActivity,
        entry: HistoryEntry,
        onConfirm: (newDateTime: LocalDateTime, isLent: Boolean) -> Unit
    ) = BaseDialog.show(context, bindingInflater = EditPopupBinding::inflate) {
        binding.run {
            lentCheckbox.isChecked = entry.isLent
            timePicker.is24Hour = DateFormat.is24HourFormat(context)

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
                dismiss()
            }
        }
    }

    fun showDeleteDialog(
        context: FragmentActivity,
        onConfirm: () -> Unit
    ) = BaseDialog.show(context, bindingInflater = DeletePopupBinding::inflate) {
        binding.buttonConfirm.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }

    fun showThemeDialog(
        context: FragmentActivity,
        selectedTheme: Int,
        onThemeSelected: (Int) -> Unit
    ) = BaseDialog.show(context, bindingInflater = ThemePopupBinding::inflate) {
        binding.run {
            listOf(checkboxSystem, checkboxLightTheme, checkboxDarkTheme).forEachIndexed { index, checkbox ->
                checkbox.isChecked = selectedTheme == index
                checkbox.setOnClickListener {
                    onThemeSelected(index)
                    dismiss()
                }
            }
        }
    }

    fun showLanguageDialog(
        context: FragmentActivity,
        selectedLanguage: Int,
        onLanguageSelected: (Int) -> Unit
    ) = BaseDialog.show(context, bindingInflater = LanguagePopupBinding::inflate) {
        binding.run {
            listOf(checkboxSystem, checkboxEnglish, checkboxSlovenian, checkboxUkrainian,
                checkboxGerman, checkboxFrench, checkboxSerbianCyrillicScript,
                checkboxSerbianLatinScript, checkboxChineseSimplified).forEachIndexed { index, checkbox ->
                checkbox.isChecked = selectedLanguage == index
                checkbox.setOnClickListener {
                    onLanguageSelected(index)
                    dismiss()
                }
            }
        }
    }

    fun showNotificationsDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        notificationsSettings: NotificationsSettingsEntity,
        onSettingsSelected: (SettingsEntity) -> Unit,
        onNotificationSettingsSelected: (NotificationsSettingsEntity) -> Unit
    ) = BaseDialog.show(context, bindingInflater = NotificationsPopupBinding::inflate) {
        binding.run {
            var currentNotificationSettings = notificationsSettings
            var currentSettings = settings

            fun bindCheckbox(
                checkbox: CheckBox,
                initialValue: Boolean,
                update: (NotificationsSettingsEntity, Boolean) -> NotificationsSettingsEntity
            ) {
                checkbox.isChecked = initialValue
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    currentNotificationSettings = update(currentNotificationSettings, isChecked)
                    onNotificationSettingsSelected(currentNotificationSettings)
                }
            }

            bindCheckbox(checkboxSystem, initialValue = currentNotificationSettings.system) { ns, c -> ns.copy(system = c) }
            bindCheckbox(checkboxProgress, initialValue = currentNotificationSettings.progress) { ns, c -> ns.copy(progress = c) }
            bindCheckbox(checkboxAchievements, initialValue = currentNotificationSettings.achievements) { ns, c -> ns.copy(achievements = c) }

            spinnerProgressFrequency.apply {
                setText(context.resources.getStringArray(R.array.frequency_options)[currentSettings.frequency], false)
                setOnItemClickListener { _, _, position, _ ->
                    currentSettings = currentSettings.copy(frequency = position)
                    onSettingsSelected(currentSettings)
                }
            }
        }
    }

    fun showCurrencyDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onCurrencySelected: (currency: String, customCurrency: String) -> Unit
    ) = BaseDialog.show(context, bindingInflater = CurrencyPopupBinding::inflate) {
        binding.run {
            inputCustomCurrency.setText(settings.customCurrency)

            fun selectAndClose(currencyValue: String) {
                onCurrencySelected(currencyValue, inputCustomCurrency.text.toString().trim())
                dismiss()
            }

            val currencyMap = mapOf(checkboxEuro to "€", checkboxDollar to "$", checkboxPound to "£")
            val activeCheckbox = currencyMap.entries.firstOrNull { it.value == settings.currency }?.key ?: checkboxCustom
            activeCheckbox.isChecked = true

            currencyMap.forEach { (checkbox, value) ->
                checkbox.setOnClickListener {
                    editTextError.visibility = View.GONE
                    selectAndClose(currencyValue = value)
                }
            }

            checkboxCustom.setOnClickListener {
                editTextError.visibility = View.GONE
                val customVal = inputCustomCurrency.text.toString().trim()
                customVal.takeIf { it.isNotEmpty() }?.let(block = ::selectAndClose) ?: run {
                    checkboxCustom.isChecked = false
                    editTextError.visibility = View.VISIBLE
                    inputCustomCurrency.requestFocus()
                }
            }

            inputCustomCurrency.doAfterTextChanged { text ->
                if (!text.isNullOrBlank()) editTextError.visibility = View.GONE
            }

            inputCustomCurrency.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && inputCustomCurrency.text?.isNotBlank() == true) {
                    checkboxCustom.isChecked = true
                    editTextError.visibility = View.GONE
                }
            }
        }
    }

    fun showCostsDialog(
        context: FragmentActivity,
        costs: List<CostEntry>,
        currency: String,
        onDelete: suspend (CostEntry) -> Unit,
        onCostAdded: suspend (CostEntity) -> Unit,
        onRefresh: suspend () -> List<CostEntry>
    ) = BaseDialog.show(context, bindingInflater = CostsPopupBinding::inflate) {
        binding.run {
            var startDate: Calendar? = null
            var endDate: Calendar? = null
            val decimalFormat = DecimalFormat("0.00#")

            lateinit var adapter: Adapter<CostEntry, CostContainerBinding>

            fun formatDate(date: LocalDate): String {
                return when (date) {
                    LocalDate.now() -> context.getString(R.string.day_today)
                    else -> date.formatLocalized()
                }
            }

            suspend fun refreshData() {
                adapter.submitList(onRefresh()) {
                    recyclerviewCostPeriods.scrollToPosition(0)
                }
            }

            adapter = Adapter(
                bindingFactory = CostContainerBinding::inflate,
                onBind = { costEntry ->
                    dateLabel.text = context.getString(
                        R.string.cost_format,
                        formatDate(costEntry.startDate.toLocalDate()),
                        formatDate(costEntry.endDate.toLocalDate())
                    )
                    priceLabel.text = context.getString(
                        R.string.cost_price,
                        decimalFormat.format(costEntry.price),
                        currency
                    )

                    delete.setOnClickListener {
                        context.lifecycleScope.launch {
                            onDelete(costEntry)
                            refreshData()
                        }
                    }
                }
            )
            recyclerviewCostPeriods.apply {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
            }
            adapter.submitList(costs)

            listOf(inputStartDate to true, inputEndDate to false).forEach { (inputField, isStartDate) ->
                inputField.apply {
                    setOnClickListener {
                        showDatePickerDialog(context) { selectedDate ->
                            val (start, end, formattedText) = TimeHelper.applySelectedDate(
                                startDate,
                                endDate,
                                selectedDate = selectedDate,
                                isStartDate = isStartDate
                            )
                            startDate = start
                            endDate = end
                            setText(formattedText)
                        }
                    }
                }
            }

            buttonAddPeriod.setOnClickListener {
                val start = TimeHelper.toLocalDateTime(calendar = startDate ?: Calendar.getInstance())
                val end = TimeHelper.toLocalDateTime(calendar = endDate ?: Calendar.getInstance())

                context.lifecycleScope.launch {
                    onCostAdded(
                        CostEntity(
                            id = 0L,
                            startDate = start,
                            endDate = end.takeIf { it.toLocalDate() != LocalDate.now() }
                                ?: end.toLocalDate().atTime(23, 59, 59),
                            price = inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0
                        )
                    )
                    refreshData()

                    startDate = null
                    endDate = null
                    listOf(inputStartDate, inputEndDate, inputPackPrice).forEach { it.text.clear() }
                }
            }
        }
    }

    fun showBackupDialog(
        context: FragmentActivity,
        onDownload: () -> Unit
    ) = BaseDialog.show(context, bindingInflater = DownloadPopupBinding::inflate) {
        binding.buttonDownload.setOnClickListener {
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
    ) = BaseDialog.show(context, bindingInflater = UploadPopupBinding::inflate) {
        binding.run {
            onViewCreated(textSelectedFile)

            textSelectedFile.text = context.getString(
                R.string.restore_popup_file,
                context.getString(R.string.restore_popup_file_none)
            )

            buttonOpenFile.setOnClickListener { onOpenFile() }
            buttonConfirm.setOnClickListener {
                if (textSelectedFile.tag is Uri) {
                    onConfirm()
                    dismiss()
                }
            }
        }

        dialog.setOnDismissListener {
            onDismiss()
        }
    }

    fun showDatePickerDialog(
        context: FragmentActivity,
        onDateSelected: (Calendar) -> Unit
    ) = BaseDialog.show(context, bindingInflater = DialogDatePickerBinding::inflate) {
        binding.run {
            val selectedDate = Calendar.getInstance()

            customCalendarView.date = selectedDate.timeInMillis
            customCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
            }

            buttonConfirm.setOnClickListener {
                onDateSelected(selectedDate)
                dismiss()
            }
        }
    }

    fun showResultDialog(
        context: FragmentActivity,
        totalCost: Double,
        totalTimeMinutes: Int,
        totalCigarettes: Int,
        currencyUnit: String,
        formatTime: (Int) -> String
    ) = BaseDialog.show(context, bindingInflater = CalculatorResultPopupBinding::inflate) {
        binding.run {
            val averageCostPerCigarette = totalCigarettes.takeIf { it > 0 }?.let { totalCost / it } ?: 0.0
            val totalHours = totalTimeMinutes / 60.0
            val averageCostPerHour = totalHours.takeIf { it > 0 }?.let { totalCost / it } ?: 0.0

            val decimalFormatTwo = DecimalFormat("0.00")
            val decimalFormatThree = DecimalFormat("0.000")

            fun formatPrice(amount: Double, format: DecimalFormat): String {
                return context.getString(R.string.cost_price, format.format(amount), currencyUnit)
            }

            popupResultTotalCosts.text = formatPrice(amount = totalCost, format = decimalFormatTwo)
            popupResultCostPerCigarette.text = formatPrice(amount = averageCostPerCigarette, format = decimalFormatThree)
            popupResultAverageCostPerHour.text = formatPrice(amount = averageCostPerHour, format = decimalFormatTwo)
            popupResultTimeSpent.text = formatTime(totalTimeMinutes)
        }
    }

    fun showLoadingDialog(
        context: FragmentActivity
    ): LoadingDialog = LoadingDialog(context).apply {
        show()
    }

    fun showSaveNoteDialog(
        context: FragmentActivity,
        onSave: () -> Unit,
        onClose: () -> Unit = {}
    ) = BaseDialog.show(context, bindingInflater = SaveNotePopupBinding::inflate) {
        binding.run {
            save.setOnClickListener {
                dismiss()
                onSave()
            }

            close.setOnClickListener {
                dismiss()
                onClose()
            }
        }
    }
}