package com.verbum.feature.bible.ui

import androidx.lifecycle.ViewModel
import com.verbum.feature.bible.data.seed.BibleDiagnosticsState
import com.verbum.feature.bible.data.seed.BibleDiagnosticsTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class BibleDiagnosticsViewModel @Inject constructor(
    tracker: BibleDiagnosticsTracker,
) : ViewModel() {
    val uiState: StateFlow<BibleDiagnosticsState> = tracker.state
}
