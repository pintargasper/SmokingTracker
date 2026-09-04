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
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.SmokingTrackerApp
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import com.gasperpintar.smokingtracker.di.AppModelFactory
import com.gasperpintar.smokingtracker.ui.bar.ProgressType
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.FileHelper
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.WebHelper
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        AppModelFactory(appContainer = (requireActivity().application as SmokingTrackerApp).appContainer)
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
        setupLaunchers()
        observeState()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        setupSettings()
        setupDataManagement()

        binding.aboutLayout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val settings = state.settings ?: return@collect
                binding.imageTheme.setImageResource(updateThemeIcon(settings.theme))
                binding.themeService.text = resources.getStringArray(R.array.theme_names).toList()[settings.theme]
                binding.languageService.text = resources.getStringArray(R.array.language_names).toList()[settings.language]
            }
        }
    }

    private fun setupSettings() {
        binding.themeLayout.setOnClickListener {
            val settings = viewModel.uiState.value.settings ?: return@setOnClickListener
            DialogManager.showThemeDialog(
                context = requireActivity(),
                selectedTheme = settings.theme,
                onThemeSelected = { theme ->
                    lifecycleScope.launch {
                        viewModel.updateSettings { it.copy(theme = theme) }
                        requireActivity().recreate()
                    }
                }
            )
        }

        binding.languageLayout.setOnClickListener {
            val settings = viewModel.uiState.value.settings ?: return@setOnClickListener
            DialogManager.showLanguageDialog(
                context = requireActivity(),
                selectedLanguage = settings.language,
                onLanguageSelected = { language ->
                    lifecycleScope.launch {
                        viewModel.updateSettings { it.copy(language = language) }
                        requireActivity().recreate()
                    }
                }
            )
        }

        binding.notificationsLayout.setOnClickListener {
            if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                startActivity(Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                    putExtra("android.provider.extra.APP_PACKAGE", requireContext().packageName)
                })
                return@setOnClickListener
            }

            val state = viewModel.uiState.value
            val settings = state.settings ?: return@setOnClickListener
            val notificationsSettings = state.notificationsSettings ?: return@setOnClickListener

            DialogManager.showNotificationsDialog(
                context = requireActivity(),
                settings = settings,
                notificationsSettings = notificationsSettings,
                onSettingsSelected = { updatedSettings ->
                    lifecycleScope.launch {
                        viewModel.updateSettings { updatedSettings }
                    }
                },
                onNotificationSettingsSelected = viewModel::updateNotificationSettings
            )
        }

        binding.currencyLayout.setOnClickListener {
            val settings = viewModel.uiState.value.settings ?: return@setOnClickListener
            DialogManager.showCurrencyDialog(
                context = requireActivity(),
                settings = settings,
                onCurrencySelected = { currency, custom ->
                    lifecycleScope.launch {
                        viewModel.updateSettings { it.copy(currency = currency, customCurrency = custom) }
                    }
                }
            )
        }

        binding.costsLayout.setOnClickListener {
            val settings = viewModel.uiState.value.settings ?: return@setOnClickListener
            DialogManager.showCostsDialog(
                context = requireActivity(),
                costs = viewModel.uiState.value.costs,
                currency = settings.currency,
                onDelete = viewModel::deleteCost,
                onCostAdded = { viewModel.addCost(CostEntry.fromEntity(it)) },
                onRefresh = {
                    viewModel.uiState.value.costs
                }
            )
        }

        val links = mapOf(
            binding.websiteLayout to "https://gasperpintar.com/smoking-tracker",
            binding.changelogLayout to "https://github.com/pintargasper/SmokingTracker/releases",
            binding.translateLayout to "https://translate.gasperpintar.com/projects/smokingtracker",
            binding.privacyPolicyLayout to "https://gasperpintar.com/smoking-tracker/privacy-policy"
        )
        links.forEach { (view, url) ->
            view.setOnClickListener { WebHelper.openUrl(context = requireContext(), url) }
        }
    }

    private fun setupDataManagement() {
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

    private fun updateThemeIcon(theme: Int): Int = when (theme) {
        1 -> R.drawable.light_mode_48px
        2 -> R.drawable.dark_mode_48px
        else -> {
            val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            if (isNight) R.drawable.dark_mode_48px else R.drawable.light_mode_48px
        }
    }

    private fun restoreFile() {
        if (!::selectedFile.isInitialized) {
            return
        }

        val uri = selectedFile.tag as? Uri ?: return

        val dialog = DialogManager.showLoadingDialog(requireActivity()).apply {
            setProgressType(ProgressType.RESTORE)
        }

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
        val dialog = DialogManager.showLoadingDialog(requireActivity()).apply {
            setProgressType(ProgressType.BACKUP)
        }

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