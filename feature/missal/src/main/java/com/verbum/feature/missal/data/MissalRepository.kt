package com.verbum.feature.missal.data

import com.verbum.feature.missal.domain.model.DailyReadings
import kotlinx.coroutines.flow.Flow

interface MissalRepository {
    suspend fun getReading(date: String): DailyReadings
    fun getDailyReadings(date: String): Flow<DailyReadings>
}
