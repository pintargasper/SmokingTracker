package com.gasperpintar.smokingtracker.ui.fragment

import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentAboutBinding
import com.gasperpintar.smokingtracker.databinding.ItemAboutPageBinding
import com.gasperpintar.smokingtracker.ui.adapter.PageAdapter
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
        val platforms = listOf(
            PageAdapter.Page(
                title = "GitHub",
                value = "github.com/pintargasper/SmokingTracker",
                url = "https://github.com/pintargasper/SmokingTracker"
            ),
            PageAdapter.Page(
                title = "F-Droid",
                value = "f-droid.org",
                url = "https://f-droid.org/packages/com.gasperpintar.smokingtracker"
            ),
            PageAdapter.Page(
                title = "IzzyOnDroid",
                value = "apt.izzysoft.de",
                url = "https://apt.izzysoft.de/fdroid/index/apk/com.gasperpintar.smokingtracker"
            ),
            PageAdapter.Page(
                title = "OpenAPK",
                value = "openapk.net",
                url = "https://www.openapk.net/smoking-tracker/com.gasperpintar.smokingtracker/"
            )
        )

        val contributors = listOf(
            PageAdapter.Page(
                title = "pintargasper",
                value = "${getString(R.string.about_contributors_code)}, ${getString(R.string.about_contributors_slovenian)}",
                url = "https://github.com/pintargasper"
            ),
            PageAdapter.Page(
                title = "anzepintar",
                value = getString(R.string.about_contributors_code),
                url = "https://github.com/anzepintar"
            ),
            PageAdapter.Page(
                title = "mlnzigzag",
                value = getString(R.string.about_contributors_code),
                url = "https://github.com/mlnzigzag"
            ),
            PageAdapter.Page(
                title = "mrtaxi",
                value = getString(R.string.about_contributors_ukrainian),
                url = "https://github.com/mrtaxi"
            ),
            PageAdapter.Page(
                title = "jocixlinux-sys",
                value = getString(R.string.about_contributors_serbian),
                url = "https://github.com/jocixlinux-sys"
            ),
            PageAdapter.Page(
                title = "iaaanneed",
                value = getString(R.string.about_contributors_russian),
                url = "https://github.com/iaaanneed"
            ),
            PageAdapter.Page(
                title = "ywnzzl",
                value = getString(R.string.about_contributors_chinese_simplified),
                url = "https://github.com/ywnzzl"
            ),
            PageAdapter.Page(
                title = "acidefluorhydrique",
                value = "${getString(R.string.about_contributors_chinese_traditional)}, ${getString(R.string.about_contributors_chinese_simplified)}",
                url = "https://github.com/acidefluorhydrique"
            ),
            PageAdapter.Page(
                title = "nrob81",
                value = getString(R.string.about_contributors_hungarian),
                url = "https://github.com/nrob81"
            )
        )

        PageAdapter.setupPager(pager = platformsViewPager, indicator = platformsPageIndicator,
            pages = platforms, bindingInflater = ItemAboutPageBinding::inflate, onPageClick = { url ->
                requireContext().openUrl(url)
            }
        )

        PageAdapter.setupPager(pager = contributorsViewPager, indicator = contributorsPageIndicator,
            pages = contributors, bindingInflater = ItemAboutPageBinding::inflate, onPageClick = { url ->
                requireContext().openUrl(url)
            }
        )
    }
}