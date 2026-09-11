package com.gasperpintar.smokingtracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.size
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.viewmodel.MainViewModel
import com.gasperpintar.smokingtracker.databinding.ActivityMainBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.fragment.GraphFragment
import com.gasperpintar.smokingtracker.ui.fragment.HomeFragment
import com.gasperpintar.smokingtracker.ui.fragment.ProgressFragment
import com.gasperpintar.smokingtracker.ui.fragment.SettingsFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.Permissions
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import com.gasperpintar.smokingtracker.utils.notifications.Worker
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appContainer by lazy { (application as Application).container }
    private val viewModel: MainViewModel by viewModels {
        ModelFactory(application = application as Application, container = appContainer)
    }

    var permissionsHelper: Permissions = Permissions(activity = this)

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

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

    @Override
    override fun onResume() {
        super.onResume()
        if (permissionsHelper.isNotificationPermissionGranted) {
            Notifications.createNotificationChannel(context = this)
            scheduleNotificationWorker()
        }
    }

    @Override
    override fun onStart() {
        super.onStart()
        binding.navView.let { navView ->
            (0 until navView.menu.size).forEach { i ->
                navView.findViewById<View>(navView.menu[i].itemId)?.setOnLongClickListener { true }
            }
        }
    }

    private fun initialize() {
        val settings: SettingsEntity = runBlocking { viewModel.getSettings(context = this@MainActivity) }
        applyTheme(themeId = settings.theme)

        val sharedPreferences: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        handleNotifications(sharedPreferences = sharedPreferences)
        setupPager()
    }

    private fun setupPager() = binding.apply {
        mainViewPager.adapter = Pager(
            this@MainActivity,
            listOf(::HomeFragment, ::GraphFragment, ::ProgressFragment, ::SettingsFragment)
        )

        mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @Override
            override fun onPageSelected(position: Int) {
                binding.navView.menu[position].isChecked = true
            }
        })

        navView.setOnItemSelectedListener {
            binding.mainViewPager.setCurrentItem(binding.navView.menu.children.indexOf(it), false)
            true
        }
    }

    private fun handleNotifications(sharedPreferences: SharedPreferences) {
        when {
            sharedPreferences.getBoolean("first_run", true) -> {
                permissionsHelper.checkAndRequestNotificationPermission { isGranted ->
                    if (isGranted) {
                        Notifications.createNotificationChannel(context = this)
                        scheduleNotificationWorker()
                    }
                }
                sharedPreferences.edit { putBoolean("first_run", false) }
            }

            permissionsHelper.isNotificationPermissionGranted -> {
                Notifications.createNotificationChannel(context = this)
                scheduleNotificationWorker()
            }
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
}