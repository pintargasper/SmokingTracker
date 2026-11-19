package com.gasperpintar.smokingtracker.ui.fragment

import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentAboutBinding
import com.gasperpintar.smokingtracker.databinding.ItemAboutBinding
import com.gasperpintar.smokingtracker.ui.adapter.ItemAdapter
import com.gasperpintar.smokingtracker.utils.JsonHelper
import com.gasperpintar.smokingtracker.utils.WebHelper.openUrl

class AboutFragment : Base<FragmentAboutBinding>(
    bindingInflater = FragmentAboutBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionName = packageInfo.versionName ?: getString(R.string.about_version_unknown)
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)

        appVersion.text = getString(R.string.about_version, versionName, versionCode)
        createdBy.text = getString(R.string.about_created_by, "Gašper Pintar")

        setupPagers()
        showContent()
    }

    private fun setupPagers() = binding.apply {
        val (platforms, contributors) = JsonHelper().initializeAbout(context = requireContext())

        ItemAdapter.setupPager(
            pager = platformsViewPager,
            indicator = platformsPageIndicator,
            items = platforms,
            bindingInflater = ItemAboutBinding::inflate,
            onPageClick = { url ->
                requireContext().openUrl(url)
            }
        )

        ItemAdapter.setupPager(
            pager = contributorsViewPager,
            indicator = contributorsPageIndicator,
            items = contributors,
            bindingInflater = ItemAboutBinding::inflate,
            onPageClick = { url ->
                requireContext().openUrl(url)
            }
        )
    }
}