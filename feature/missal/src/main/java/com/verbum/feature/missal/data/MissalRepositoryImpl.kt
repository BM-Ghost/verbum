package com.verbum.feature.missal.data

import android.util.LruCache
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.database.entity.MissalReadingEntity
import com.verbum.core.database.dao.MissalDao
import com.verbum.core.network.dto.MissalReadingsDto
import com.verbum.core.network.api.VerbumApi
import com.verbum.feature.missal.data.local.FixedFeast
import com.verbum.feature.missal.data.local.MissalAssetDataSource
import com.verbum.feature.missal.domain.LiturgicalCalendarEngine
import com.verbum.feature.missal.domain.model.DailyReadings
import com.verbum.feature.missal.domain.model.MissalReading
import com.verbum.feature.missal.domain.model.ReadingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MissalRepositoryImpl @Inject constructor(
    private val missalDao: MissalDao,
    private val missalAssetDataSource: MissalAssetDataSource,
    private val liturgicalCalendarEngine: LiturgicalCalendarEngine,
    private val verbumApi: VerbumApi,
) : MissalRepository {

    private val readingCache = LruCache<String, DailyReadings>(128)
    private val fixedFeasts: List<FixedFeast> by lazy { missalAssetDataSource.loadFixedFeasts() }

    override suspend fun getReading(date: String): DailyReadings {
        readingCache.get(date)?.let { return it }

        val cached = missalDao.getReadingsByDateImmediate(date)
        if (cached.isNotEmpty()) {
            return cached.toDomain(date).also { readingCache.put(date, it) }
        }

        val generated = generateAndPersist(date)
        readingCache.put(date, generated)
        return generated
    }

    override fun getDailyReadings(date: String): Flow<DailyReadings> {
        return flow {
            emit(getReading(date))
            emitAll(
                missalDao.getReadingsByDate(date).map { entities ->
                    entities.toDomain(date).also { mapped ->
                        readingCache.put(date, mapped)
                    }
                }
            )
        }
    }

    private suspend fun generateAndPersist(date: String): DailyReadings {
        val localDate = LocalDate.parse(date)
        val calendarDay = liturgicalCalendarEngine.compute(localDate)
        val feast = fixedFeasts.firstOrNull { it.date == "%02d-%02d".format(localDate.monthValue, localDate.dayOfMonth) }

        val fromAsset = missalAssetDataSource.loadReadings(localDate)
        if (fromAsset != null) {
            val season = fromAsset.season.toSeasonOr(calendarDay.season)
            val feastOrMemorial = feast?.name ?: fromAsset.celebrationName
            val entities = fromAsset.readings.map { reading ->
                MissalReadingEntity(
                    id = "${date}_${reading.type}",
                    date = date,
                    readingType = reading.type,
                    title = reading.title,
                    reference = reading.reference,
                    text = reading.reference,
                    liturgicalSeason = season.name,
                    feastOrMemorial = feastOrMemorial,
                )
            }
            missalDao.insertReadings(entities)
            return entities.toDomain(date)
        }

        val fromApi = runCatching { verbumApi.getMissalReadings(date) }.getOrNull()
        if (fromApi != null) {
            val entities = fromApi.toEntities(date)
            missalDao.insertReadings(entities)
            return entities.toDomain(date)
        }

        throw IllegalStateException("No missal readings source available for $date")
    }

    private fun List<MissalReadingEntity>.toDomain(date: String): DailyReadings {
        val sorted = sortedBy { entity -> readingOrder(entity.readingType) }
        return DailyReadings(
            date = date,
            season = sorted.firstOrNull()?.let { LiturgicalSeason.fromString(it.liturgicalSeason) }
                ?: LiturgicalSeason.ORDINARY_TIME,
            feastOrMemorial = sorted.firstOrNull()?.feastOrMemorial,
            readings = sorted.map { entity ->
                MissalReading(
                    id = entity.id,
                    type = entity.readingType.toReadingType(),
                    title = entity.title,
                    reference = entity.reference,
                    text = entity.text,
                )
            },
        )
    }

    private fun MissalReadingsDto.toEntities(date: String): List<MissalReadingEntity> {
        val season = liturgicalSeason.toSeasonOr(LiturgicalSeason.ORDINARY_TIME)
        return readings.map { dto ->
            MissalReadingEntity(
                id = dto.id,
                date = date,
                readingType = dto.readingType,
                title = dto.title,
                reference = dto.reference,
                text = dto.text,
                liturgicalSeason = season.name,
                feastOrMemorial = feastOrMemorial,
            )
        }
    }

    private fun String.toReadingType(): ReadingType {
        return when (this) {
            "first_reading" -> ReadingType.FIRST_READING
            "psalm" -> ReadingType.PSALM
            "second_reading" -> ReadingType.SECOND_READING
            "gospel" -> ReadingType.GOSPEL
            else -> ReadingType.FIRST_READING
        }
    }

    private fun readingOrder(type: String): Int {
        return when (type) {
            "first_reading" -> 1
            "psalm" -> 2
            "second_reading" -> 3
            "gospel" -> 4
            else -> Int.MAX_VALUE
        }
    }

    private fun String.toSeasonOr(fallback: LiturgicalSeason): LiturgicalSeason {
        val normalized = trim().uppercase()
        return when {
            normalized.contains("ADVENT") -> LiturgicalSeason.ADVENT
            normalized.contains("CHRISTMAS") -> LiturgicalSeason.CHRISTMAS
            normalized.contains("LENT") -> LiturgicalSeason.LENT
            normalized.contains("EASTER") -> LiturgicalSeason.EASTER
            normalized.contains("PENTECOST") -> LiturgicalSeason.PENTECOST
            normalized.contains("ORDINARY") -> LiturgicalSeason.ORDINARY_TIME
            else -> fallback
        }
    }
}
