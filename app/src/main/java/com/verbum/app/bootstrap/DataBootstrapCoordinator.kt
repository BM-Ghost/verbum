package com.verbum.app.bootstrap

import com.verbum.core.common.dispatcher.Dispatcher
import com.verbum.core.common.dispatcher.VerbumDispatcher
import com.verbum.feature.bible.data.seed.BibleAssetSeeder
import com.verbum.feature.missal.data.MissalRepository
import com.verbum.feature.prayer.data.seed.PrayerAssetSeeder
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class DataBootstrapCoordinator @Inject constructor(
    private val bibleAssetSeeder: BibleAssetSeeder,
    private val prayerAssetSeeder: PrayerAssetSeeder,
    private val missalRepository: MissalRepository,
    @Dispatcher(VerbumDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun bootstrap() {
        scope.launch {
            runCatching { bibleAssetSeeder.ensureSeeded() }
                .onFailure { Timber.e(it, "Bible preload failed") }
        }

        scope.launch {
            runCatching { prayerAssetSeeder.ensureSeeded() }
                .onFailure { Timber.e(it, "Prayer preload failed") }
        }

        scope.launch {
            val today = LocalDate.now().toString()
            runCatching { missalRepository.getReading(today) }
                .onFailure { Timber.e(it, "Missal warm cache failed for %s", today) }
        }
    }
}
