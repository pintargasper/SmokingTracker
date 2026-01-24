package com.gasperpintar.smokingtracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.databinding.ActivityCalculatorBinding
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.RoundedAlertDialog
import java.time.ZoneId
import java.util.Calendar

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding

    private lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.inputStartDate.setOnClickListener {
            showDatePicker(isStart = true)
        }

        binding.inputEndDate.setOnClickListener {
            showDatePicker(isStart = false)
        }

        binding.inputStartDate.setOnLongClickListener {
            binding.inputStartDate.setText("")
            startDate = null
            true
        }

        binding.inputEndDate.setOnLongClickListener {
            binding.inputEndDate.setText("")
            endDate = null
            true
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonCalculate.setOnClickListener {
            val dailyCigarettes = binding.inputDailyCigarettes.text.toString().toIntOrNull() ?: 0
            val cigarettesPerPack =
                binding.inputCigarettesPerPack.text.toString().toIntOrNull() ?: 20
            val packPrice = binding.inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0
            calculateResults(dailyCigarettes, cigarettesPerPack, packPrice)
        }
    }

    @SuppressLint(value = ["DefaultLocale", "InflateParams"])
    private fun calculateResults(dailyCigarettes: Int, cigarettesPerPack: Int, packPrice: Double) {
        val dailyCost = (dailyCigarettes.toDouble() / cigarettesPerPack) * packPrice
        val timePerCigaretteMinutes = 5
        val dailyTimeMinutes = dailyCigarettes * (timePerCigaretteMinutes)

        val days = if (startDate != null && endDate != null && !endDate!!.before(startDate)) {
            val diff = endDate!!.timeInMillis - startDate!!.timeInMillis
            (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        } else {
            1
        }

        val totalCost = dailyCost * days
        val totalTime = dailyTimeMinutes * days

        val dialogView = LayoutInflater.from(this).inflate(R.layout.calculator_result_popup, null)
        val totalCostsView = dialogView.findViewById<TextView>(R.id.popup_result_total_costs)
        val averageCostPerCigaretteView = dialogView.findViewById<TextView>(R.id.popup_result_cost_per_cigarette)
        val averageCostPerHourView = dialogView.findViewById<TextView>(R.id.popup_result_average_cost_per_hour)
        val timeSpentView = dialogView.findViewById<TextView>(R.id.popup_result_time_spent)
        val closeButton = dialogView.findViewById<Button>(R.id.button_close_result)

        totalCostsView.text = String.format("%.2f %s", totalCost, getString(R.string.calculator_result_valute_unit))

        val totalCigarettes = dailyCigarettes * days
        val averageCostPerCigarette = if (totalCigarettes > 0) {
            totalCost / totalCigarettes
        } else {
            0.0
        }
        averageCostPerCigaretteView.text = String.format("%.3f %s", averageCostPerCigarette, getString(R.string.calculator_result_valute_unit))

        val totalHours = totalTime / 60.0
        val averageCostPerHour = if (totalHours > 0) {
            totalCost / totalHours
        } else {
            0.0
        }
        averageCostPerHourView.text = String.format("%.2f %s", averageCostPerHour, getString(R.string.calculator_result_valute_unit))

        val dialog = RoundedAlertDialog(context = this)
            .setViewChained(dialogView)
            .showChained()

        val hours = totalTime / 60
        val minutes = totalTime % 60

        timeSpentView.text = listOfNotNull(
            if (hours > 0) {
                resources.getQuantityString(R.plurals.time_hours, hours, hours)
            } else {
                null
            },
            resources.getQuantityString(R.plurals.time_minutes, minutes, minutes)
        ).joinToString(" ")

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = (if (isStart) {
            startDate
        } else {
            endDate
        })?.clone() as? Calendar ?: Calendar.getInstance()
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_picker, null)
        val calendarView = dialogView.findViewById<CalendarView>(R.id.customCalendarView)
        val selectedDate = calendar.clone() as Calendar

        calendarView.date = calendar.timeInMillis
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            calendar.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
            if (isStart) {
                startDate = calendar.clone() as Calendar
                val localDate = calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                binding.inputStartDate.setText(LocalizationHelper.formatDate(localDate))
                if (endDate != null && endDate!!.before(startDate)) {
                    endDate = null
                    binding.inputEndDate.setText("")
                }
            } else {
                endDate = calendar.clone() as Calendar
                val localDate = calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                binding.inputEndDate.setText(LocalizationHelper.formatDate(localDate))
                if (startDate != null && startDate!!.after(endDate)) {
                    startDate = null
                    binding.inputStartDate.setText("")
                }
            }
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun attachBaseContext(context: Context) {
        database = Provider.getDatabase(context = context.applicationContext)
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        super.attachBaseContext(LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository))
    }
}
