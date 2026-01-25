package com.gasperpintar.smokingtracker.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.FileHelper
import com.gasperpintar.smokingtracker.utils.Manager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationsSettingsRepository: NotificationsSettingsRepository

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedFileUri: Uri? = null
    private var selectedFile: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        database = (requireActivity() as MainActivity).database
        historyRepository = HistoryRepository(historyDao = database.historyDao())
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        notificationsSettingsRepository = NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())

        setupFilePicker()
        setup()
        setupAbout()

        return binding.root
    }

    private fun setup() {
        lifecycleScope.launch {
            withSettings { settings ->
                binding.languageServiceUrl.text = getLanguages()[settings.language]
                binding.themeServiceUrl.text = getThemes()[settings.theme]
            }
        }

        binding.themeLayout.setOnClickListener {
            lifecycleScope.launch {
                withSettings { settings ->
                    DialogManager.showThemeDialog(
                        context = requireActivity(),
                        selectedTheme = settings.theme,
                        onThemeSelected = { theme ->
                            updateSettingsField(
                                updateBlock = {
                                    it.copy(theme = theme)
                                }
                            )
                        }
                    )
                }
            }
        }

        binding.languageLayout.setOnClickListener {
            lifecycleScope.launch {
                withSettings { settings ->
                    DialogManager.showLanguageDialog(
                        context = requireActivity(),
                        selectedLanguage = settings.language,
                        onLanguageSelected = { language ->
                            updateSettingsField(
                                updateBlock = {
                                    it.copy(language = language)
                                }
                            )
                        }
                    )
                }
            }
        }

        binding.notificationsLayout.setOnClickListener {
            lifecycleScope.launch {
                if (!areNotificationsEnabled()) {
                    openNotificationSettings()
                    return@launch
                }

                DialogManager.showNotificationsDialog(
                    context = requireActivity(),
                    notificationsSettings = notificationsSettingsRepository.get()!!,
                    onNotificationSettingsSelected = { notification ->
                        lifecycleScope.launch {
                            notificationsSettingsRepository.update(settings = notification)
                        }
                    }
                )
            }
        }

        binding.downloadLayout.setOnClickListener {
            if (!areStoragePermissionsEnabled()) {
                openAppSettings()
                return@setOnClickListener
            }

            DialogManager.showDownloadDialog(context = requireActivity()) {
                lifecycleScope.launch {
                    Manager.downloadFile(
                        context = requireActivity(),
                        historyRepository = historyRepository,
                        settingsRepository = settingsRepository,
                        notificationsSettingsRepository = notificationsSettingsRepository
                    )
                }
            }
        }

        binding.uploadLayout.setOnClickListener {
            DialogManager.showUploadDialog(
                context = requireActivity(),
                onOpenFile = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    filePickerLauncher.launch(intent)
                },
                onConfirm = {
                    selectedFileUri?.let { uri ->
                        lifecycleScope.launch {
                            Manager.uploadFile(
                                context = requireActivity(),
                                fileUri = uri,
                                historyRepository = historyRepository,
                                settingsRepository = settingsRepository,
                                notificationsSettingsRepository = notificationsSettingsRepository
                            )
                            requireActivity().recreate()
                        }
                    }
                },
                onDismiss = {
                    selectedFileUri = null
                },
                onViewCreated = { textView ->
                    selectedFile = textView
                }
            )
        }
    }

    private fun updateSettingsField(
        updateBlock: (SettingsEntity) -> SettingsEntity
    ) {
        lifecycleScope.launch {
            withSettings { currentSettings ->
                val updatedSettings = updateBlock(currentSettings)
                lifecycleScope.launch {
                    settingsRepository.update(updatedSettings)
                    requireActivity().recreate()
                }
            }
        }
    }

    private suspend fun withSettings(
        block: suspend (SettingsEntity) -> Unit
    ) {
        block(settingsRepository.get()!!)
    }

    private fun setupFilePicker() {
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedFileUri = uri
                    selectedFile?.text = String.format(
                        $$"%1$s: %2$s",
                        getString(R.string.upload_popup_file),
                        FileHelper.getFileName(context = requireActivity(), uri = uri)
                    )
                }
            }
        }
    }

    @SuppressLint(value = ["SetTextI18n"])
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
            ).forEach {
                (view, url) -> setupLink(view, url)
            }
        }
    }

    private fun setupLink(
        view: View,
        url: String
    ) {
        view.setOnClickListener { openUrl(url) }
    }

    private fun openUrl(
        url: String
    ) {
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

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
    }

    private fun openNotificationSettings() {
        val intent = Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("android.provider.extra.APP_PACKAGE", requireContext().packageName)
        }
        startActivity(intent)
    }

    private fun areStoragePermissionsEnabled(): Boolean {
        val mainActivity = requireActivity() as MainActivity
        return mainActivity.permissionsHelper.isWriteExternalStoragePermissionGranted()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}