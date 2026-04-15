package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.MissalReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissalDao {

    @Query("SELECT * FROM missal_readings WHERE date = :date ORDER BY readingType ASC")
    fun getReadingsByDate(date: String): Flow<List<MissalReadingEntity>>

    @Query("SELECT * FROM missal_readings WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getReadingsInRange(startDate: String, endDate: String): Flow<List<MissalReadingEntity>>

    @Query("SELECT DISTINCT date FROM missal_readings ORDER BY date ASC")
    suspend fun getAvailableDates(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<MissalReadingEntity>)

    @Query("DELETE FROM missal_readings WHERE date < :date")
    suspend fun deleteReadingsBefore(date: String)
}
