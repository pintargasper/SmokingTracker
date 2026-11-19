package com.gasperpintar.smokingtracker.ui.fragment

import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentAboutBinding
import com.gasperpintar.smokingtracker.databinding.ItemAboutPageBinding
import com.gasperpintar.smokingtracker.ui.adapter.PageAdapter
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

        PageAdapter.setupPager(
            pager = platformsViewPager,
            indicator = platformsPageIndicator,
            pages = platforms,
            bindingInflater = ItemAboutPageBinding::inflate,
            onPageClick = { url ->
                requireContext().openUrl(url)
            }
        )

        PageAdapter.setupPager(
            pager = contributorsViewPager,
            indicator = contributorsPageIndicator,
            pages = contributors,
            bindingInflater = ItemAboutPageBinding::inflate,
            onPageClick = { url ->
                requireContext().openUrl(url)
            }
        )
    }
}