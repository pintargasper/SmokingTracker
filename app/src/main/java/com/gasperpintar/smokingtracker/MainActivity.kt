package com.gasperpintar.smokingtracker

import android.content.Intent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gasperpintar.smokingtracker.database.viewmodel.MainViewModel
import com.gasperpintar.smokingtracker.databinding.ActivityMainBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.Appearance
import com.gasperpintar.smokingtracker.type.Preferences
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.GraphFragment
import com.gasperpintar.smokingtracker.ui.fragment.HomeFragment
import com.gasperpintar.smokingtracker.ui.fragment.MinimalHomeFragment
import com.gasperpintar.smokingtracker.ui.fragment.MoreFragment
import com.gasperpintar.smokingtracker.ui.fragment.SettingsFragment
import com.gasperpintar.smokingtracker.utils.Permissions
import com.gasperpintar.smokingtracker.utils.PreferencesHelper
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import com.gasperpintar.smokingtracker.utils.notifications.Worker
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class MainActivity : Base<ActivityMainBinding>(
    bindingInflater = ActivityMainBinding::inflate
) {

    private val viewModel: MainViewModel by viewModels {
        ModelFactory(container = (application as Application).container)
    }

    var permissionsHelper: Permissions = Permissions(activity = this)

    @Override
    override fun initialize() {
        val state = runBlocking { viewModel.getState(context = this@MainActivity) }

        val fromOnboarding = intent.getBooleanExtra("onboarding_completed", false)

        PreferencesHelper.with(context = this, name = Preferences.Name.SETTINGS).let { preferences ->
            if (preferences.get(key = Preferences.Key.FIRST_RUN, defaultValue = true)) {
                preferences.put(key = Preferences.Key.FIRST_RUN, false)
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
                return
            }
        }

        setupPager(startPage = state.startPage)
        setupNavigation()
        setupSubNavigation()

        if (fromOnboarding) handleNotifications()
        if (state.isNewVersion) DialogManager.showChangelogDialog(context = this)
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

    fun openSubFragment(fragment: Fragment, tag: String = fragment::class.java.simpleName) {
        if (supportFragmentManager.backStackEntryCount > 0) return
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(binding.subFragmentContainer.id, fragment, tag)
            addToBackStack(tag)
        }
    }

    private fun setupPager(startPage: Appearance.StartPage) = binding.apply {
        mainViewPager.adapter = Pager(
            this@MainActivity,
            creators = listOf(::MinimalHomeFragment, ::HomeFragment, ::GraphFragment, ::MoreFragment, ::SettingsFragment)
        )

        mainViewPager.setCurrentItem(startPage.ordinal, false)
        navView.menu[startPage.ordinal].isChecked = true

        mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @Override
            override fun onPageSelected(position: Int) {
                navView.menu[position].isChecked = true
            }
        })

        navView.setOnItemSelectedListener {
            mainViewPager.setCurrentItem(navView.menu.children.indexOf(element = it), false)
            true
        }
    }

    private fun setupNavigation() {
        onBackPressedDispatcher.addCallback(
            owner = this,
            onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
                @Override
                override fun handleOnBackPressed() {
                    when {
                        supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
                        binding.mainViewPager.currentItem != 0 -> binding.mainViewPager.setCurrentItem(0, false)
                        else -> {
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        )
    }

    private fun setupSubNavigation() {
        supportFragmentManager.addOnBackStackChangedListener {
            updateSubNavigationVisibility()
        }
        updateSubNavigationVisibility()
    }

    private fun updateSubNavigationVisibility() {
        val isSubPageVisible = supportFragmentManager.backStackEntryCount > 0
        binding.mainContent.visibility = if (isSubPageVisible) View.GONE else View.VISIBLE
        binding.subFragmentContainer.visibility = if (isSubPageVisible) View.VISIBLE else View.GONE
    }

    private fun handleNotifications() {
        if (permissionsHelper.isNotificationPermissionGranted) {
            Notifications.createNotificationChannel(context = this)
            scheduleNotificationWorker()
        } else {
            permissionsHelper.checkAndRequestNotificationPermission { isGranted ->
                if (isGranted) {
                    Notifications.createNotificationChannel(context = this)
                    scheduleNotificationWorker()
                }
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
}