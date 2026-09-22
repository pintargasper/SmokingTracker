package com.gasperpintar.smokingtracker.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(value = AndroidJUnit4::class)
class HistoryDaoTest {

    private lateinit var historyDao: HistoryDao

    @Before
    fun setup() {
        historyDao = TestProvider.getInMemoryDatabase(context = ApplicationProvider.getApplicationContext()).historyDao()
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun getLastIgnoresLentEntries() = runBlocking {
        insert(LocalDateTime.of(2026, 1, 1, 10, 0))
        insert(LocalDateTime.of(2026, 1, 1, 12, 0))
        insert(LocalDateTime.of(2026, 1, 1, 13, 0), lent = true)

        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), historyDao.getLast()?.createdAt)
    }

    @Test
    fun getBetweenIncludesStartAndExcludesEnd() = runBlocking {
        val start = LocalDateTime.of(2026, 1, 1, 0, 0)
        val end = LocalDateTime.of(2026, 1, 2, 0, 0)
        insert(start.minusSeconds(1))
        insert(start)
        insert(start.plusHours(12))
        insert(end)

        assertEquals(listOf(start.plusHours(12), start), historyDao.getBetween(start = start, end = end).map { it.createdAt })
        assertEquals(2, historyDao.getCountBetween(start = start, end = end))
    }

    @Test
    fun getAveragePerDayReturnsZeroWithoutHistory() = runBlocking {
        assertEquals(0.0, historyDao.getAveragePerDay(), 0.0)
    }

    @Test
    fun getAveragePerDayIgnoresLentEntries() = runBlocking {
        repeat(times = 2) { insert(LocalDateTime.of(2026, 1, 1, 10, it)) }
        repeat(times = 4) { insert(LocalDateTime.of(2026, 1, 2, 10, it)) }
        insert(LocalDateTime.of(2026, 1, 2, 11, 0), lent = true)
        insert(LocalDateTime.of(2026, 1, 3, 10, 0), lent = true)

        assertEquals(3.0, historyDao.getAveragePerDay(), 0.0001)
    }

    @Test
    fun getMaxCigarettesPerDayPrefersLatestDayOnTie() = runBlocking {
        repeat(times = 2) { insert(LocalDateTime.of(2026, 1, 1, 10, it)) }
        repeat(times = 2) { insert(LocalDateTime.of(2026, 1, 2, 10, it)) }
        repeat(times = 3) { insert(LocalDateTime.of(2026, 1, 1, 11, it), lent = true) }

        assertEquals(CigarettesPerDay(dailySum = 2, day = "2026-01-02"), historyDao.getMaxCigarettesPerDay())
    }

    @Test
    fun getMinCigarettesPerDayExcludesToday() = runBlocking {
        val today = LocalDate.now()
        repeat(times = 3) { insert(today.minusDays(3).atTime(10, it)) }
        repeat(times = 2) { insert(today.minusDays(2).atTime(10, it)) }
        insert(LocalDateTime.now())

        assertEquals(CigarettesPerDay(dailySum = 2, day = today.minusDays(2).toString()), historyDao.getMinCigarettesPerDay())
    }

    @Test
    fun getFirstRecordDateReturnsOldestEntry() = runBlocking {
        assertNull(historyDao.getFirstRecordDate())

        insert(LocalDateTime.of(2026, 1, 2, 10, 0))
        insert(LocalDateTime.of(2026, 1, 1, 10, 0), lent = true)

        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), historyDao.getFirstRecordDate())
    }

    @Test
    fun deleteAllResetsGeneratedIds() = runBlocking {
        val repository = HistoryRepository(historyDao = historyDao)
        insert(LocalDateTime.of(2026, 1, 1, 10, 0))
        insert(LocalDateTime.of(2026, 1, 1, 11, 0))

        repository.deleteAll()
        insert(LocalDateTime.of(2026, 1, 1, 12, 0))

        assertEquals(1L, historyDao.getAll().single().id)
    }

    private suspend fun insert(createdAt: LocalDateTime, lent: Boolean = false) {
        historyDao.insert(entity = HistoryEntity(id = 0, lent = if (lent) 1 else 0, createdAt = createdAt))
    }
}
