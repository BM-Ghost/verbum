package com.verbum.feature.prayer.data

import com.verbum.feature.prayer.domain.contract.PrayerRepositoryContract
import com.verbum.feature.prayer.domain.model.Prayer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PrayerRepositoryContractImpl @Inject constructor(
    private val repository: PrayerRepository,
) : PrayerRepositoryContract {
    override fun getAll(): Flow<List<Prayer>> = repository.getAllPrayers()
}
