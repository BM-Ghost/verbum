package com.verbum.feature.bible.data.seed

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class BibleDiagnosticsTracker @Inject constructor() {

    private val _state = MutableStateFlow(BibleDiagnosticsState())
    val state: StateFlow<BibleDiagnosticsState> = _state.asStateFlow()

    fun markSeedingStarted() {
        _state.value = _state.value.copy(
            seedStatus = SeedStatus.RUNNING,
            lastSeedError = null,
        )
    }

    fun markSeedingSucceeded(snapshot: BibleDiagnosticsSnapshot) {
        _state.value = _state.value.copy(
            seedStatus = SeedStatus.SUCCESS,
            snapshot = snapshot,
            lastSeedError = null,
        )
    }

    fun markSeedingFailed(error: Throwable) {
        _state.value = _state.value.copy(
            seedStatus = SeedStatus.FAILED,
            lastSeedError = error.message ?: error::class.java.simpleName,
        )
    }
}

enum class SeedStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILED,
}

data class BibleDiagnosticsState(
    val seedStatus: SeedStatus = SeedStatus.IDLE,
    val snapshot: BibleDiagnosticsSnapshot? = null,
    val lastSeedError: String? = null,
)

data class BibleDiagnosticsSnapshot(
    val generatedAtMs: Long,
    val totalBooks: Int,
    val languageVerseCounts: Map<String, Int>,
    val missingBooksByLanguage: Map<String, List<String>>,
    val partialBooksByLanguage: Map<String, List<String>>,
    val missingChapterCountByLanguage: Map<String, Int>,
)
