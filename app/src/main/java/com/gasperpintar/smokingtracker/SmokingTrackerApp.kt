package com.gasperpintar.smokingtracker

import android.app.Application
import com.gasperpintar.smokingtracker.di.AppContainer

class SmokingTrackerApp : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(context = this)
    }
}