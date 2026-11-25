package com.gasperpintar.smokingtracker.ui.fragment.onboarding

import com.gasperpintar.smokingtracker.databinding.FragmentOnboardingFinishBinding
import com.gasperpintar.smokingtracker.ui.fragment.Base

class OnboardingFinishFragment : Base<FragmentOnboardingFinishBinding>(
    bindingInflater = FragmentOnboardingFinishBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        showContent()
    }
}