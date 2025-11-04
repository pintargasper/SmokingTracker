package com.gasperpintar.smokingtracker

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.databinding.ActivityMainBinding
import com.gasperpintar.smokingtracker.utils.Permissions
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import com.gasperpintar.smokingtracker.utils.notifications.Worker
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import androidx.work.PeriodicWorkRequest
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var permissionsHelper: Permissions
    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navigationView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navigationView.setupWithNavController(navController)

        database = Provider.getDatabase(context = this@MainActivity)
        permissionsHelper = Permissions(activity = this@MainActivity)

        lifecycleScope.launch {
            val settings: SettingsEntity? = database.settingsDao().getSettings()

            val sharedPreferences: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
            val isFirstRun: Boolean = sharedPreferences.getBoolean("first_run", true)
            val themeId: Int = sharedPreferences.getInt("theme", 0)

            applyTheme(themeId = themeId)
            if (isFirstRun || settings?.notifications == 1) {
                permissionsHelper.checkAndRequestNotificationPermission { isGranted ->
                    if (isGranted) {
                        Notifications.createNotificationChannel(this@MainActivity)
                        scheduleNotificationWorker()
                    }
                }

                if (isFirstRun) {
                    sharedPreferences.edit { putBoolean("first_run", false) }
                }
            }
        }
    }

    private fun scheduleNotificationWorker() {
        val workManager: WorkManager = WorkManager.getInstance(context = this@MainActivity)

        val periodicWorkRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<Worker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "smoking_notification_work",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    fun applyTheme(themeId: Int) {
        when (themeId) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val sharedPreferences: SharedPreferences = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val languageId: Int = sharedPreferences.getInt("language", 0)
        val supportedLanguages: Array<out String?> = newBase.resources.getStringArray(R.array.language_values)

        val selectedLanguage: String = supportedLanguages.getOrNull(index = languageId) ?: "system"

        val locale: Locale =
            if (selectedLanguage == "system") newBase.resources.configuration.locales.get(0)
            else Locale.forLanguageTag(selectedLanguage)

        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val context: Context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }
}