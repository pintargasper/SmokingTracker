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
        val expected = context.resources.configuration.locales[0].language
        val actual = localizedContext.resources.configuration.locales[0].language

        assertEquals(expected, actual)
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
        val expected = context.resources.configuration.locales[0].language
        val actual = localizedContext.resources.configuration.locales[0].language

        assertEquals(expected, actual)
    }

    @Test
    fun formatDateTimeReturnsFormattedDateTimeForSlovenianLocale() {
        val originalLocale = Locale.getDefault()

        try {
            Locale.setDefault(Locale.forLanguageTag("sl-SI"))

            val dateTime = LocalDateTime.of(2026, 8, 12, 18, 36)
            val result = LocalizationHelper.formatDateTime(dateTime)

            assert(result.isNotEmpty())
            assert(result.contains("12"))
            assert(result.contains("8"))
            assert(result.contains("18"))
            assert(result.contains("36"))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun formatDateTimeReturnsFormattedDateTimeForEnglishLocale() {
        val originalLocale = Locale.getDefault()

        try {
            Locale.setDefault(Locale.forLanguageTag("en-US"))

            val dateTime = LocalDateTime.of(2026, 8, 12, 18, 36)
            val result = LocalizationHelper.formatDateTime(dateTime)

            assertEquals("8_12_26_6_36_PM", result)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun formatDateTimeDoesNotContainInvalidFileNameCharacters() {
        val originalLocale = Locale.getDefault()

        try {
            Locale.setDefault(Locale.forLanguageTag("en-US"))

            val dateTime = LocalDateTime.of(2026, 8, 12, 18, 36)
            val result = LocalizationHelper.formatDateTime(dateTime)

            assert(result.isNotEmpty())
            assert(result.none {
                it in "/:\\*?\"<>|"
            })
        } finally {
            Locale.setDefault(originalLocale)
        }
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

    @Test
    fun formatLoggedDateReturnsFormattedDateWhenDayIsProvided() {
        val day = "2026-08-12"

        val formattedDate = LocalizationHelper.formatDate(LocalDate.parse(day))

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
        settingsRepository.insert(
            settings = createSettingsEntity(languageId = 0).copy(currency = "$")
        )

        val expected = "${DecimalFormat("0.00#").format(12.5)} $"
        val actual = LocalizationHelper.formatMoney(settingsRepository = settingsRepository, value = 12.5)

        assertEquals(expected, actual)
    }

    @Test
    fun formatMoneyFormatsValueWithThreeDecimalPlaces() = runBlocking {
        settingsRepository.insert(settings = createSettingsEntity(languageId = 0).copy(currency = "€"))

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

    private fun createSettingsEntity(languageId: Int): SettingsEntity {
        return SettingsEntity(
            id = 0,
            theme = 0,
            language = languageId,
            frequency = 0,
            currency = "€",
            customCurrency = ""
        )
    }
}