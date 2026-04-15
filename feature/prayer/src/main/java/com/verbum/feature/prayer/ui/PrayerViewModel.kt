package com.verbum.feature.prayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.prayer.domain.GetPrayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val getPrayers: GetPrayersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        loadPrayers()
    }

    private fun loadPrayers() {
        getPrayers.allPrayers()
            .asResult()
            .onEach { result ->
                _uiState.value = when (result) {
                    is VerbumResult.Loading -> PrayerUiState.Loading
                    is VerbumResult.Success -> PrayerUiState.Loaded(
                        prayersByCategory = result.data.groupBy { it.category }
                    )
                    is VerbumResult.Error -> PrayerUiState.Error("Failed to load prayers")
                }
            }
            .launchIn(viewModelScope)
    }
}
