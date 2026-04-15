package com.verbum.feature.missal.data

import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.database.dao.MissalDao
import com.verbum.core.network.api.VerbumApi
import com.verbum.feature.missal.domain.model.DailyReadings
import com.verbum.feature.missal.domain.model.MissalReading
import com.verbum.feature.missal.domain.model.ReadingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MissalRepositoryImpl @Inject constructor(
    private val missalDao: MissalDao,
    private val verbumApi: VerbumApi,
) : MissalRepository {

    override fun getDailyReadings(date: String): Flow<DailyReadings> {
        return missalDao.getReadingsByDate(date).map { entities ->
            DailyReadings(
                date = date,
                season = entities.firstOrNull()?.let {
                    LiturgicalSeason.fromString(it.liturgicalSeason)
                } ?: LiturgicalSeason.ORDINARY_TIME,
                feastOrMemorial = entities.firstOrNull()?.feastOrMemorial,
                readings = entities.map { entity ->
                    MissalReading(
                        id = entity.id,
                        type = when (entity.readingType) {
                            "first_reading" -> ReadingType.FIRST_READING
                            "psalm" -> ReadingType.PSALM
                            "second_reading" -> ReadingType.SECOND_READING
                            "gospel" -> ReadingType.GOSPEL
                            else -> ReadingType.FIRST_READING
                        },
                        title = entity.title,
                        reference = entity.reference,
                        text = entity.text,
                    )
                },
            )
        }
    }
}
