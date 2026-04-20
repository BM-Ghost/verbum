package com.verbum.feature.prayer.domain.contract

import com.verbum.feature.prayer.domain.model.Prayer
import kotlinx.coroutines.flow.Flow

interface PrayerRepositoryContract {
    fun getAll(): Flow<List<Prayer>>
}
