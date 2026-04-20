package com.verbum.feature.prayer.data

import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.database.dao.PrayerDao
import com.verbum.feature.prayer.data.seed.PrayerAssetSeeder
import com.verbum.feature.prayer.domain.model.Prayer
import com.verbum.feature.prayer.domain.model.PrayerCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrayerRepositoryImpl @Inject constructor(
    private val prayerDao: PrayerDao,
    private val prayerAssetSeeder: PrayerAssetSeeder,
) : PrayerRepository {

    override fun getAll(): Flow<List<Prayer>> = getAllPrayers()

    override fun getAllPrayers(): Flow<List<Prayer>> {
        return flow {
            prayerAssetSeeder.ensureSeeded()
            emitAll(
                prayerDao.getAllPrayers().map { entities ->
                    entities.map { it.toDomain() }
                }
            )
        }
    }

    override fun getPrayersForSeason(season: LiturgicalSeason): Flow<List<Prayer>> {
        return flow {
            prayerAssetSeeder.ensureSeeded()
            emitAll(
                prayerDao.getPrayersForSeason(season.name).map { entities ->
                    entities.map { it.toDomain() }
                }
            )
        }
    }

    override suspend fun getPrayerById(id: String): Prayer? {
        prayerAssetSeeder.ensureSeeded()
        return prayerDao.getPrayerById(id)?.toDomain()
    }

    private fun com.verbum.core.database.entity.PrayerEntity.toDomain(): Prayer {
        return Prayer(
            id = id,
            title = title,
            category = when (category) {
                "rosary" -> PrayerCategory.ROSARY
                "devotion" -> PrayerCategory.DEVOTION
                "morning" -> PrayerCategory.MORNING
                "evening" -> PrayerCategory.EVENING
                "saints" -> PrayerCategory.SAINTS
                else -> PrayerCategory.DEVOTION
            },
            text = text,
            latinText = latinText,
        )
    }
}
