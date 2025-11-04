package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import java.time.LocalDateTime

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(history: HistoryEntity)

    @Update
    suspend fun update(history: HistoryEntity)

    @Delete
    suspend fun delete(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name = 'history'")
    suspend fun resetAutoIncrement()

    @Query(value = "SELECT * FROM history WHERE createdAt <= :endOfToday AND lent = 0 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastHistoryEntry(endOfToday: LocalDateTime): HistoryEntity?

    @Query(value = "SELECT * FROM history WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun getHistoryBetween(start: LocalDateTime, end: LocalDateTime): List<HistoryEntity>

    @Query(value = "SELECT COUNT(*) FROM history WHERE createdAt BETWEEN :start AND :end")
    suspend fun getHistoryCountBetween(start: LocalDateTime, end: LocalDateTime): Int

    @Query(value = "SELECT * FROM history")
    suspend fun getHistory(): List<HistoryEntity>
}
