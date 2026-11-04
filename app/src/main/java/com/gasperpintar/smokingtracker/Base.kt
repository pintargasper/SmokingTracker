package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker.type.ColorScheme
import com.gasperpintar.smokingtracker.utils.LocalizationHelper

abstract class Base<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private lateinit var application: Application

    @Override
    override fun attachBaseContext(
        newBase: Context
    ) {
        application = newBase.applicationContext as Application
        val localizedContext = LocalizationHelper.getLocalizedContext(context = newBase, settingsRepository = application.container.settingsRepository)
        super.attachBaseContext(localizedContext)
    }

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        // TODO add implementation to the database (@AppearanceFragment.kt)
        // val colorScheme = runBlocking { application.container.settingsRepository.get()?.colorScheme }
        // setTheme(ColorScheme.fromName(name = colorScheme).themeResId)

        val colorSchemes = listOf("SYSTEM", "LEGACY", "SKY")
        setTheme(ColorScheme.fromName(name = colorSchemes.random()).themeResId)

        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected abstract fun initialize(): Any?
}