package com.gasperpintar.smokingtracker.ui.fragment.onboarding

import com.gasperpintar.smokingtracker.databinding.FragmentOnboardingBinding
import com.gasperpintar.smokingtracker.ui.fragment.Base

class OnboardingFragment : Base<FragmentOnboardingBinding>(
    bindingInflater = FragmentOnboardingBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        showContent()
    }
}