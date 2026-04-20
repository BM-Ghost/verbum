package com.verbum.feature.missal.data

import com.verbum.feature.missal.domain.contract.MissalRepositoryContract
import com.verbum.feature.missal.domain.contract.model.MissalReading
import com.verbum.feature.missal.domain.model.ReadingType
import javax.inject.Inject

class MissalRepositoryContractImpl @Inject constructor(
    private val repository: MissalRepository,
) : MissalRepositoryContract {
    override suspend fun getReading(date: String): MissalReading {
        val daily = repository.getReading(date)
        val firstReading = daily.readings.firstOrNull { it.type == ReadingType.FIRST_READING }?.text.orEmpty()
        val psalm = daily.readings.firstOrNull { it.type == ReadingType.PSALM }?.text.orEmpty()
        val gospel = daily.readings.firstOrNull { it.type == ReadingType.GOSPEL }?.text.orEmpty()
        return MissalReading(
            date = daily.date,
            season = daily.season.name,
            firstReading = firstReading,
            psalm = psalm,
            gospel = gospel,
        )
    }
}
