package com.verbum.feature.missal.domain

import com.verbum.feature.missal.data.MissalRepository
import com.verbum.feature.missal.domain.model.DailyReadings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyReadingsUseCase @Inject constructor(
    private val repository: MissalRepository,
) {
    operator fun invoke(date: String): Flow<DailyReadings> =
        repository.getDailyReadings(date)
}
