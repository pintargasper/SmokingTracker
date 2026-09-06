package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.databinding.ActivityAboutBinding
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.WebHelper

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)

        initialize()

        setContentView(binding.root)
    }

    @Override
    override fun attachBaseContext(
        context: Context
    ) {
        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = (context.applicationContext as Application).container.settingsRepository
            )
        )
    }

    private fun initialize() = with(receiver = binding) {
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

    private fun setupLinks() = with(receiver = binding) {
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
            contributor6Layout to "https://github.com/acidefluorhydrique"
        ).forEach { (view, url) ->
            view.setOnClickListener {
                WebHelper.openUrl(context = this@AboutActivity, url)
            }
        }
    }
}