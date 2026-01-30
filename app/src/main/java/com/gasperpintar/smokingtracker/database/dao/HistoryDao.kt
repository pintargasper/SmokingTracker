package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import java.time.LocalDateTime

@Dao
interface HistoryDao: Base<HistoryEntity> {

    @Query(value = "DELETE FROM history")
    suspend fun deleteAll()

    @Query(value = "DELETE FROM sqlite_sequence WHERE name = 'history'")
    suspend fun resetAutoIncrement()

    @Query(value = "SELECT * FROM history WHERE lent = 0 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLast(): HistoryEntity?

    @Query(value = "SELECT * FROM history WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun getBetween(start: LocalDateTime, end: LocalDateTime): List<HistoryEntity>

    @Query(value = "SELECT COUNT(*) FROM history WHERE createdAt BETWEEN :start AND :end")
    suspend fun getCountBetween(start: LocalDateTime, end: LocalDateTime): Int

    @Query(
        value = """
            SELECT AVG(daily_sum)
            FROM (
                SELECT DATE(createdAt) as day, SUM(CASE WHEN lent = 0 THEN 1 ELSE 0 END) as daily_sum
                FROM history
                GROUP BY DATE(createdAt)
                HAVING daily_sum > 0
            )
        """
    )
    suspend fun getAveragePerDay(): Double

    @Query(value = "SELECT * FROM history")
    suspend fun getAll(): List<HistoryEntity>
}
