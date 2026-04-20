package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.PrayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Query("SELECT * FROM prayers ORDER BY orderIndex ASC")
    fun getAllPrayers(): Flow<List<PrayerEntity>>

    @Query("SELECT COUNT(*) FROM prayers")
    suspend fun countPrayers(): Int

    @Query("SELECT * FROM prayers WHERE category = :category ORDER BY orderIndex ASC")
    fun getPrayersByCategory(category: String): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayers WHERE id = :id")
    suspend fun getPrayerById(id: String): PrayerEntity?

    @Query(
        """
        SELECT * FROM prayers 
        WHERE seasonRecommendation IS NULL 
           OR seasonRecommendation = :season
        ORDER BY orderIndex ASC
        """
    )
    fun getPrayersForSeason(season: String): Flow<List<PrayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(prayers: List<PrayerEntity>)
}
