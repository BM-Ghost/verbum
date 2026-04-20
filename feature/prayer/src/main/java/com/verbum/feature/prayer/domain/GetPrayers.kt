package com.verbum.feature.prayer.domain

import com.verbum.feature.prayer.data.PrayerRepository
import com.verbum.feature.prayer.domain.model.Prayer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetPrayers @Inject constructor(
    private val repository: PrayerRepository,
) {
    operator fun invoke(): Flow<List<Prayer>> = repository.getAllPrayers()
}
