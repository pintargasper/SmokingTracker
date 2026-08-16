package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.databinding.ActivityAboutBinding
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.WebHelper

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }

        setup()
    }

    override fun attachBaseContext(
        context: Context
    ) {
        database = Provider.getDatabase(context = context.applicationContext)
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())

        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = settingsRepository
            )
        )
    }

    private fun setup() {

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName ?: getString(R.string.settings_category_data_version_unknown)
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)

        binding.appVersion.text = getString(
            R.string.about_version,
            versionName,
            versionCode
        )

        setupLinks()

        binding.createdBy.text = getString(R.string.about_created_by, "Gašper Pintar")
    }

    private fun setupLinks() {
        setupLink(binding.githubLayout, "https://github.com/pintargasper/SmokingTracker")
        setupLink(binding.fDroidLayout, "https://f-droid.org/packages/com.gasperpintar.smokingtracker")
        setupLink(binding.izzyOnDroidLayout, "https://apt.izzysoft.de/fdroid/index/apk/com.gasperpintar.smokingtracker")
        setupLink(binding.openApkLayout, "https://www.openapk.net/smoking-tracker/com.gasperpintar.smokingtracker/")

        setupLink(binding.contributor1Layout, "https://github.com/pintargasper")
        setupLink(binding.contributor2Layout, "https://github.com/mrtaxi")
        setupLink(binding.contributor3Layout, "https://github.com/jocixlinux-sys")
        setupLink(binding.contributor4Layout, "https://github.com/iaanneed")
        setupLink(binding.contributor5Layout, "https://github.com/ywnzzl")
        setupLink(binding.contributor6Layout, "https://github.com/acidefluorhydrique")
    }

    private fun setupLink(view: View, url: String) {
        view.setOnClickListener {
            WebHelper.openUrl(this, url)
        }
    }
}