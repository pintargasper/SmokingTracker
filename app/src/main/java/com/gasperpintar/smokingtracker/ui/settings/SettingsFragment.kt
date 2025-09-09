package com.gasperpintar.smokingtracker.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: Lazy<AppDatabase>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = lazy { (requireActivity() as MainActivity).database }

        setup()

        return root
    }

    private fun setup() {
        lifecycleScope.launch {
            setupSettings()
        }
        setupAbout()
    }

    private suspend fun setupSettings() {
        val settingsDao = database.value.settingsDao()
        val currentSettings = settingsDao.getSettings() ?: insertDefaultSettings(settingsDao)

        with(receiver = binding) {
            spinnerTheme.setSelection(currentSettings.theme)
            spinnerLanguage.setSelection(currentSettings.language)
            switchNotifications.isChecked = currentSettings.notifications == 1
        }

        setupThemeListener(settingsDao, currentSettings)
        setupLanguageListener(settingsDao, currentSettings)
        setupNotificationsListener(settingsDao, currentSettings)
    }

    private suspend fun insertDefaultSettings(settingsDao: SettingsDao): SettingsEntity {
        val defaultSettings = SettingsEntity(id = 0L, theme = 0, language = 0, notifications = 1)
        settingsDao.insert(settingsEntity = defaultSettings)
        return defaultSettings
    }

    private fun setupThemeListener(
        settingsDao: SettingsDao,
        currentSettings: SettingsEntity
    ) {
        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val updatedSettings = currentSettings.copy(theme = position)
                    updateSettings(settingsDao, updatedSettings)
                    (requireActivity() as MainActivity).applyTheme(themeId = position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupLanguageListener(
        settingsDao: SettingsDao,
        currentSettings: SettingsEntity
    ) {
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val updatedSettings = currentSettings.copy(language = position)
                    updateSettings(settingsDao, updatedSettings)

                    requireContext()
                        .getSharedPreferences("settings", Context.MODE_PRIVATE)
                        .edit { putInt("language", position) }

                    requireActivity().recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupNotificationsListener(
        settingsDao: SettingsDao,
        currentSettings: SettingsEntity
    ) {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                val updatedSettings = currentSettings.copy(notifications = if (isChecked) 1 else 0)
                updateSettings(settingsDao, updatedSettings)
            }
        }
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

    private suspend fun updateSettings(settingsDao: SettingsDao, updatedSettings: SettingsEntity) {
        settingsDao.update(settingsEntity = updatedSettings)
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}