package com.gasperpintar.smokingtracker.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.AboutActivity
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsState
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.bar.ProgressType
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.FileHelper
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatLocalized
import com.gasperpintar.smokingtracker.utils.WebHelper.openUrl
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    private lateinit var exportDocumentLauncher: ActivityResultLauncher<String>
    private lateinit var importDocumentLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var selectedFile: TextView

    private val mimeExcel = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            setupSettings()
        }

        setupDataManagement()
        setupLaunchers()

        aboutLayout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

    private suspend fun setupSettings() = binding.apply {
        val state = viewModel.getSettings()
        updateUi(state = state)

        themeLayout.setOnClickListener {
            DialogManager.showThemeDialog(
                context = requireActivity(),
                selectedTheme = state.settings.theme,
                onThemeSelected = { theme ->
                    lifecycleScope.launch {
                        state.settings = state.settings.copy(theme = theme)
                        viewModel.updateSettings(state.settings)
                        requireActivity().recreate()
                    }
                }
            )
        }

        languageLayout.setOnClickListener {
            DialogManager.showLanguageDialog(
                context = requireActivity(),
                selectedLanguage = state.settings.language,
                onLanguageSelected = { language ->
                    lifecycleScope.launch {
                        state.settings = state.settings.copy(language = language)
                        viewModel.updateSettings(state.settings)
                        requireActivity().recreate()
                    }
                }
            )
        }

        notificationsLayout.setOnClickListener {
            if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                startActivity(Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                    putExtra("android.provider.extra.APP_PACKAGE", requireContext().packageName)
                })
                return@setOnClickListener
            }

            DialogManager.showNotificationsDialog(
                context = requireActivity(),
                settings = state.settings,
                notificationsSettings = state.notificationsSettings,
                onSettingsSelected = { updatedSettings ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        state.settings = updatedSettings
                        viewModel.updateSettings(updatedSettings)
                    }
                },
                onNotificationSettingsSelected = { updatedNotificationSettings ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        state.notificationsSettings = updatedNotificationSettings
                        viewModel.updateNotificationSettings(updatedNotificationSettings)
                    }
                }
            )
        }

        currencyLayout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                DialogManager.showCurrencyDialog(
                    context = requireActivity(),
                    settings = state.settings,
                    onCurrencySelected = { currency, custom ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            state.settings = state.settings.copy(currency = currency, customCurrency = custom)
                            viewModel.updateSettings(state.settings)
                        }
                    }
                )
            }
        }

        costsLayout.setOnClickListener {
            DialogManager.showCostsDialog(
                context = requireActivity(),
                costs = state.costs,
                currency = state.settings.currency,
                onDelete = viewModel::deleteCost,
                onCostAdded = { viewModel.addCost(CostEntry.fromEntity(it)) },
                onRefresh = { viewModel.getSettings().costs }
            )
        }

        val links = mapOf(
            websiteLayout to "https://gasperpintar.com/smoking-tracker",
            changelogLayout to "https://github.com/pintargasper/SmokingTracker/releases",
            translateLayout to "https://translate.gasperpintar.com/projects/smokingtracker",
            privacyPolicyLayout to "https://gasperpintar.com/smoking-tracker/privacy-policy"
        )
        links.forEach { (view, url) ->
            view.setOnClickListener { requireContext().openUrl(url) }
        }
    }

    private fun updateUi(state: SettingsState) = binding.apply {
        imageTheme.setImageResource(updateThemeIcon(state.settings.theme))
        themeService.text = resources.getStringArray(R.array.theme_names)[state.settings.theme]
        languageService.text = resources.getStringArray(R.array.language_names)[state.settings.language]
    }

    private fun setupDataManagement() = binding.apply {
        backupLayout.setOnClickListener {
            DialogManager.showBackupDialog(context = requireActivity()) {
                val fileName = "st_data_${LocalDateTime.now().formatLocalized()}"
                try {
                    exportDocumentLauncher.launch(fileName)
                } catch (_: ActivityNotFoundException) {
                    exportViaShareIntent(fileName)
                }
            }
        }

        restoreLayout.setOnClickListener {
            DialogManager.showRestoreDialog(
                context = requireActivity(),
                onOpenFile = { importDocumentLauncher.launch(arrayOf(mimeExcel)) },
                onConfirm = ::restoreFile,
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
    }

    private fun setupLaunchers() {
        importDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            if (::selectedFile.isInitialized) {
                val fileName = FileHelper.getFileName(requireActivity(), uri)
                selectedFile.text = getString(R.string.restore_popup_file, fileName)
                selectedFile.tag = uri
            }
        }

        exportDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument(mimeExcel)) { uri ->
            uri?.let { exportFile(fileUri = it) }
        }
    }

    private fun updateThemeIcon(theme: Int): Int {
        val isNight = theme == 2 || (theme != 1 &&
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
        return if (isNight) R.drawable.dark_mode_48px
        else R.drawable.light_mode_48px
    }

    private fun restoreFile() {
        if (!::selectedFile.isInitialized) {
            return
        }

        val uri = selectedFile.tag as? Uri ?: return

        val dialog = DialogManager.showLoadingDialog(context = requireActivity()).apply {
            setProgressType(ProgressType.RESTORE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.restoreFile(
                context = requireActivity(),
                fileUri = uri,
                onProgress = dialog::updateProgress,
                onFinished = {
                    dialog.dismiss()
                    requireActivity().recreate()
                },
                onError = dialog::dismiss
            )
        }
    }

    private fun exportViaShareIntent(fileName: String) {
        val context = requireContext()
        val cacheFile = File(context.cacheDir, "$fileName.xlsx").apply { if (exists()) delete() }

        exportFile(fileUri = Uri.fromFile(cacheFile)) {
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeExcel
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.backup_popup)))
        }
    }

    private fun exportFile(fileUri: Uri, onFinished: () -> Unit = {}) {
        val dialog = DialogManager.showLoadingDialog(context = requireActivity()).apply {
            setProgressType(ProgressType.BACKUP)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exportFile(
                context = requireContext(),
                fileUri = fileUri,
                onProgress = dialog::updateProgress,
                onFinished = {
                    dialog.dismiss()
                    onFinished()
                },
                onError = dialog::dismiss
            )
        }
    }
}