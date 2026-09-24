package com.gasperpintar.smokingtracker.database.fake

import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.CigarettesPerDay
import java.time.LocalDateTime

class FakeHistoryDao : FakeDao<HistoryEntity>(
    idOf = { it.id },
    withId = { entity, id -> entity.copy(id = id) }
), HistoryDao {

    var averagePerDay = 0.0
    var maxPerDay: CigarettesPerDay? = null
    var minPerDay: CigarettesPerDay? = null

    @Override
    override suspend fun deleteAll() {
        items.clear()
    }

    @Override
    override suspend fun resetAutoIncrement() {
        resetId()
    }

    @Override
    override suspend fun getLast(): HistoryEntity? {
        return items.filter { it.lent == 0 }.maxByOrNull { it.createdAt }
    }

    @Override
    override suspend fun getBetween(start: LocalDateTime, end: LocalDateTime): List<HistoryEntity> {
        return items.filter { it.createdAt >= start && it.createdAt < end }.sortedByDescending { it.createdAt }
    }

    @Override
    override suspend fun getCountBetween(start: LocalDateTime, end: LocalDateTime): Int {
        return getBetween(start = start, end = end).size
    }

    @Override
    override suspend fun getAveragePerDay(): Double {
        return averagePerDay
    }

    @Override
    override suspend fun getAll(): List<HistoryEntity> {
        return items.toList()
    }

    @Override
    override suspend fun getTotalCount(): Int {
        return items.size
    }

    @Override
    override suspend fun getFirstRecordDate(): LocalDateTime? {
        return items.minOfOrNull { it.createdAt }
    }

    @Override
    override suspend fun getMaxCigarettesPerDay(): CigarettesPerDay? {
        return maxPerDay
    }

    @Override
    override suspend fun getMinCigarettesPerDay(): CigarettesPerDay? {
        return minPerDay
    }
}