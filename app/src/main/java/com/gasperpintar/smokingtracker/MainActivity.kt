package com.gasperpintar.smokingtracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gasperpintar.smokingtracker.adapter.Pager
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.databinding.ActivityMainBinding
import com.gasperpintar.smokingtracker.ui.fragment.AnalyticsFragment
import com.gasperpintar.smokingtracker.ui.fragment.GraphFragment
import com.gasperpintar.smokingtracker.ui.fragment.HomeFragment
import com.gasperpintar.smokingtracker.ui.fragment.SettingsFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.Permissions
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import com.gasperpintar.smokingtracker.utils.JsonHelper
import com.gasperpintar.smokingtracker.utils.notifications.Worker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.core.view.size
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationsSettingsRepository: NotificationsSettingsRepository

    lateinit var permissionsHelper: Permissions

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        initViewBinding()
        initPager()
        initPermissions()

        lifecycleScope.launch {
            initializeApplication()
        }
    }

    override fun attachBaseContext(
        context: Context
    ) {
        database = Provider.getDatabase(context = context.applicationContext)
        achievementRepository = AchievementRepository(
            achievementDao = database.achievementDao()
        )
        settingsRepository = SettingsRepository(
            settingsDao = database.settingsDao()
        )
        notificationsSettingsRepository = NotificationsSettingsRepository(
            notificationsSettingsDao = database.notificationsSettingsDao()
        )

        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = settingsRepository
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if (permissionsHelper.isNotificationPermissionGranted()) {
            Notifications.createNotificationChannel(context = this)
            scheduleNotificationWorker()
        }
    }

    private fun initViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initPermissions() {
        permissionsHelper = Permissions(activity = this)
    }

    private suspend fun initializeApplication() {
        val settings: SettingsEntity = getOrCreateDefaultSettings()
        val sharedPreferences: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        handleAppVersioning(sharedPreferences = sharedPreferences)
        applyTheme(themeId = settings.theme)
        handleNotifications(sharedPreferences = sharedPreferences)
    }

    private fun initPager() {
        val fragments = listOf(
            { HomeFragment() },
            { GraphFragment() },
            { AnalyticsFragment() },
            { SettingsFragment() }
        )

        val navigationIds = listOf(
            R.id.navigation_home,
            R.id.navigation_graph,
            R.id.navigation_analytics,
            R.id.navigation_settings
        )

        binding.mainViewPager.adapter = Pager(
            fragmentActivity = this,
            fragmentCreator = fragments
        )

        binding.mainViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position in 0 until binding.navView.menu.size) {
                        binding.navView.menu[position].isChecked = true
                    }
                }
            }
        )

        binding.navView.setOnItemSelectedListener { item ->
            val index: Int = navigationIds.indexOf(item.itemId)
            if (index >= 0) {
                binding.mainViewPager.setCurrentItem(index, false)
            }
            true
        }
    }

    private suspend fun getOrCreateDefaultSettings(): SettingsEntity {
        val defaultSettings = settingsRepository.get() ?: SettingsEntity(
            id = 1,
            theme = 0,
            language = getDefaultLanguageIndex(),
            frequency = 0
        ).also {
            settingsRepository.insert(settings = it)
        }

        notificationsSettingsRepository.get() ?: NotificationsSettingsEntity(
            id = 1,
            system = true,
            achievements = true,
            progress = true
        ).also {
            notificationsSettingsRepository.insert(settings = it)
        }
        return defaultSettings
    }

    private suspend fun handleAppVersioning(
        sharedPreferences: SharedPreferences
    ) {
        val versionName: String? = packageManager.getPackageInfo(packageName, 0).versionName
        val lastVersionName: String? = sharedPreferences.getString("last_version_name", null)

        if (versionName != lastVersionName) {
            JsonHelper(achievementRepository = achievementRepository).initializeAchievementsIfNeeded(context = this)
            sharedPreferences.edit {
                putString("last_version_name", versionName)
            }
            recreate()
        }
    }

    private fun handleNotifications(sharedPreferences: SharedPreferences) {
        val isFirstRun: Boolean = sharedPreferences.getBoolean("first_run", true)

        if (isFirstRun) {
            permissionsHelper.checkAndRequestNotificationPermission { isGranted ->
                if (isGranted) {
                    Notifications.createNotificationChannel(context = this)
                    scheduleNotificationWorker()
                }
            }
            sharedPreferences.edit {
                putBoolean("first_run", false)
            }
        } else if (permissionsHelper.isNotificationPermissionGranted()) {
            Notifications.createNotificationChannel(context = this)
            scheduleNotificationWorker()
        }
    }

    private fun scheduleNotificationWorker() {
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<Worker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

        WorkManager.getInstance(context = this)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = "smoking_notification_work",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    private fun applyTheme(themeId: Int) {
        when (themeId) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> Unit
        }
    }

    private fun getDefaultLanguageIndex(): Int {
        return when (Locale.getDefault().language) {
            "en" -> 1
            "sl" -> 2
            else -> 0
        }
    }
}