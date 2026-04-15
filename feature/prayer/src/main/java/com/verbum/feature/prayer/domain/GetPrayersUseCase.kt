package com.verbum.feature.prayer.domain

import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.feature.prayer.data.PrayerRepository
import com.verbum.feature.prayer.domain.model.Prayer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPrayersUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    fun allPrayers(): Flow<List<Prayer>> = repository.getAllPrayers()

    fun forSeason(season: LiturgicalSeason): Flow<List<Prayer>> =
        repository.getPrayersForSeason(season)
}
