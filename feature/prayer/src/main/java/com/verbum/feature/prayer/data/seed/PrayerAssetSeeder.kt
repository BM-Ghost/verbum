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

    private val seedMutex = Mutex()

    suspend fun ensureSeeded() = withContext(ioDispatcher) {
        seedMutex.withLock {
            if (bootstrapPreferences.isPrayersPreloaded() && prayerDao.countPrayers() > 0) {
                return@withLock
            }
            if (prayerDao.countPrayers() > 0) {
                bootstrapPreferences.markPrayersPreloaded()
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

            prayerDao.insertPrayers(entities)
            bootstrapPreferences.markPrayersPreloaded()
        }
    }
}
