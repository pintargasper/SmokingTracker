package com.gasperpintar.smokingtracker.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

open class Permissions(private val activity: ComponentActivity) {

    private val requestNotificationPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean -> permissionCallback?.invoke(isGranted)
        }

    private var permissionCallback: ((Boolean) -> Unit)? = null

    open fun checkAndRequestNotificationPermission(callback: (Boolean) -> Unit) {
        permissionCallback = callback

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> callback(true)
                else -> requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else callback(true)
    }
}