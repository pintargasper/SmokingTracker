package com.gasperpintar.smokingtracker.ui.fragment

import androidx.appcompat.widget.AppCompatRadioButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsState
import com.gasperpintar.smokingtracker.databinding.FragmentAppearanceBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.ColorScheme
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
            val theme = listOf(systemThemeRadio, lightThemeRadio, darkThemeRadio)
            val colorScheme = listOf(systemSchemeRadio, legacySchemeRadio, skySchemeRadio)

            theme.forEachIndexed { index, radio ->
                radio.isChecked = state.settings.theme == index
                radio.setOnClickListener {
                    selectTheme(state = state, radios = theme, theme = index)
                }
            }

            listOf(systemTheme, lightTheme, darkTheme).forEachIndexed { index, card ->
                card.setOnClickListener {
                    selectTheme(state = state, radios = theme, theme = index)
                }
            }

            colorScheme.forEachIndexed { index, radio ->
                radio.isChecked = state.settings.theme == index
                radio.setOnClickListener {
                    selectColorScheme(state = state, radios = colorScheme, scheme = ColorScheme.entries[index])
                }
            }

            listOf(systemScheme, legacyScheme, skyScheme).forEachIndexed { index, card ->
                card.setOnClickListener {
                    selectColorScheme(state = state, radios = colorScheme, scheme = ColorScheme.entries[index])
                }
            }

            showContent()
        }
    }

    private fun selectTheme(
        state: SettingsState,
        radios: List<AppCompatRadioButton>,
        theme: Int
    ) {
        if (state.settings.theme == theme) return
        viewLifecycleOwner.lifecycleScope.launch {
            state.settings = state.settings.copy(theme = theme)
            viewModel.updateSettings(state.settings)

            (requireActivity().application as Application).applyTheme(themeId = theme)
            radios.forEachIndexed { index, radio ->
                radio.isChecked = index == theme
            }
        }
    }

    private fun selectColorScheme(
        state: SettingsState,
        radios: List<AppCompatRadioButton>,
        scheme: ColorScheme
    ) {
        // TODO TODO add implementation to the database (@Base.kt)
        // if (scheme.fromName(state.settings.colorScheme) == scheme) return
        viewLifecycleOwner.lifecycleScope.launch {
            // state.settings = state.settings.copy(colorScheme = scheme.name)
            // viewModel.updateSettings(state.settings)

            radios.forEachIndexed { index, radio ->
                radio.isChecked = ColorScheme.entries[index] == scheme
            }
            requireActivity().recreate()
        }
    }
}