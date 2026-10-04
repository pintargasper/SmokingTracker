package com.gasperpintar.smokingtracker.ui.dialog

import android.net.Uri
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
import com.gasperpintar.smokingtracker.databinding.ContainerCostBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentChangelogBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentCostsBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentCurrencyBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentDatePickerBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentEditBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentEndDayBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentInsertBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentLanguageBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentNotificationsBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentRestoreBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentResultBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentThemeBinding
import com.gasperpintar.smokingtracker.databinding.DialogContentWidgetsBinding
import com.gasperpintar.smokingtracker.type.Widget
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.bar.LoadingDialog
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper.toLocalDate
import com.gasperpintar.smokingtracker.utils.TimeHelper.toLocalTime
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.linkify.LinkifyPlugin
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
        onSuccess: (Boolean) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentInsertBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.message.isGone = true
            binding.title.setText(R.string.insert_popup)
            binding.dialogConfirm.isVisible = true
            binding.confirm.setText(R.string.popup_add)
            binding.confirm.setOnClickListener {
                onSuccess(lentCheckbox.isChecked)
                dismiss()
            }
        }
    }

    fun showEditDialog(
        context: FragmentActivity,
        entry: HistoryEntry,
        onSuccess: (LocalDateTime, Boolean) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentEditBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            lentCheckbox.isChecked = entry.isLent
            timePicker.is24Hour = DateFormat.is24HourFormat(context)

            entry.createdAt.let { dateTime ->
                datePicker.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
                timePicker.hour = dateTime.hour
                timePicker.minute = dateTime.minute
            }

            binding.message.isGone = true
            binding.title.setText(R.string.edit_popup_message)
            binding.dialogConfirm.isVisible = true
            binding.confirm.setOnClickListener {
                val selectedDateTime = LocalDateTime.of(datePicker.toLocalDate(), timePicker.toLocalTime())
                    .withSecond(entry.createdAt.second)

                onSuccess(selectedDateTime, lentCheckbox.isChecked)
                dismiss()
            }
        }
    }

    fun showDeleteDialog(
        context: FragmentActivity,
        onSuccess: () -> Unit
    ) = BaseDialog.show(context = context) {
        binding.message.isGone = true
        binding.title.setText(R.string.delete_popup_message)
        binding.dialogConfirm.isVisible = true
        binding.confirm.setOnClickListener {
            onSuccess()
            dismiss()
        }
    }

    fun showThemeDialog(
        context: FragmentActivity,
        selectedTheme: Int,
        onSuccess: (Int) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentThemeBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.theme_popup)
            binding.message.setText(R.string.theme_popup_info)

            listOf(checkboxSystem, checkboxLightTheme, checkboxDarkTheme).forEachIndexed { index, checkbox ->
                checkbox.isChecked = selectedTheme == index
                checkbox.setOnClickListener {
                    onSuccess(index)
                    dismiss()
                }
            }
        }
    }

    fun showLanguageDialog(
        context: FragmentActivity,
        selectedLanguage: String,
        onLanguageSelected: (String) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentLanguageBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.language_popup)
            binding.message.setText(R.string.language_popup_info)

            val languages = context.resources.getStringArray(R.array.language_values)
            listOf(
                checkboxSystem, checkboxGerman, checkboxEnglish, checkboxFrench, checkboxHungarian,
                checkboxSlovenian, checkboxSerbianCyrillicScript, checkboxSerbianLatinScript,
                checkboxUkrainian, checkboxChineseTraditional, checkboxChineseSimplified
            ).forEachIndexed { index, checkbox ->
                checkbox.isChecked = selectedLanguage == languages[index]
                checkbox.setOnClickListener {
                    onLanguageSelected(languages[index])
                    dismiss()
                }
            }
        }
    }

    fun showNotificationsDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        notificationsSettings: NotificationsSettingsEntity,
        onSuccess: (SettingsEntity, NotificationsSettingsEntity) -> Unit,
    ) = BaseDialog.show(context = context) {
        DialogContentNotificationsBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.notifications_popup)
            binding.message.setText(R.string.notifications_popup_info)

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
                    onSuccess(currentSettings, currentNotificationSettings)
                }
            }

            bindCheckbox(checkboxSystem, currentNotificationSettings.system) { ns, c -> ns.copy(system = c) }
            bindCheckbox(checkboxProgress, currentNotificationSettings.progress) { ns, c -> ns.copy(progress = c) }
            bindCheckbox(checkboxAchievements, currentNotificationSettings.achievements) { ns, c -> ns.copy(achievements = c) }

            spinnerProgressFrequency.apply {
                setText(context.resources.getStringArray(R.array.frequency_options)[currentSettings.frequency], false)
                setOnItemClickListener { _, _, position, _ ->
                    currentSettings = currentSettings.copy(frequency = position)
                    onSuccess(currentSettings, currentNotificationSettings)
                }
            }
        }
    }

    fun showEndDayDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onSuccess: (Int) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentEndDayBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            timePicker.is24Hour = DateFormat.is24HourFormat(context)
            timePicker.hour = settings.dayEndMinutes / 60
            timePicker.minute = settings.dayEndMinutes % 60

            binding.title.setText(R.string.settings_general_day_end_hour)
            binding.message.setText(R.string.settings_general_day_end_hour_description)
            binding.dialogConfirm.isVisible = true
            binding.confirm.setOnClickListener {
                onSuccess(timePicker.hour * 60 + timePicker.minute)
                dismiss()
            }
        }
    }

    fun showWidgetsDialog(
        context: FragmentActivity,
        onSuccess: (Widget) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentWidgetsBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.settings_general_widgets)
            binding.message.setText(R.string.settings_general_widgets_description)

            listOf(widget1 to Widget.ONLY_QUICK_ADD, widget2 to Widget.QUICK_ADD, widget3 to Widget.STATS, widget4 to Widget.STATS_QUICK_ADD).forEach { (button, widget) ->
                button.setOnClickListener {
                    onSuccess(widget)
                    dismiss()
                }
            }
        }
    }

    fun showCurrencyDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onSuccess: (String, String) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentCurrencyBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.currency_popup)
            binding.message.setText(R.string.currency_popup_info)
            inputCustomCurrency.setText(settings.customCurrency)

            fun select(currencyValue: String) {
                onSuccess(currencyValue, inputCustomCurrency.text.toString().trim())
                dismiss()
            }

            val currencyMap = mapOf(checkboxEuro to "€", checkboxDollar to "$", checkboxPound to "£")
            val activeCheckbox = currencyMap.entries.firstOrNull { it.value == settings.currency }?.key ?: checkboxCustom

            activeCheckbox.isChecked = true
            currencyMap.forEach { (checkbox, value) ->
                checkbox.setOnClickListener {
                    editTextError.visibility = View.GONE
                    select(currencyValue = value)
                }
            }

            checkboxCustom.setOnClickListener {
                editTextError.visibility = View.GONE
                inputCustomCurrency.text.toString().trim().takeIf { it.isNotEmpty() }?.let(block = ::select) ?: run {
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
        currency: String,
        onSuccess: suspend (CostEntry?, CostEntity?, List<CostEntry>?) -> List<CostEntry>
    ) = BaseDialog.show(context = context) {
        DialogContentCostsBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.costs_popup)
            binding.message.setText(R.string.costs_popup_info)
            binding.dialogConfirm.isVisible = true

            var startDate: Calendar? = null
            var endDate: Calendar? = null

            lateinit var adapter: Adapter<CostEntry, ContainerCostBinding>

            fun formatDate(date: LocalDate): String {
                return when (date) {
                    LocalDate.now() -> context.getString(R.string.day_today)
                    else -> LocalizationHelper.formatDate(date = date)
                }
            }

            suspend fun refreshData() {
                adapter.submitList(onSuccess(null, null, null)) {
                    recyclerviewCostPeriods.scrollToPosition(0)
                }
            }

            adapter = Adapter(
                bindingFactory = ContainerCostBinding::inflate,
                onBind = { costEntry ->
                    dateLabel.text = context.getString(
                        R.string.cost_format,
                        formatDate(costEntry.startDate.toLocalDate()),
                        formatDate(costEntry.endDate.toLocalDate())
                    )
                    priceLabel.text = context.getString(
                        R.string.cost_price,
                        DecimalFormat("0.00#").format(costEntry.price),
                        currency
                    )
                    delete.setOnClickListener {
                        context.lifecycleScope.launch {
                            onSuccess(costEntry, null, null)
                            refreshData()
                        }
                    }
                }
            )

            recyclerviewCostPeriods.apply {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
            }

            context.lifecycleScope.launch {
                refreshData()
            }

            listOf(inputStartDate to true, inputEndDate to false).forEach { (inputField, isStartDate) ->
                inputField.setOnClickListener {
                    showDatePickerDialog(context) { selectedDate ->
                        val (start, end, formattedText) = TimeHelper.applySelectedDate(
                            startDate,
                            endDate,
                            selectedDate = selectedDate,
                            isStartDate = isStartDate
                        )

                        startDate = start
                        endDate = end
                        inputField.setText(formattedText)
                    }
                }
            }

            binding.confirm.setOnClickListener {
                val start = TimeHelper.toLocalDateTime(calendar = startDate ?: Calendar.getInstance())
                val end = TimeHelper.toLocalDateTime(calendar = endDate ?: Calendar.getInstance())

                context.lifecycleScope.launch {
                    onSuccess(
                        null,
                        CostEntity(
                            id = 0L,
                            startDate = start,
                            endDate = end.takeIf { it.toLocalDate() != LocalDate.now() }
                                ?: end.toLocalDate().atTime(23, 59, 59),
                            price = inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0
                        ),
                        null
                    )

                    refreshData()

                    startDate = null
                    endDate = null
                    listOf(inputStartDate, inputEndDate, inputPackPrice).forEach {
                        it.text.clear()
                    }
                }
            }
        }
    }

    fun showBackupDialog(
        context: FragmentActivity,
        onSuccess: () -> Unit
    ) = BaseDialog.show(context = context) {
        binding.title.setText(R.string.backup_popup)
        binding.message.setText(R.string.backup_popup_description)
        binding.dialogConfirm.isVisible = true
        binding.confirm.setText(R.string.popup_backup)
        binding.confirm.setOnClickListener {
            onSuccess()
            dismiss()
        }
    }

    fun showRestoreDialog(
        context: FragmentActivity,
        onSuccess: (Unit?, Unit?, Unit?, TextView?) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentRestoreBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.setText(R.string.restore_popup)
            binding.message.setText(R.string.restore_popup_info)
            binding.dialogConfirm.isVisible = true

            onSuccess(null, null, null, textSelectedFile)
            textSelectedFile.text = context.getString(
                R.string.restore_popup_file,
                context.getString(R.string.restore_popup_file_none)
            )

            openFile.setOnClickListener {
                onSuccess(Unit, null, null, null)
            }

            binding.confirm.setOnClickListener {
                if (textSelectedFile.tag is Uri) {
                    onSuccess(null, Unit, null, null)
                    dismiss()
                }
            }

            dialog.setOnDismissListener {
                onSuccess(null, null, Unit, null)
            }
        }
    }

    fun showDatePickerDialog(
        context: FragmentActivity,
        onSuccess: (Calendar) -> Unit
    ) = BaseDialog.show(context = context) {
        DialogContentDatePickerBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.isGone = true
            binding.message.isGone = true
            binding.dialogConfirm.isVisible = true

            val selectedDate = Calendar.getInstance()

            customCalendarView.date = selectedDate.timeInMillis
            customCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
            }

            binding.confirm.setOnClickListener {
                onSuccess(selectedDate)
                dismiss()
            }
        }
    }

    fun showResultDialog(
        context: FragmentActivity,
        values: Pair<Triple<Double, Int, Int>, String>,
        formatTime: (Int) -> String
    ) = BaseDialog.show(context = context) {
        DialogContentResultBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.message.isGone = true
            binding.title.setText(R.string.calculator_result)

            val (result, currencyUnit) = values
            val (total, totalTimeMinutes, totalCigarettes) = result

            val averageCostPerCigarette = totalCigarettes.takeIf { it > 0 }?.let { total / it } ?: 0.0
            val totalHours = totalTimeMinutes / 60.0
            val averageCost = totalHours.takeIf { it > 0 }?.let { total / it } ?: 0.0

            val decimalFormatTwo = DecimalFormat("0.00")
            val decimalFormatThree = DecimalFormat("0.000")

            fun formatPrice(
                amount: Double,
                format: DecimalFormat
            ): String {
                return context.getString(R.string.cost_price, format.format(amount), currencyUnit)
            }

            totalCosts.text = formatPrice(total, decimalFormatTwo)
            costPerCigarette.text = formatPrice(averageCostPerCigarette, decimalFormatThree)
            averageCostPerHour.text = formatPrice(averageCost, decimalFormatTwo)
            timeSpent.text = formatTime(totalTimeMinutes)
        }
    }

    fun showLoadingDialog(
        context: FragmentActivity
    ): LoadingDialog {
        return LoadingDialog(context).also(block = LoadingDialog::show)
    }

    fun showSaveNoteDialog(
        context: FragmentActivity,
        onSuccess: (Boolean, Boolean) -> Unit
    ) = BaseDialog.show(context = context) {
        binding.message.isGone = true
        binding.title.setText(R.string.note_popup_save)
        binding.dialogConfirm.isVisible = true

        binding.confirm.setText(R.string.popup_save)
        binding.confirm.setOnClickListener {
            onSuccess(true, false)
            dismiss()
        }

        binding.close.setOnClickListener {
            onSuccess(false, true)
            dismiss()
        }
    }

    fun showChangelogDialog(
        context: FragmentActivity,
    ) = BaseDialog.show(context = context) {
        DialogContentChangelogBinding.inflate(context.layoutInflater, binding.dialogContent, true).apply {
            binding.title.isGone = true
            binding.message.isGone = true
            setCancelable(false)

            val locale = LocalizationHelper.getLocale()
            val languageTag = locale.toLanguageTag()
            val language = locale.language

            runCatching {
                val version = context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName

                val directories = context.assets.list("changelogs") ?: emptyArray()
                val changelogFile = "v$version.md"

                val directory = sequenceOf(languageTag, language)
                    .plus(elements = directories.filter {
                        it.startsWith("$language-")
                    }).firstOrNull { directory ->
                        changelogFile in (context.assets.list("changelogs/$directory")
                            ?: emptyArray())
                    } ?: "en-US"

                val content = context.assets
                    .open("changelogs/$directory/$changelogFile")
                    .bufferedReader()
                    .use { it.readText() }

                Markwon.builder(context)
                    .usePlugin(LinkifyPlugin.create())
                    .usePlugin(
                        object : AbstractMarkwonPlugin() {
                            override fun configureTheme(
                                builder: MarkwonTheme.Builder
                            ) {
                                builder.headingBreakHeight(0)
                            }
                        }
                    )
                    .build()
                    .setMarkdown(changelogText, content)
                changelogText.movementMethod = LinkMovementMethod.getInstance()
            }.onFailure {
                changelogText.text = ""
            }
        }
    }
}