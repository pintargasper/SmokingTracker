package com.gasperpintar.smokingtracker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.databinding.ActivityCalculatorBinding
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import java.time.ZoneId
import java.util.Calendar

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding

    private lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    companion object {
        private const val TIME_PER_CIGARETTE_MINUTES: Int = 5
        private const val MILLIS_IN_DAY: Long = 1000L * 60L * 60L * 24L
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        initViewBinding()
        initListeners()
    }

    override fun attachBaseContext(
        context: Context
    ) {
        database = Provider.getDatabase(context = context.applicationContext)
        settingsRepository = SettingsRepository(
            settingsDao = database.settingsDao()
        )

        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = settingsRepository
            )
        )
    }

    private fun initViewBinding() {
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initListeners(): Unit = with(binding) {
        inputStartDate.setOnClickListener {
            DialogManager.showDatePickerDialog(
                context = this@CalculatorActivity,
            ) { date ->
                applySelectedDate(selectedDate = date, isStartDate = true)
            }
        }

        inputEndDate.setOnClickListener {
            DialogManager.showDatePickerDialog(
                context = this@CalculatorActivity,
            ) { date ->
                applySelectedDate(selectedDate = date, isStartDate = false)
            }
        }

        inputStartDate.setOnLongClickListener {
            clearStartDate()
            true
        }

        inputEndDate.setOnLongClickListener {
            clearEndDate()
            true
        }

        buttonBack.setOnClickListener {
            finish()
        }

        buttonCalculate.setOnClickListener {
            calculate()
        }
    }

    private fun calculate() {
        val dailyCigarettes: Int = binding.inputDailyCigarettes.text.toString().toIntOrNull() ?: 0
        val cigarettesPerPack: Int = binding.inputCigarettesPerPack.text.toString().toIntOrNull() ?: 20
        val packPrice: Double = binding.inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0

        val days: Int = calculateDays()
        val dailyCost: Double = (dailyCigarettes.toDouble() / cigarettesPerPack) * packPrice
        val dailyTimeMinutes: Int = dailyCigarettes * TIME_PER_CIGARETTE_MINUTES
        val totalCost: Double = dailyCost * days
        val totalTimeMinutes: Int = dailyTimeMinutes * days

        showResultDialog(
            totalCost = totalCost,
            totalTimeMinutes = totalTimeMinutes,
            totalCigarettes = dailyCigarettes * days
        )
    }

    private fun calculateDays(): Int {
        if (startDate == null || endDate == null) {
            return 1
        }

        if (endDate!!.before(startDate)) {
            return 1
        }
        val diffMillis: Long = endDate!!.timeInMillis - startDate!!.timeInMillis
        return (diffMillis / MILLIS_IN_DAY).toInt() + 1
    }

    @SuppressLint(value = ["InflateParams"])
    private fun showResultDialog(
        totalCost: Double,
        totalTimeMinutes: Int,
        totalCigarettes: Int
    ) {
        DialogManager.showResultDialog(
            context = this,
            totalCost = totalCost,
            totalTimeMinutes = totalTimeMinutes,
            totalCigarettes = totalCigarettes,
            currencyUnit = getString(R.string.calculator_result_valute_unit),
            formatTime = { minutes -> formatTime(totalMinutes = minutes) }
        )
    }

    private fun applySelectedDate(
        selectedDate: Calendar,
        isStartDate: Boolean
    ) {
        val localDate = selectedDate.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        if (isStartDate) {
            startDate = selectedDate.clone() as Calendar
            binding.inputStartDate.setText(LocalizationHelper.formatDate(localDate))

            if (endDate != null && endDate!!.before(startDate)) {
                clearEndDate()
            }
        } else {
            endDate = selectedDate.clone() as Calendar
            binding.inputEndDate.setText(LocalizationHelper.formatDate(localDate))

            if (startDate != null && startDate!!.after(endDate)) {
                clearStartDate()
            }
        }
    }

    private fun clearStartDate() {
        startDate = null
        binding.inputStartDate.setText("")
    }

    private fun clearEndDate() {
        endDate = null
        binding.inputEndDate.setText("")
    }

    private fun formatTime(
        totalMinutes: Int
    ): String {
        val hours: Int = totalMinutes / 60
        val minutes: Int = totalMinutes % 60

        return listOfNotNull(
            if (hours > 0) {
                resources.getQuantityString(
                    R.plurals.time_hours,
                    hours,
                    hours
                )
            } else {
                null
            },
            resources.getQuantityString(
                R.plurals.time_minutes,
                minutes,
                minutes
            )
        ).joinToString(" ")
    }
}