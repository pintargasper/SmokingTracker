package com.gasperpintar.smokingtracker.repository

import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.model.HistoryEntry
import java.time.LocalDateTime

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    suspend fun insert(
        entry: HistoryEntity
    ) {
        historyDao.insert(entity = entry)
    }

    suspend fun update(
        entry: HistoryEntity
    ) {
        historyDao.update(entity = entry)
    }

    suspend fun delete(
        entry: HistoryEntity
    ) {
        historyDao.delete(entity = entry)
    }

    suspend fun deleteAll() {
        historyDao.deleteAll()
        historyDao.resetAutoIncrement()
    }

    suspend fun getLast(): HistoryEntity? {
        return historyDao.getLast()
    }

    suspend fun getAll(): List<HistoryEntity> {
        return historyDao.getAll()
    }

    suspend fun getBetween(
        start: LocalDateTime, end: LocalDateTime
    ): List<HistoryEntity> {
        return historyDao.getBetween(start = start, end = end)
    }

    suspend fun getCountBetween(
        start: LocalDateTime, end: LocalDateTime
    ): Int {
        return historyDao.getCountBetween(start = start, end = end)
    }

    suspend fun getAverageCigarettesPerDay(): Double {
        return historyDao.getAveragePerDay()
    }

    suspend fun getTotalCigarettes(): Int {
        return historyDao.getTotalCount()
    }

    suspend fun getFirstRecordDate(): LocalDateTime? {
        return historyDao.getFirstRecordDate()
    }

    suspend fun getMaxCigarettesPerDay(): CigarettesPerDay? {
        return historyDao.getMaxCigarettesPerDay()
    }

    suspend fun getMinCigarettesPerDay(): CigarettesPerDay? {
        return historyDao.getMinCigarettesPerDay()
    }

    suspend fun getEntries(date: LocalDateTime): List<HistoryEntry> {
        return getBetween(start = date.minusMonths(12), end = date).map(transform = HistoryEntry.Companion::fromEntity)
    }
}
