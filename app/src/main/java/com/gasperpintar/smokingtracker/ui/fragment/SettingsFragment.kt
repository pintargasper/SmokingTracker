package com.gasperpintar.smokingtracker.ui.fragment

import android.content.ActivityNotFoundException
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.AboutActivity
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import com.gasperpintar.smokingtracker.model.CostEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.bar.ProgressType
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.FileHelper
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.Manager
import com.gasperpintar.smokingtracker.utils.WebHelper
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationsSettingsRepository: NotificationsSettingsRepository
    private lateinit var costsRepository: CostsRepository
    private lateinit var notesRepository: NotesRepository

    private lateinit var exportDocumentLauncher: ActivityResultLauncher<String>
    private lateinit var importDocumentLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var selectedFile: TextView

    private val mimeExcel = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        database = (requireActivity() as MainActivity).database
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        historyRepository = HistoryRepository(historyDao = database.historyDao())
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        notificationsSettingsRepository = NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())
        costsRepository = CostsRepository(costDao = database.costsDao())
        notesRepository = NotesRepository(notesDao = database.notesDao())

        setupImportLauncher()
        setupExportLauncher()
        setup()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setup() {
        lifecycleScope.launch {
            withSettings { settings ->
                binding.languageService.text = getLanguages()[settings.language]
                binding.themeService.text = getThemes()[settings.theme]
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
                    settings = settingsRepository.get()!!,
                    notificationsSettings = notificationsSettingsRepository.get()!!,
                    onSettingsSelected = { settings ->
                        lifecycleScope.launch {
                            settingsRepository.update(settings = settings)
                        }
                    },
                    onNotificationSettingsSelected = { notification ->
                        lifecycleScope.launch {
                            notificationsSettingsRepository.update(settings = notification)
                        }
                    }
                )
            }
        }

        binding.currencyLayout.setOnClickListener {
            lifecycleScope.launch {
                withSettings { settings ->
                    DialogManager.showCurrencyDialog(
                        context = requireActivity(),
                        settings = settings,
                        onCurrencySelected = { currency, custom ->
                            updateSettingsField(
                                updateBlock = {
                                    it.copy(
                                        currency = currency,
                                        customCurrency = custom
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }

        binding.costsLayout.setOnClickListener {

            lifecycleScope.launch {
                val costs = costsRepository.getAll().map(transform = CostEntry::fromEntity)
                DialogManager.showCostsDialog(
                    context = requireActivity(),
                    costs = costs,
                    currency = settingsRepository.get()?.currency ?: "€",
                    onDelete = { costEntry ->
                        costsRepository.delete(
                            entry = costEntry.toEntity()
                        )
                    },
                    onCostAdded = { costEntity ->
                        costsRepository.insert(
                            entry = costEntity
                        )
                    },
                    onRefresh = {
                        costsRepository.getAll().map(transform = CostEntry::fromEntity)
                    }
                )
            }
        }

        binding.backupLayout.setOnClickListener {
            DialogManager.showBackupDialog(context = requireActivity()) {
                val fileName = "st_data_${LocalizationHelper.formatDateTime(LocalDateTime.now())}"

                try {
                    exportDocumentLauncher.launch(fileName)
                } catch (_: ActivityNotFoundException) {
                    exportViaShareIntent(fileName)
                }
            }
        }

        binding.restoreLayout.setOnClickListener {
            DialogManager.showRestoreDialog(
                context = requireActivity(),
                onOpenFile = {
                    importDocumentLauncher.launch(arrayOf(mimeExcel))
                },
                onConfirm = {
                    val dialog = DialogManager.showLoadingDialog(context = requireActivity())
                    dialog.setProgressType(ProgressType.RESTORE)
                    lifecycleScope.launch {
                        if (::selectedFile.isInitialized) {
                            val uri = selectedFile.tag as? Uri
                            uri?.let {
                                lifecycleScope.launch {
                                    Manager.uploadFile(
                                        context = requireActivity(),
                                        fileUri = it,
                                        achievementRepository = achievementRepository,
                                        historyRepository = historyRepository,
                                        settingsRepository = settingsRepository,
                                        notificationsSettingsRepository = notificationsSettingsRepository,
                                        costsRepository = costsRepository,
                                        notesRepository = notesRepository,
                                        onProgress = { progress ->
                                            dialog.updateProgress(progress)
                                        }
                                    )
                                    requireActivity().recreate()
                                }
                            }
                        }
                    }
                },
                onDismiss = {
                    if (::selectedFile.isInitialized) {
                        selectedFile.text = getString(R.string.restore_popup_file_none)
                        selectedFile.tag = null
                    }
                },
                onViewCreated = { textView ->
                    selectedFile = textView
                    selectedFile.text = getString(R.string.restore_popup_file_none)
                }
            )
        }

        binding.aboutLayout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        binding.websiteLayout.setOnClickListener {
            WebHelper.openUrl(context = requireContext(), url = "https://gasperpintar.com/smoking-tracker")
        }

        binding.changelogLayout.setOnClickListener {
            WebHelper.openUrl(context = requireContext(), url =  "https://github.com/pintargasper/SmokingTracker/releases")
        }

        binding.translateLayout.setOnClickListener {
            WebHelper.openUrl(context = requireContext(), url =  "https://translate.gasperpintar.com/projects/smokingtracker")
        }

        binding.privacyPolicyLayout.setOnClickListener {
            WebHelper.openUrl(context = requireContext(), url = "https://gasperpintar.com/smoking-tracker/privacy-policy")
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

    private fun getLanguages(): List<String> {
        return resources.getStringArray(R.array.language_names).toList()
    }

    private fun getThemes(): List<String> {
        return resources.getStringArray(R.array.theme_names).toList()
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

    private fun setupImportLauncher() {
        importDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri ?: return@registerForActivityResult
                if (::selectedFile.isInitialized) {
                    selectedFile.text = String.format(
                        $$"%1$s: %2$s",
                        getString(R.string.restore_popup_file),
                        FileHelper.getFileName(context = requireActivity(), uri = uri)
                    )
                    selectedFile.tag = uri
                }
            }
    }

    private fun setupExportLauncher() {
        exportDocumentLauncher =
            registerForActivityResult(
                ActivityResultContracts.CreateDocument(mimeExcel)
            ) { uri: Uri? ->
                uri ?: return@registerForActivityResult
                exportFile(fileUri = uri)
            }
    }

    private fun exportViaShareIntent(fileName: String) {
        val context = requireContext()
        val cacheFile = File(context.cacheDir, "$fileName.xlsx")

        if (cacheFile.exists()) {
            cacheFile.delete()
        }

        exportFile(fileUri = Uri.fromFile(cacheFile)) {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeExcel
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.backup_popup)))
        }
    }

    private fun exportFile(
        fileUri: Uri,
        onFinished: () -> Unit = {}
    ) {
        val dialog = DialogManager.showLoadingDialog(context = requireActivity())
        dialog.setProgressType(ProgressType.BACKUP)

        lifecycleScope.launch {
            try {
                Manager.downloadFile(
                    context = requireActivity(),
                    fileUri = fileUri,
                    achievementRepository = achievementRepository,
                    historyRepository = historyRepository,
                    settingsRepository = settingsRepository,
                    notificationsSettingsRepository = notificationsSettingsRepository,
                    costsRepository = costsRepository,
                    notesRepository = notesRepository,
                    onProgress = { progress ->
                        dialog.updateProgress(progress)
                    }
                )

                dialog.dismiss()
                onFinished()
            } catch (_: Exception) {
                dialog.dismiss()
            }
        }
    }
}