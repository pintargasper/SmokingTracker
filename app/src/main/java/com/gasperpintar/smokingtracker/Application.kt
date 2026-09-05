package com.gasperpintar.smokingtracker

import android.app.Application
import com.gasperpintar.smokingtracker.di.Container

class Application : Application() {

    val container: Container by lazy {
        Container(context = this)
    }
}