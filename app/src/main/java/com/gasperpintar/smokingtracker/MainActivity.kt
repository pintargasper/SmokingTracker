package com.gasperpintar.smokingtracker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var permissionsHelper: Permissions
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

        database = Provider.getDatabase(context = this)
        permissionsHelper = Permissions(activity = this)

        permissionsHelper.checkAndRequestNotificationPermission { isGranted ->
            if (isGranted) {
                Notifications.createNotificationChannel(context = this)
                scheduleNotificationWorker()
            }
        }
    }

    private fun scheduleNotificationWorker() {
        val workManager = WorkManager.getInstance(context = this)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<Worker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
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
        val sharedPreferences = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val languageId = sharedPreferences.getInt("language", 0)
        val supportedLanguages = newBase.resources.getStringArray(R.array.language_values)

        val selectedLanguage = supportedLanguages.getOrNull(languageId) ?: "system"

        val locale: Locale =
            if (selectedLanguage == "system") newBase.resources.configuration.locales.get(0)
            else Locale.forLanguageTag(selectedLanguage)

        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }
}