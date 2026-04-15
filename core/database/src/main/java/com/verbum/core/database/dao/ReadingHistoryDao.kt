package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 1")
    fun getLastRead(): Flow<ReadingHistoryEntity?>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<ReadingHistoryEntity>>

    @Query("SELECT COUNT(*) FROM reading_history")
    suspend fun getTotalReadings(): Int

    @Query(
        """
        SELECT COUNT(DISTINCT date(timestamp / 1000, 'unixepoch')) 
        FROM reading_history 
        WHERE timestamp >= :sinceTimestamp
        """
    )
    suspend fun getReadingStreak(sinceTimestamp: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history WHERE timestamp < :beforeTimestamp")
    suspend fun clearOldHistory(beforeTimestamp: Long)
}
