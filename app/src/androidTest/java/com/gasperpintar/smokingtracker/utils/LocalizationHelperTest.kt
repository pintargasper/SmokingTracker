package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.time.Month

@RunWith(value = AndroidJUnit4::class)
class LocalizationHelperTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = TestProvider.getInMemoryDatabase(context)
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun getLocalizedContextReturnsSystemLocaleWhenLanguageIsSystem() = runBlocking {
        settingsRepository.insert(settings = createSettingsEntity(languageId = 0))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository)
        val expectedLanguage = context.resources.configuration.locales[0].language
        val actualLanguage = localizedContext.resources.configuration.locales[0].language

        assertEquals(expectedLanguage, actualLanguage)
    }

    @Test
    fun getLocalizedContextReturnsEnglishLocaleWhenLanguageIsEnglish() = runBlocking {
        val supportedLanguages = context.resources.getStringArray(R.array.language_values)
        val index = supportedLanguages.indexOf("en")

        database.settingsDao().insert(entity = createSettingsEntity(languageId = index))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository)
        val actualLanguage = localizedContext.resources.configuration.locales[0].language

        assertEquals("en", actualLanguage)
    }

    @Test
    fun getLocalizedContextReturnsSystemLocaleWhenLanguageIdIsInvalid() = runBlocking {
        database.settingsDao().insert(entity = createSettingsEntity(languageId = 999))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository)
        val expectedLanguage = context.resources.configuration.locales[0].language
        val actualLanguage = localizedContext.resources.configuration.locales[0].language

        assertEquals(expectedLanguage, actualLanguage)
    }

    @Test
    fun getDayOfWeekNameReturnsNonEmptyString() {
        for (day in DayOfWeek.entries) {
            val result = LocalizationHelper.getDayOfWeekName(context, dayOfWeek = day)
            assert(result.isNotEmpty())
        }
    }

    @Test
    fun getMonthNameReturnsNonEmptyString() {
        for(month in Month.entries) {
            val result = LocalizationHelper.getMonthName(context, month = month)
            assert(result.isNotEmpty())
        }
    }

    private fun createSettingsEntity(languageId: Int): SettingsEntity {
        return SettingsEntity(
            id = 0,
            theme = 0,
            language = languageId,
        )
    }
}