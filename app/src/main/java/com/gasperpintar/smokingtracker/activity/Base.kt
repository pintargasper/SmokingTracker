package com.gasperpintar.smokingtracker.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.utils.LocalizationHelper

abstract class Base<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    @Override
    override fun attachBaseContext(
        newBase: Context
    ) {
        val repository = (newBase.applicationContext as Application).container.settingsRepository
        val localizedContext = LocalizationHelper.getLocalizedContext(context = newBase, settingsRepository = repository)
        super.attachBaseContext(localizedContext)
    }

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
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