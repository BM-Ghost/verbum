package com.verbum.feature.prayer.data.seed

import android.content.Context
import com.verbum.core.common.dispatcher.Dispatcher
import com.verbum.core.common.dispatcher.VerbumDispatcher
import com.verbum.core.common.preferences.BootstrapPreferences
import com.verbum.core.database.dao.PrayerDao
import com.verbum.core.database.entity.PrayerEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Singleton
class PrayerAssetSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prayerDao: PrayerDao,
    private val bootstrapPreferences: BootstrapPreferences,
    @Dispatcher(VerbumDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private companion object {
        // Increment this when bundled prayer content changes.
        const val CURRENT_PRAYERS_ASSET_VERSION = 2
    }

    private val seedMutex = Mutex()

    suspend fun ensureSeeded() = withContext(ioDispatcher) {
        seedMutex.withLock {
            val prayerCount = prayerDao.countPrayers()
            val storedAssetVersion = bootstrapPreferences.getPrayersAssetVersion()
            if (storedAssetVersion >= CURRENT_PRAYERS_ASSET_VERSION && prayerCount > 0) {
                return@withLock
            }

            val rawJson = context.assets.open("prayers/prayers.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(rawJson)
            val entities = ArrayList<PrayerEntity>(jsonArray.length())

            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                entities.add(
                    PrayerEntity(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        category = item.getString("category"),
                        text = item.getString("text"),
                        latinText = item.optString("latinText").takeIf { it.isNotBlank() },
                        seasonRecommendation = item.optString("seasonRecommendation").takeIf { it.isNotBlank() },
                        orderIndex = item.optInt("orderIndex", index),
                    )
                )
            }

            prayerDao.deletePrayersNotIn(entities.map { it.id })
            prayerDao.insertPrayers(entities)
            bootstrapPreferences.markPrayersPreloaded()
            bootstrapPreferences.setPrayersAssetVersion(CURRENT_PRAYERS_ASSET_VERSION)
        }
    }
}
