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
import com.gasperpintar.smokingtracker.databinding.ContainerContentCostBinding
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
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
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
    ) = BaseDialog.show(context = context, inflate = DialogContentInsertBinding::inflate) { content ->
        binding.message.isGone = true
        binding.title.setText(R.string.insert_popup)
        binding.dialogConfirm.isVisible = true
        binding.confirm.setText(R.string.popup_add)

        binding.confirm.setOnClickListener {
            onSuccess(content.lentCheckbox.isChecked)
            dismiss()
        }
    }

    fun showEditDialog(
        context: FragmentActivity,
        entry: HistoryEntry,
        onSuccess: (LocalDateTime, Boolean) -> Unit
    ) = BaseDialog.show(context = context, inflate = DialogContentEditBinding::inflate) { content ->
        content.lentCheckbox.isChecked = entry.isLent
        content.timePicker.is24Hour = DateFormat.is24HourFormat(context)

        entry.createdAt.let { dateTime ->
            content.datePicker.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
            content.timePicker.hour = dateTime.hour
            content.timePicker.minute = dateTime.minute
        }

        binding.message.isGone = true
        binding.title.setText(R.string.edit_popup_message)
        binding.dialogConfirm.isVisible = true

        binding.confirm.setOnClickListener {
            val selectedDateTime = LocalDateTime.of(
                content.datePicker.toLocalDate(),
                content.timePicker.toLocalTime()
            ).withSecond(entry.createdAt.second)

            onSuccess(selectedDateTime, content.lentCheckbox.isChecked)
            dismiss()
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
    ) = BaseDialog.show(context = context, inflate = DialogContentThemeBinding::inflate) { content ->
        binding.title.setText(R.string.theme_popup)
        binding.message.setText(R.string.theme_popup_info)

        listOf(content.checkboxSystem, content.checkboxLightTheme, content.checkboxDarkTheme).forEachIndexed { index, checkbox ->
            checkbox.isChecked = selectedTheme == index
            checkbox.setOnClickListener {
                onSuccess(index)
                dismiss()
            }
        }
    }

    fun showLanguageDialog(
        context: FragmentActivity,
        selectedLanguage: String,
        onLanguageSelected: (String) -> Unit
    ) = BaseDialog.show(context = context, inflate = DialogContentLanguageBinding::inflate) { content ->
        binding.title.setText(R.string.language_popup)
        binding.message.setText(R.string.language_popup_info)

        val languages = context.resources.getStringArray(R.array.language_values)
        listOf(content.checkboxSystem, content.checkboxGerman, content.checkboxEnglish,
            content.checkboxFrench, content.checkboxHungarian, content.checkboxSlovenian,
            content.checkboxSerbianCyrillicScript, content.checkboxSerbianLatinScript,
            content.checkboxUkrainian, content.checkboxChineseTraditional,
            content.checkboxChineseSimplified
        ).forEachIndexed { index, checkbox ->
            checkbox.isChecked = selectedLanguage == languages[index]
            checkbox.setOnClickListener {
                onLanguageSelected(languages[index])
                dismiss()
            }
        }
    }

    fun showNotificationsDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        notificationsSettings: NotificationsSettingsEntity,
        onSuccess: (SettingsEntity, NotificationsSettingsEntity) -> Unit,
    ) = BaseDialog.show(context = context, inflate = DialogContentNotificationsBinding::inflate) { content ->
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

        bindCheckbox(content.checkboxSystem, currentNotificationSettings.system) { ns, c -> ns.copy(system = c) }
        bindCheckbox(content.checkboxProgress, currentNotificationSettings.progress) { ns, c -> ns.copy(progress = c) }
        bindCheckbox(content.checkboxAchievements, currentNotificationSettings.achievements) { ns, c -> ns.copy(achievements = c) }

        content.spinnerProgressFrequency.apply {
            setText(context.resources.getStringArray(R.array.frequency_options)[currentSettings.frequency], false)
            setOnItemClickListener { _, _, position, _ ->
                currentSettings = currentSettings.copy(frequency = position)
                onSuccess(currentSettings, currentNotificationSettings)
            }
        }
    }

    fun showEndDayDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onSuccess: (Int) -> Unit
    ) = BaseDialog.show(context = context, inflate = DialogContentEndDayBinding::inflate) { content ->
        content.timePicker.is24Hour = DateFormat.is24HourFormat(context)
        content.timePicker.hour = settings.dayEndMinutes / 60
        content.timePicker.minute = settings.dayEndMinutes % 60

        binding.title.setText(R.string.settings_general_day_end_hour)
        binding.message.setText(R.string.settings_general_day_end_hour_description)
        binding.dialogConfirm.isVisible = true

        binding.confirm.setOnClickListener {
            onSuccess(content.timePicker.hour * 60 + content.timePicker.minute)
            dismiss()
        }
    }

    fun showWidgetsDialog(
        context: FragmentActivity,
        onSuccess: (Widget) -> Unit
    ) = BaseDialog.show(context = context, inflate = DialogContentWidgetsBinding::inflate) { content ->
        binding.title.setText(R.string.settings_general_widgets)
        binding.message.setText(R.string.settings_general_widgets_description)

        listOf(
            content.widget1 to Widget.ONLY_QUICK_ADD,
            content.widget2 to Widget.QUICK_ADD,
            content.widget3 to Widget.STATS,
            content.widget4 to Widget.STATS_QUICK_ADD
        ).forEach { (button, widget) ->
            button.setOnClickListener {
                onSuccess(widget)
            }
        }
    }

    fun showCurrencyDialog(
        context: FragmentActivity,
        settings: SettingsEntity,
        onSuccess: (String, String) -> Unit
    ) = BaseDialog.show(context = context, inflate = DialogContentCurrencyBinding::inflate) { content ->
        binding.title.setText(R.string.currency_popup)
        binding.message.setText(R.string.currency_popup_info)

        content.inputCustomCurrency.setText(settings.customCurrency)

        fun select(currencyValue: String) {
            onSuccess(currencyValue, content.inputCustomCurrency.text.toString().trim())
            dismiss()
        }

        val currencyMap = mapOf(content.checkboxEuro to "€", content.checkboxDollar to "$", content.checkboxPound to "£")
        val activeCheckbox = currencyMap.entries.firstOrNull { it.value == settings.currency }?.key ?: content.checkboxCustom

        activeCheckbox.isChecked = true

        currencyMap.forEach { (checkbox, value) ->
            checkbox.setOnClickListener {
                content.editTextError.visibility = View.GONE
                select(currencyValue = value)
            }
        }

        content.checkboxCustom.setOnClickListener {
            content.editTextError.visibility = View.GONE
            content.inputCustomCurrency.text.toString().trim().takeIf { it.isNotEmpty() }?.let(block = ::select) ?: run {
                content.checkboxCustom.isChecked = false
                content.editTextError.visibility = View.VISIBLE
                content.inputCustomCurrency.requestFocus()
            }
        }

        content.inputCustomCurrency.doAfterTextChanged { text ->
            if (!text.isNullOrBlank()) content.editTextError.visibility = View.GONE
        }

        content.inputCustomCurrency.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && content.inputCustomCurrency.text?.isNotBlank() == true) {
                content.checkboxCustom.isChecked = true
                content.editTextError.visibility = View.GONE
            }
        }
    }

    fun showCostsDialog(
        context: FragmentActivity,
        currency: String,
        onSuccess: suspend (CostEntry?, CostEntity?, List<CostEntry>?) -> List<CostEntry>
    ) = BaseDialog.show(context = context, inflate = DialogContentCostsBinding::inflate) { content ->
        binding.title.setText(R.string.costs_popup)
        binding.message.setText(R.string.costs_popup_info)
        binding.dialogConfirm.isVisible = true

        var startDate: Calendar? = null
        var endDate: Calendar? = null

        lateinit var adapter: Adapter<CostEntry, ContainerContentCostBinding>

        suspend fun refreshData() {
            adapter.submitList(onSuccess(null, null, null)) {
                content.recyclerviewCostPeriods.scrollToPosition(0)
            }
        }

        adapter = ContainerManager.createCostAdapter(context = context, currency = currency) { entry ->
            context.lifecycleScope.launch {
                onSuccess(entry, null, null)
                refreshData()
            }
        }

        content.recyclerviewCostPeriods.layoutManager = LinearLayoutManager(context)
        content.recyclerviewCostPeriods.adapter = adapter

        context.lifecycleScope.launch {
            refreshData()
        }

        listOf(content.inputStartDate to true, content.inputEndDate to false).forEach { (inputField, isStartDate) ->
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
                        price = content.inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0
                    ),
                    null
                )
                refreshData()

                startDate = null
                endDate = null

                listOf(content.inputStartDate, content.inputEndDate, content.inputPackPrice).forEach {
                    it.text.clear()
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
    ) = BaseDialog.show(context = context, inflate = DialogContentRestoreBinding::inflate) { content ->
        binding.title.setText(R.string.restore_popup)
        binding.message.setText(R.string.restore_popup_info)
        binding.dialogConfirm.isVisible = true

        onSuccess(null, null, null, content.textSelectedFile)

        content.textSelectedFile.text = context.getString(
            R.string.restore_popup_file,
            context.getString(R.string.restore_popup_file_none)
        )

        content.openFile.setOnClickListener {
            onSuccess(Unit, null, null, null)
        }

        binding.confirm.setOnClickListener {
            if (content.textSelectedFile.tag is Uri) {
                onSuccess(null, Unit, null, null)
                dismiss()
            }
        }

        dialog.setOnDismissListener {
            onSuccess(null, null, Unit, null)
        }
    }

    fun showDatePickerDialog(
        context: FragmentActivity,
        onSuccess: (Calendar) -> Unit
    ) = BaseDialog.show(context = context, inflate = DialogContentDatePickerBinding::inflate) { content ->
        binding.title.isGone = true
        binding.message.isGone = true
        binding.dialogConfirm.isVisible = true

        val selectedDate = Calendar.getInstance()
        content.customCalendarView.date = selectedDate.timeInMillis
        content.customCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        }

        binding.confirm.setOnClickListener {
            onSuccess(selectedDate)
            dismiss()
        }
    }

    fun showResultDialog(
        context: FragmentActivity,
        values: Pair<Triple<Double, Int, Int>, String>,
        formatTime: (Int) -> String
    ) = BaseDialog.show(context = context, inflate = DialogContentResultBinding::inflate) { content ->
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

        content.totalCosts.text = formatPrice(total, decimalFormatTwo)
        content.costPerCigarette.text = formatPrice(averageCostPerCigarette, decimalFormatThree)
        content.averageCostPerHour.text = formatPrice(averageCost, decimalFormatTwo)
        content.timeSpent.text = formatTime(totalTimeMinutes)
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
    ) = BaseDialog.show(context = context, inflate = DialogContentChangelogBinding::inflate) { content ->
        binding.title.isGone = true
        binding.message.isGone = true
        setCancelable(false)

        val locale = LocalizationHelper.getLocale()
        val languageTag = locale.toLanguageTag()
        val language = locale.language

        runCatching {
            val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            val directories = context.assets.list("changelogs") ?: emptyArray()
            val changelogFile = "v$version.md"

            val directory = sequenceOf(languageTag, language)
                .plus(elements = directories.filter {
                    it.startsWith("$language-")
                }).firstOrNull { directory ->
                    changelogFile in (context.assets.list("changelogs/$directory") ?: emptyArray())
                } ?: "en-US"

            val changelogContent = context.assets
                .open("changelogs/$directory/$changelogFile")
                .bufferedReader()
                .use { it.readText() }

            Markwon.builder(context)
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(
                    object : AbstractMarkwonPlugin() {
                        @Override
                        override fun configureTheme(
                            builder: MarkwonTheme.Builder
                        ) {
                            builder.headingBreakHeight(0)
                        }
                    }
                )
                .build()
                .setMarkdown(content.changelogText, changelogContent)
            content.changelogText.movementMethod = LinkMovementMethod.getInstance()
        }.onFailure {
            content.changelogText.text = ""
        }
    }
}