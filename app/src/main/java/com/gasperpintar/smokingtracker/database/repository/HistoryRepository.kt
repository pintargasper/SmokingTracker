package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.database.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    private val _changes = MutableStateFlow(0L)
    val changes: StateFlow<Long> = _changes.asStateFlow()

    suspend fun insert(
        entry: HistoryEntity
    ) {
        historyDao.insert(entity = entry)
        notifyChanged()
    }

    suspend fun insertAll(
        entries: List<HistoryEntity>
    ) {
        historyDao.insertAll(entities = entries)
        notifyChanged()
    }

    suspend fun update(
        entry: HistoryEntity
    ) {
        historyDao.update(entity = entry)
        notifyChanged()
    }

    suspend fun delete(
        entry: HistoryEntity
    ) {
        historyDao.delete(entity = entry)
        notifyChanged()
    }

    suspend fun deleteAll() {
        historyDao.deleteAll()
        historyDao.resetAutoIncrement()
        notifyChanged()
    }

    private fun notifyChanged() {
        _changes.value += 1
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

    suspend fun getSmokedCountBetween(start: LocalDateTime, end: LocalDateTime): Int {
        return historyDao.getSmokedCountBetween(start = start, end = end)
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
        return getBetween(start = date.minusMonths(12), end = date).map(transform = HistoryEntry::fromEntity)
    }
}
