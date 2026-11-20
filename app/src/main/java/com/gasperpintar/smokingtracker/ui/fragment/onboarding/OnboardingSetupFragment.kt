package com.gasperpintar.smokingtracker.ui.fragment.onboarding

import com.gasperpintar.smokingtracker.databinding.FragmentOnboardingSetupBinding
import com.gasperpintar.smokingtracker.ui.fragment.Base

class OnboardingSetupFragment : Base<FragmentOnboardingSetupBinding>(
    bindingInflater = FragmentOnboardingSetupBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        showContent()
    }
}