package com.verbum.app.bootstrap

import com.verbum.core.common.dispatcher.Dispatcher
import com.verbum.core.common.dispatcher.VerbumDispatcher
import com.verbum.feature.bible.data.seed.BibleAssetSeeder
import com.verbum.feature.missal.data.MissalRepository
import com.verbum.feature.prayer.data.seed.PrayerAssetSeeder
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
    private val hasStarted = AtomicBoolean(false)
    private val _criticalDataReady = MutableStateFlow(false)
    val criticalDataReady: StateFlow<Boolean> = _criticalDataReady.asStateFlow()

    fun bootstrap() {
        if (!hasStarted.compareAndSet(false, true)) return

        // Seed critical data before allowing UI to load
        scope.launch {
            var bibleReady = false
            var prayerReady = false

            for (attempt in 0 until MAX_CRITICAL_SEED_ATTEMPTS) {
                if (!bibleReady) {
                    try {
                        bibleAssetSeeder.ensureSeeded()
                        bibleReady = true
                    } catch (e: Exception) {
                        Timber.e(e, "Bible preload failed (attempt %d)", attempt + 1)
                    }
                }

                if (!prayerReady) {
                    try {
                        prayerAssetSeeder.ensureSeeded()
                        prayerReady = true
                    } catch (e: Exception) {
                        Timber.e(e, "Prayer preload failed (attempt %d)", attempt + 1)
                    }
                }

                if (bibleReady && prayerReady) {
                    Timber.i("Critical data seeding complete")
                    _criticalDataReady.value = true
                    break
                }

                if (attempt < MAX_CRITICAL_SEED_ATTEMPTS - 1) {
                    Timber.w("Critical preload incomplete (attempt %d): bible=%s, prayer=%s", attempt + 1, bibleReady, prayerReady)
                    delay(CRITICAL_SEED_RETRY_DELAY_MS)
                }
            }

            if (!bibleReady || !prayerReady) {
                Timber.e("Critical preload failed after %d attempts (bible=%s, prayer=%s)", MAX_CRITICAL_SEED_ATTEMPTS, bibleReady, prayerReady)
            }

            _criticalDataReady.value = true
        }

        // Warm cache for Missal in background
        scope.launch {
            val today = LocalDate.now().toString()
            runCatching { missalRepository.getReading(today) }
                .onFailure { Timber.e(it, "Missal warm cache failed for %s", today) }
        }
    }

    private companion object {
        const val MAX_CRITICAL_SEED_ATTEMPTS = 3
        const val CRITICAL_SEED_RETRY_DELAY_MS = 300L
    }
}
