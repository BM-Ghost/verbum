package com.verbum.feature.prayer.domain

import com.verbum.feature.prayer.data.PrayerRepository
import com.verbum.feature.prayer.domain.model.Prayer
import javax.inject.Inject

class GetPrayerByIdUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke(prayerId: String): Prayer? = repository.getPrayerById(prayerId)
}
