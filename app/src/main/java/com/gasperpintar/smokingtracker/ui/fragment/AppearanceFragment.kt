package com.gasperpintar.smokingtracker.ui.fragment

import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsState
import com.gasperpintar.smokingtracker.databinding.FragmentAppearanceBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.Appearance
import kotlinx.coroutines.launch

class AppearanceFragment : Base<FragmentAppearanceBinding>(
    bindingInflater = FragmentAppearanceBinding::inflate
) {

    private val viewModel: SettingsViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getState()

            setupDropdown(
                dropdown = themeDropdown,
                options = R.array.appearance_theme_options,
                selectedIndex = state.settings.theme
            ) { selectTheme(state, theme = it) }

            setupDropdown(
                dropdown = schemeDropdown,
                options = R.array.appearance_scheme_options,
                selectedIndex = Appearance.ColorScheme.entries.indexOf(state.settings.colorScheme)
            ) { position ->
                Appearance.ColorScheme.entries.getOrNull(index = position)?.let {
                    selectColorScheme(state, scheme = it)
                }
            }

            setupDropdown(
                dropdown = startPageDropdown,
                options = R.array.appearance_start_page_options,
                selectedIndex = Appearance.StartPage.entries.indexOf(state.settings.startPage)
            ) { position ->
                Appearance.StartPage.entries.getOrNull(index = position)?.let {
                    selectStartPage(state, startPage = it)
                }
            }
            showContent()
        }
    }

    private fun setupDropdown(
        dropdown: AutoCompleteTextView,
        options: Int,
        selectedIndex: Int,
        onSelected: (Int) -> Unit
    ) {
        val items = resources.getStringArray(options)
        dropdown.apply {
            setText(items[selectedIndex.coerceIn(range = items.indices)], false)
            setOnItemClickListener { _, _, position, _ ->
                onSelected(position)
            }
        }
    }

    private fun selectTheme(
        state: SettingsState,
        theme: Int
    ) {
        if (state.settings.theme == theme) return
        viewLifecycleOwner.lifecycleScope.launch {
            state.settings = state.settings.copy(theme = theme)
            viewModel.updateSettings(state.settings)
            (requireActivity().application as Application).applyTheme(themeId = theme)
        }
    }

    private fun selectColorScheme(
        state: SettingsState,
        scheme: Appearance.ColorScheme
    ) {
        if (state.settings.colorScheme == scheme) return
        viewLifecycleOwner.lifecycleScope.launch {
            state.settings = state.settings.copy(colorScheme = scheme)
            viewModel.updateSettings(state.settings)
            requireActivity().recreate()
        }
    }

    private fun selectStartPage(
        state: SettingsState,
        startPage: Appearance.StartPage
    ) {
        if (state.settings.startPage == startPage) return
        viewLifecycleOwner.lifecycleScope.launch {
            state.settings = state.settings.copy(startPage = startPage)
            viewModel.updateSettings(state.settings)
        }
    }
}