package com.gasperpintar.smokingtracker.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    private var selectedFile: TextView? = null

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

        binding.downloadLayout.setOnClickListener { showDownloadDialog() }
        binding.uploadLayout.setOnClickListener { showUploadDialog() }
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
                selectedFile?.text = getString(
                    R.string.upload_popup_file_status,
                    getString(R.string.upload_popup_file),
                    getFileName(uri = result.data?.data)
                )
            }
        }
    }

    private fun showDownloadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.download_popup, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.show()

        val buttonDownload: Button = dialogView.findViewById(R.id.button_download)
        val buttonClose: Button = dialogView.findViewById(R.id.button_close)

        buttonDownload.setOnClickListener {
            requireActivity().lifecycleScope.launch {
                Manager.downloadFile(context = requireContext(), database)
            }
            dialog.dismiss()
        }
        buttonClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showUploadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.upload_popup, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.show()

        selectedFile = dialogView.findViewById(R.id.text_selected_file)
        val buttonOpenFile: Button = dialogView.findViewById(R.id.button_open_file)
        val buttonConfirm: Button = dialogView.findViewById(R.id.button_confirm)
        val buttonBack: Button = dialogView.findViewById(R.id.button_back)

        selectedFile?.text = getString(
            R.string.upload_popup_file_status,
            getString(R.string.upload_popup_file),
            getString(R.string.upload_popup_file_none)
        )

        buttonOpenFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            filePickerLauncher.launch(intent)
        }

        buttonConfirm.setOnClickListener {
            requireActivity().lifecycleScope.launch {
                selectedFileUri?.let { fileUri ->
                    Manager.uploadFile(
                        context = requireContext(),
                        fileUri = fileUri,
                        database = database
                    )
                }
            }
            dialog.dismiss()
        }
        buttonBack.setOnClickListener { dialog.dismiss() }
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
            setupLink(view = translationsWebsiteUrl, url = "https://translate.gasperpintar.com/projects/smokingtracker")
            setupLink(view = privacyPolicyUrl, url = "https://gasperpintar.com/smoking-tracker/privacy-policy")
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

    private fun getFileName(uri: Uri?): String {
        var name = getString(R.string.upload_popup_file_unknown)

        if (uri == null) return name
        context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}