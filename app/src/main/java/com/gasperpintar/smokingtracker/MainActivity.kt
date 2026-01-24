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
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var settingsRepository: SettingsRepository

    lateinit var permissionsHelper: Permissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createPager()

        permissionsHelper = Permissions(activity = this@MainActivity)

        lifecycleScope.launch {
            val settings: SettingsEntity? = settingsRepository.get()

            val sharedPreferences: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
            val isFirstRun: Boolean = sharedPreferences.getBoolean("first_run", true)
            val themeId: Int = settings?.theme ?: 0

            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            val lastVersionName = sharedPreferences.getString("last_version_name", null)

            if (versionName != lastVersionName) {
                JsonHelper(achievementRepository = achievementRepository).initializeAchievementsIfNeeded(context = this@MainActivity)
                sharedPreferences.edit { putString("last_version_name", versionName) }
            }

            applyTheme(themeId = themeId)
            if (isFirstRun || settings?.notifications == 1) {
                permissionsHelper.checkAndRequestNotificationPermission { isGranted ->
                    if (isGranted) {
                        Notifications.createNotificationChannel(context = this@MainActivity)
                        scheduleNotificationWorker()
                    }
                }

                if (isFirstRun) {
                    sharedPreferences.edit { putBoolean("first_run", false) }
                }
            }
        }
    }

    private fun createPager() {
        val navigationView = binding.navView
        val viewPager = binding.mainViewPager

        val fragments = listOf(
            { HomeFragment() },
            { GraphFragment() },
            { AnalyticsFragment() },
            { SettingsFragment() }
        )

        val tabMenuIds = listOf(
            R.id.navigation_home,
            R.id.navigation_graph,
            R.id.navigation_analytics,
            R.id.navigation_settings
        )

        viewPager.adapter = Pager(
            fragmentActivity = this,
            fragmentCreator = fragments
        )
        viewPager.isUserInputEnabled = true

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position in 0 until navigationView.menu.size) {
                    navigationView.menu[position].isChecked = true
                }
            }
        })

        navigationView.setOnItemSelectedListener { item ->
            val index = tabMenuIds.indexOf(item.itemId)
            if (index >= 0) {
                viewPager.setCurrentItem(index, false)
            }
            true
        }
    }

    private fun scheduleNotificationWorker() {
        val workManager: WorkManager = WorkManager.getInstance(context = this@MainActivity)

        val workRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<Worker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "notification_and_achievement_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun applyTheme(themeId: Int) {
        when (themeId) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun attachBaseContext(context: Context) {
        database = Provider.getDatabase(context = context.applicationContext)
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        super.attachBaseContext(LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository))
    }
}