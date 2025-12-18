package com.gasperpintar.smokingtracker.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import com.gasperpintar.smokingtracker.ui.DialogManager
import com.gasperpintar.smokingtracker.utils.Helper
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { (requireActivity() as MainActivity).database }
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedFileUri: Uri? = null
    private var selectedFile: TextView? = null

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

    private fun setupUI() {
        val settingsDao = database.settingsDao()

        lifecycleScope.launch {
            val settings = settingsDao.getSettings() ?: insertDefaultSettings(settingsDao)

            binding.languageServiceUrl.text = getLanguages()[settings.language]
            binding.themeServiceUrl.text = getThemes()[settings.theme]
        }

        binding.themeLayout.setOnClickListener {
            lifecycleScope.launch {
                val currentTheme = settingsDao.getSettings()?.theme ?: 0
                DialogManager.showThemeDialog(
                    activity = requireActivity(),
                    selectedTheme = currentTheme,
                    onThemeSelected = { theme -> updateSettingsField(updateBlock = { it.copy(theme = theme) }, recreateActivity = true) }
                )
            }
        }

        binding.languageLayout.setOnClickListener {
            lifecycleScope.launch {
                val currentLanguage = settingsDao.getSettings()?.language ?: getDefaultLanguageIndex()
                DialogManager.showLanguageDialog(
                    activity = requireActivity(),
                    selectedLanguage = currentLanguage,
                    onLanguageSelected = { language -> updateSettingsField(updateBlock = { it.copy(language = language) }, recreateActivity = true) }
                )
            }
        }

        binding.notificationsLayout.setOnClickListener {
            lifecycleScope.launch {
                if (!areNotificationsEnabled()) {
                    openNotificationSettings()
                    return@launch
                }

                val currentNotifications = settingsDao.getSettings()?.notifications ?: 0
                DialogManager.showNotificationsDialog(
                    activity = requireActivity(),
                    selectedNotificationOption = currentNotifications,
                    onNotificationOptionSelected = { notification ->
                        if (notification == 1 && !areNotificationsEnabled()) {
                            openNotificationSettings()
                            return@showNotificationsDialog
                        }
                        updateSettingsField(updateBlock = { it.copy(notifications = notification) })
                    }
                )
            }
        }

        binding.downloadLayout.setOnClickListener {
            DialogManager.showDownloadDialog(
                activity = requireActivity(),
                database = database
            )
        }

        binding.uploadLayout.setOnClickListener {
            DialogManager.showUploadDialog(
                activity = requireActivity(),
                database = database,
                filePickerLauncher = filePickerLauncher,
                selectedFileSetter = { textView -> selectedFile = textView },
                getSelectedFileUri = { selectedFileUri },
                clearSelectedFile = { selectedFileUri = null }
            )
        }
    }

    private fun updateSettingsField(updateBlock: (SettingsEntity) -> SettingsEntity, recreateActivity: Boolean = false) {
        val settingsDao = database.settingsDao()
        lifecycleScope.launch {
            settingsDao.getSettings()?.let { currentSettings ->
                settingsDao.update(settingsEntity = updateBlock(currentSettings))
                if (recreateActivity) requireActivity().recreate()
            }
        }
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

    private fun setupFilePicker() {
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedFileUri = uri
                    selectedFile?.text = String.format(
                        $$"%1$s: %2$s",
                        getString(R.string.upload_popup_file),
                        Helper.getFileName(context = requireActivity(), uri = uri)
                    )
                }
            }
        }
    }

    private fun setupAbout() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionName = packageInfo.versionName ?: getString(R.string.settings_category_data_version_unknown)

        with(binding) {
            appVersion.text = getString(R.string.settings_category_data_version, versionName)
            appVersionUrl.text = "https://play.google.com/store/apps/details?id=com.gasperpintar.smokingtracker"
            websiteServiceUrl.text = "https://gasperpintar.com/smoking-tracker"

            listOf(
                versionUrl to "https://play.google.com/store/apps/details?id=com.gasperpintar.smokingtracker",
                websiteUrl to "https://gasperpintar.com/smoking-tracker",
                translationsWebsiteUrl to "https://translate.gasperpintar.com/projects/smokingtracker",
                privacyPolicyUrl to "https://gasperpintar.com/smoking-tracker/privacy-policy"
            ).forEach { (view, url) -> setupLink(view, url) }
        }
    }

    private fun setupLink(view: View, url: String) {
        view.setOnClickListener { openUrl(url) }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun getLanguages(): List<String> {
        return listOf(
            getString(R.string.language_popup_check_box_system),
            "English",
            "Slovenščina"
        )
    }

    private fun getThemes(): List<String> {
        return listOf(
            getString(R.string.theme_popup_check_box_system),
            getString(R.string.theme_popup_check_box_light_theme),
            getString(R.string.theme_popup_check_box_dark_theme)
        )
    }

    private fun getDefaultLanguageIndex(): Int {
        return when(Locale.getDefault().language) {
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