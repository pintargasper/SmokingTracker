package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.core.content.edit
import com.gasperpintar.smokingtracker.type.Preferences

object PreferencesHelper {

    fun with(
        context: Context,
        name: Preferences.Name
    ): Instance {
        return Instance(context, name)
    }

    class Instance(
        context: Context,
        name: Preferences.Name
    ) {

        @PublishedApi
        internal val preferences = context.applicationContext.getSharedPreferences(name.value, Context.MODE_PRIVATE)

        inline fun <reified T> get(
            key: Preferences.Key,
            defaultValue: T
        ): T {
            return when (T::class) {
                String::class -> preferences.getString(key.value, defaultValue as String?) as T
                Boolean::class -> preferences.getBoolean(key.value, defaultValue as Boolean) as T
                Int::class -> preferences.getInt(key.value, defaultValue as Int) as T
                Long::class -> preferences.getLong(key.value, defaultValue as Long) as T
                Float::class -> preferences.getFloat(key.value, defaultValue as Float) as T
                else -> error("Unsupported type: ${T::class}")
            }
        }

        fun put(
            key: Preferences.Key,
            value: Any
        ) {
            preferences.edit {
                when (value::class) {
                    String::class -> putString(key.value, value as String)
                    Boolean::class -> putBoolean(key.value, value as Boolean)
                    Int::class -> putInt(key.value, value as Int)
                    Long::class -> putLong(key.value, value as Long)
                    Float::class -> putFloat(key.value, value as Float)
                    else -> error("Unsupported type: ${value::class}")
                }
            }
        }
    }
}