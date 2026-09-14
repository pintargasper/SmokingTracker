package com.gasperpintar.smokingtracker.activity

import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.ActivityAboutBinding
import com.gasperpintar.smokingtracker.utils.WebHelper.openUrl

class AboutActivity : Base<ActivityAboutBinding>(
    bindingInflater = ActivityAboutBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            finish()
        }

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName ?: getString(R.string.about_version_unknown)
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)

        appVersion.text = getString(
            R.string.about_version,
            versionName,
            versionCode
        )
        createdBy.text = getString(R.string.about_created_by, "Gašper Pintar")

        setupLinks()
    }

    private fun setupLinks() = binding.apply {
        mapOf(
            githubLayout to "https://github.com/pintargasper/SmokingTracker",
            fDroidLayout to "https://f-droid.org/packages/com.gasperpintar.smokingtracker",
            izzyOnDroidLayout to "https://apt.izzysoft.de/fdroid/index/apk/com.gasperpintar.smokingtracker",
            openApkLayout to "https://www.openapk.net/smoking-tracker/com.gasperpintar.smokingtracker/",
            contributor1Layout to "https://github.com/pintargasper",
            contributor2Layout to "https://github.com/mrtaxi",
            contributor3Layout to "https://github.com/jocixlinux-sys",
            contributor4Layout to "https://github.com/iaanneed",
            contributor5Layout to "https://github.com/ywnzzl",
            contributor6Layout to "https://github.com/acidefluorhydrique",
            contributor7Layout to "https://github.com/nrob81"
        ).forEach { (view, url) ->
            view.setOnClickListener {
                this@AboutActivity.openUrl(url)
            }
        }
    }
}