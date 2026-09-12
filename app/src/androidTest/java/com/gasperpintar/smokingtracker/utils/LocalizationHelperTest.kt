package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatLocalized
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.Locale

@RunWith(value = AndroidJUnit4::class)
class LocalizationHelperTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    private val languageSystem = 0
    private val languageEnglish = "en"

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
        settingsRepository.insert(settings = SettingsEntity.default(language = 0))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository)
        val expected = context.resources.configuration.locales[0].language
        val actual = localizedContext.resources.configuration.locales[0].language

        assertEquals(expected, actual)
    }

    @Test
    fun getLocalizedContextReturnsEnglishLocaleWhenLanguageIsEnglish() = runBlocking {
        val languageValues = context.resources.getStringArray(R.array.language_values)
        val englishIndex = languageValues.indexOf(languageEnglish)

        assertTrue("English language must exist in language values", englishIndex >= 0)

        settingsRepository.insert(settings = SettingsEntity.default(language = englishIndex))

        val actual = LocalizationHelper.getLocalizedContext(context, settingsRepository).resources.configuration.locales[0].language

        assertEquals(languageEnglish, actual)
    }

    @Test
    fun getLocalizedContextReturnsSystemLocaleWhenLanguageIdIsInvalid() = runBlocking {
        settingsRepository.insert(settings = SettingsEntity.default(language = 999))

        val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository)
        val expected = context.resources.configuration.locales[0].language
        val actual = localizedContext.resources.configuration.locales[0].language

        assertEquals(expected, actual)
    }

    @Test
    fun formatDateTimeReturnsFormattedDateTimeForSlovenianLocale() {
        withLocale(Locale.forLanguageTag("sl-SI")) {
            val result = LocalDateTime.of(2026, 8, 12, 18, 36).formatLocalized()

            assertTrue(result.isNotEmpty())
            assertTrue(result.contains(other = "12"))
            assertTrue(result.contains(other = "8"))
            assertTrue(result.contains(other = "18"))
            assertTrue(result.contains(other = "36"))
        }
    }

    @Test
    fun formatDateTimeReturnsFormattedDateTimeForEnglishLocale() {
        withLocale(Locale.US) {
            val result = LocalDateTime.of(2026, 8, 12, 18, 36).formatLocalized()
            assertEquals("8_12_26_6_36_PM", result)
        }
    }

    @Test
    fun formatDateTimeDoesNotContainInvalidFileNameCharacters() {
        withLocale(Locale.US) {
            val result = LocalDateTime.of(2026, 8, 12, 18, 36).formatLocalized()

            assertTrue(result.isNotEmpty())
            assertFalse(result.any { it in "/:\\*?\"<>|" })
        }
    }

    @Test
    fun getDayOfWeekNameReturnsNonEmptyString() {
        DayOfWeek.entries.forEach {
            assertTrue(LocalizationHelper.getDayOfWeekName(dayOfWeek = it).isNotEmpty())
        }
    }

    @Test
    fun getMonthNameReturnsNonEmptyString() {
        Month.entries.forEach {
            assertTrue(LocalizationHelper.getMonthName(month = it).isNotEmpty())
        }
    }

    @Test
    fun formatLoggedDateReturnsFormattedDateWhenDayIsProvided() {
        val day = "2026-08-12"

        val formattedDate = LocalDate.parse(day).formatLocalized()
        val expected = context.resources.getString(R.string.statistics_logged, formattedDate)
        val actual = LocalizationHelper.formatLoggedDate(resources = context.resources, day = day)

        assertEquals(expected, actual)
    }

    @Test
    fun formatLoggedDateReturnsEmptyStringWhenDayIsNull() {
        val actual = LocalizationHelper.formatLoggedDate(resources = context.resources, day = null)
        assertEquals("", actual)
    }

    @Test
    fun formatMoneyFormatsValueWithConfiguredCurrency() = runBlocking {
        settingsRepository.insert(settings = SettingsEntity.default(language = languageSystem).copy(currency = "$"))

        val expected = "${DecimalFormat("0.00#").format(12.5)} $"
        val actual = LocalizationHelper.formatMoney(settingsRepository = settingsRepository, value = 12.5)

        assertEquals(expected, actual)
    }

    @Test
    fun formatMoneyFormatsValueWithThreeDecimalPlaces() = runBlocking {
        settingsRepository.insert(settings = SettingsEntity.default(language = languageSystem).copy(currency = "€"))

        val expected = "${DecimalFormat("0.00#").format(12.30)} €"
        val actual = LocalizationHelper.formatMoney(settingsRepository = settingsRepository, value = 12.30)

        assertEquals(expected, actual)
    }

    @Test
    fun formatMoneyReturnsEuroWhenSettingsAreMissing() = runBlocking {
        val actual = LocalizationHelper.formatMoney(settingsRepository = settingsRepository, value = 10.0)
        val expected = "${DecimalFormat("0.00#").format(10.0)} €"

        assertEquals(expected, actual)
    }

    private fun withLocale(locale: Locale, block: () -> Unit) {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(locale)
            block()
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}