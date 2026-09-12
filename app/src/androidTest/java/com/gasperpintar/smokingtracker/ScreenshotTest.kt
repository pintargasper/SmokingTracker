package com.gasperpintar.smokingtracker

import android.content.Context
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.manager.Manager
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.io.File

@RunWith(value = AndroidJUnit4::class)
class ScreenshotTest {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationsSettingsRepository: NotificationsSettingsRepository
    private lateinit var costsRepository: CostsRepository
    private lateinit var notesRepository: NotesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Provider.getDatabase(context = context.applicationContext)
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        historyRepository = HistoryRepository(historyDao = database.historyDao())
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        notificationsSettingsRepository = NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())
        costsRepository = CostsRepository(costDao = database.costsDao())
        notesRepository = NotesRepository(notesDao = database.notesDao())

        grantNotificationPermission()
        importDummyData()
        setTestLanguage()
    }

    @Test fun captureAllScreenshots() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()

            captureScreenshot(name = "1")

            listOf("2", "3", "4").forEach {
                onView(withId(R.id.main_view_pager)).perform(swipeLeft())
                captureScreenshot(it)
            }
        }

        ActivityScenario.launch(StatisticsActivity::class.java).use {
            captureScreenshot(name = "5")
            onView(withId(R.id.statistics_view_pager)).perform(swipeLeft())
            captureScreenshot(name = "6")
        }

        ActivityScenario.launch(AchievementsActivity::class.java).use {
            captureScreenshot(name = "7")
            onView(withId(R.id.achievements_view_pager)).perform(swipeLeft())
            captureScreenshot(name = "8")
        }

        ActivityScenario.launch(CalculatorActivity::class.java).use {
            captureScreenshot(name = "9")
        }

        ActivityScenario.launch(NotesActivity::class.java).use {
            captureScreenshot(name = "10")

            runCatching {
                onView(withId(R.id.recyclerview_notes))
                    .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
                captureScreenshot(name = "11")
            }
        }

        ActivityScenario.launch(AboutActivity::class.java).use {
            captureScreenshot(name = "12")
        }
    }

    private fun importDummyData() = runBlocking {
        val file = File("/data/user/0/com.gasperpintar.smokingtracker/files/dummy_data.xlsx")

        check(value = file.exists()) { "Dummy data file not found: ${file.absolutePath}" }

        Manager.uploadFile(
            context = context,
            fileUri = Uri.fromFile(file),
            achievementRepository = achievementRepository,
            historyRepository = historyRepository,
            settingsRepository = settingsRepository,
            notificationsSettingsRepository = notificationsSettingsRepository,
            costsRepository = costsRepository,
            notesRepository = notesRepository,
            onProgress = {}
        )
    }

    private fun grantNotificationPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        instrumentation.uiAutomation
            .executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
            .close()
    }

    private fun setTestLanguage() = runBlocking {
        val language = getDefaultLanguageIndex()
        settingsRepository.upsert(settingsRepository.get()?.copy(language = language) ?: SettingsEntity.default(language))
    }

    private fun getDefaultLanguageIndex(): Int {
        val languageTag = InstrumentationRegistry.getArguments()
            .getString("testLocale")
            ?.replace(oldChar = '_', newChar = '-')
            ?: return 0

        val languageValues = ApplicationProvider.getApplicationContext<Context>().resources
            .getStringArray(R.array.language_values)

        return languageValues.indexOf(languageTag).takeIf { it >= 0 } ?: 0
    }

    private fun captureScreenshot(name: String, delayMs: Long = 1500) {
        Thread.sleep(delayMs)
        Screengrab.screenshot(name)
    }
}