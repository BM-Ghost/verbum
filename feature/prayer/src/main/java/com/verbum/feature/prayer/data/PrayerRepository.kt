package com.verbum.feature.prayer.data

import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.feature.prayer.domain.model.Prayer
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    fun getAllPrayers(): Flow<List<Prayer>>
    fun getPrayersForSeason(season: LiturgicalSeason): Flow<List<Prayer>>
    suspend fun getPrayerById(id: String): Prayer?
}
