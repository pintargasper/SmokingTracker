package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    private val _changes = MutableStateFlow(value = 0L)
    val changes: StateFlow<Long> = _changes.asStateFlow()

    suspend fun insert(
        entry: HistoryEntry
    ) {
        historyDao.insert(entity = entry.toEntity())
        notifyChanged()
    }

    suspend fun insertAll(
        entries: List<HistoryEntry>
    ) {
        historyDao.insertAll(entities = entries.map(transform = HistoryEntry::toEntity))
        notifyChanged()
    }

    suspend fun update(
        entry: HistoryEntry
    ) {
        historyDao.update(entity = entry.toEntity())
        notifyChanged()
    }

    suspend fun delete(
        entry: HistoryEntry
    ) {
        historyDao.delete(entity = entry.toEntity())
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

    suspend fun getLast(): HistoryEntry? {
        return historyDao.getLast()?.let(block = HistoryEntry::fromEntity)
    }

    suspend fun getAll(): List<HistoryEntry> {
        return historyDao.getAll().map(transform = HistoryEntry::fromEntity)
    }

    suspend fun getBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<HistoryEntry> {
        return historyDao.getBetween(start = start, end = end).map(transform = HistoryEntry::fromEntity)
    }

    suspend fun getCountBetween(
        start: LocalDateTime, end: LocalDateTime
    ): Int {
        return historyDao.getCountBetween(start = start, end = end)
    }

    suspend fun getSmokedCountBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): Int {
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
        return getBetween(start = date.minusMonths(12), end = date)
    }
}