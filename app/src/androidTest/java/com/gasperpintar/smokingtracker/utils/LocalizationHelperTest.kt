package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class LocalizationHelperTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = TestProvider.getInMemoryDatabase(context)
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun getLocalizedContextReturnsSystemLocaleWhenLanguageIsSystem() = runBlocking {
        database.settingsDao().insert(settingsEntity = createSettingsEntity(languageId = 0))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, database = database)
        val expectedLanguage = context.resources.configuration.locales[0].language
        val actualLanguage = localizedContext.resources.configuration.locales[0].language

        assertEquals(expectedLanguage, actualLanguage)
    }

    @Test
    fun getLocalizedContextReturnsEnglishLocaleWhenLanguageIsEnglish() = runBlocking {
        val supportedLanguages = context.resources.getStringArray(R.array.language_values)
        val index = supportedLanguages.indexOf("en")

        database.settingsDao().insert(settingsEntity = createSettingsEntity(languageId = index))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, database = database)
        val actualLanguage = localizedContext.resources.configuration.locales[0].language

        assertEquals("en", actualLanguage)
    }

    @Test
    fun getLocalizedContextReturnsSystemLocaleWhenLanguageIdIsInvalid() = runBlocking {
        database.settingsDao().insert(settingsEntity = createSettingsEntity(languageId = 999))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, database = database)
        val expectedLanguage = context.resources.configuration.locales[0].language
        val actualLanguage = localizedContext.resources.configuration.locales[0].language

        assertEquals(expectedLanguage, actualLanguage)
    }

    private fun createSettingsEntity(languageId: Int): SettingsEntity {
        return SettingsEntity(
            id = 0,
            theme = 0,
            language = languageId,
            notifications = 0
        )
    }
}