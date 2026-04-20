package com.verbum.feature.missal.domain

import com.verbum.feature.missal.data.MissalRepository
import com.verbum.feature.missal.domain.model.DailyReadings
import javax.inject.Inject

class GetMissalReading @Inject constructor(
    private val repository: MissalRepository,
) {
    suspend operator fun invoke(date: String): DailyReadings = repository.getReading(date)
}
