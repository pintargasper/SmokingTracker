package com.gasperpintar.smokingtracker.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import com.gasperpintar.smokingtracker.utils.Manager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { (requireActivity() as MainActivity).database }
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedFileUri: Uri? = null

    private companion object {
        const val PREFS_NAME = "settings"
        const val PREF_LANGUAGE = "language"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupFilePicker()
        setupUI()
        setupAbout()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val notificationsEnabled = areNotificationsEnabled()
        binding.switchNotifications.isChecked = notificationsEnabled &&
                (runBlocking { database.settingsDao().getSettings()?.notifications } == 1)
    }

    private fun setupUI() {
        val settingsDao = database.settingsDao()
        val currentSettings: SettingsEntity = runBlocking {
            settingsDao.getSettings() ?: insertDefaultSettings(settingsDao)
        }

        applySettingsToUI(currentSettings)
        setupListeners(currentSettings)

        binding.buttonDataAction.setOnClickListener { showDataDialog() }
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
    }

    private suspend fun insertDefaultSettings(settingsDao: SettingsDao): SettingsEntity {
        return SettingsEntity(
            id = 0L,
            theme = 0,
            language = getDefaultLanguageIndex(),
            notifications = if (areNotificationsEnabled()) 1 else 0
        ).also { settingsDao.insert(settingsEntity = it) }
    }

    private fun applySettingsToUI(settings: SettingsEntity) = with(receiver = binding) {
        spinnerTheme.setSelection(settings.theme)
        spinnerLanguage.setSelection(settings.language)
        switchNotifications.isChecked = settings.notifications == 1
    }

    private fun setupListeners(settings: SettingsEntity) = with(receiver = binding) {
        spinnerTheme.setupSpinnerListener(currentValue = settings.theme) { newValue ->
            updateSettings(settings = settings.copy(theme = newValue)) {
                (requireActivity() as MainActivity).applyTheme(themeId = newValue)
            }
        }

        spinnerLanguage.setupSpinnerListener(currentValue = settings.language) { newValue ->
            updateSettings(settings = settings.copy(language = newValue)) {
                requireContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit {
                        putInt(PREF_LANGUAGE, newValue)
                    }
                requireActivity().recreate()
            }
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val notificationsCurrentlyEnabled = areNotificationsEnabled()
                if (isChecked) {
                    if (!notificationsCurrentlyEnabled) {
                        switchNotifications.isChecked = false
                        openNotificationSettings()
                    } else updateSettings(settings = settings.copy(notifications = 1))
                } else updateSettings(settings = settings.copy(notifications = 0))
            }
        }
    }

    private fun Spinner.setupSpinnerListener(currentValue: Int, onChange: suspend (Int) -> Unit) {
        setSelection(currentValue)

        var lastSelectedPosition = currentValue
        var isFirstSelection = true

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    lastSelectedPosition = position
                    return
                }

                if (position != lastSelectedPosition) {
                    lastSelectedPosition = position
                    lifecycleScope.launch { onChange(position) }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private suspend fun updateSettings(settings: SettingsEntity, afterUpdate: (() -> Unit)? = null) {
        database.settingsDao().update(settingsEntity = settings)
        afterUpdate?.invoke()
    }

    private fun setupFilePicker() {
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedFileUri = result.data?.data
            }
        }
    }

    private fun showDataDialog() {
        val dialogView = layoutInflater.inflate(R.layout.download_upload_popup, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val cardOpenFile = dialogView.findViewById<CardView>(R.id.card_open_file_button)
        val buttonOpenFile = dialogView.findViewById<Button>(R.id.button_open_file)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_confirm)
        val buttonClose = dialogView.findViewById<Button>(R.id.button_close)
        val reasonSpinner = dialogView.findViewById<Spinner>(R.id.spinner_reason)

        reasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cardOpenFile.visibility = if (position == 1) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                cardOpenFile.visibility = View.GONE
            }
        }

        buttonOpenFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            filePickerLauncher.launch(intent)
        }

        buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                if (reasonSpinner.selectedItemPosition == 0) {
                    Manager.downloadFile(context = requireContext(), database)
                } else {
                    selectedFileUri?.let { fileUri ->
                        Manager.uploadFile(
                            context = requireContext(),
                            fileUri = fileUri,
                            database = database
                        )
                    }
                }
            }
            dialog.dismiss()
        }
        buttonClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupAbout() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionName = packageInfo.versionName ?: "Unknown"

        with(receiver = binding) {
            appVersion.text = getString(R.string.settings_category_data_version, versionName)
            appVersionUrl.text = getString(R.string.settings_category_data_version_url)
            websiteServiceUrl.text = getString(R.string.settings_category_data_website_url)

            setupLink(view = versionUrl, url = getString(R.string.settings_category_data_version_url))
            setupLink(view = websiteUrl, url = getString(R.string.settings_category_data_website_url))
        }
    }

    private fun setupLink(view: View, url: String) {
        view.setOnClickListener { openUrl(url) }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun getDefaultLanguageIndex(): Int {
        val systemLanguageCode = Locale.getDefault().language
        return when(systemLanguageCode) {
            "en" -> 1
            "sl" -> 2
            else -> 0
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("android.provider.extra.APP_PACKAGE", requireContext().packageName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}