package com.gasperpintar.smokingtracker

import android.content.Intent
import com.gasperpintar.smokingtracker.databinding.ActivityOnboardingBinding
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.fragment.onboarding.OnboardingFinishFragment
import com.gasperpintar.smokingtracker.ui.fragment.onboarding.OnboardingFragment
import com.gasperpintar.smokingtracker.ui.fragment.onboarding.OnboardingSetupFragment
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : Base<ActivityOnboardingBinding>(
    bindingInflater = ActivityOnboardingBinding::inflate
) {

    @Override
    override fun initialize() {
        setupPager()

        binding.skip.setOnClickListener {
            finishOnboarding()
        }

        binding.continues.setOnClickListener {
            if (binding.onboardingViewPager.currentItem < 2) binding.onboardingViewPager.currentItem++
            else finishOnboarding()
        }
    }

    private fun setupPager() = binding.apply {
        onboardingViewPager.apply {
            adapter = Pager(
                activity = this@OnboardingActivity,
                creators = listOf(
                    { OnboardingFragment() },
                    { OnboardingSetupFragment() },
                    { OnboardingFinishFragment() }
                )
            )
            isUserInputEnabled = false
        }

        TabLayoutMediator(onboardingIndicator, onboardingViewPager) { tab, _ ->
            tab.view.isClickable = false
        }.attach()
    }

    private fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("onboarding_completed", true)
        })
        finish()
    }
}