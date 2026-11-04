package com.gasperpintar.smokingtracker.ui.fragment

import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentAboutBinding
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

        setupLinks()
        showContent()
    }

    private fun setupLinks() = binding.apply {
        mapOf(
            githubLayout to "https://github.com/pintargasper/SmokingTracker",
            fDroidLayout to "https://f-droid.org/packages/com.gasperpintar.smokingtracker",
            izzyOnDroidLayout to "https://apt.izzysoft.de/fdroid/index/apk/com.gasperpintar.smokingtracker",
            openApkLayout to "https://www.openapk.net/smoking-tracker/com.gasperpintar.smokingtracker/",
            contributor1Layout to "https://github.com/pintargasper",
            contributor2Layout to "https://github.com/anzepintar",
            contributor3Layout to "https://github.com/mrtaxi",
            contributor4Layout to "https://github.com/jocixlinux-sys",
            contributor5Layout to "https://github.com/iaanneed",
            contributor6Layout to "https://github.com/ywnzzl",
            contributor7Layout to "https://github.com/acidefluorhydrique",
            contributor8Layout to "https://github.com/nrob81"
        ).forEach { (view, url) ->
            view.setOnClickListener {
                requireContext().openUrl(url)
            }
        }
    }
}