package com.verbum.feature.prayer.ui

import com.verbum.feature.prayer.domain.model.Prayer
import com.verbum.feature.prayer.domain.model.PrayerCategory

sealed interface PrayerUiState {
    data object Loading : PrayerUiState
    data class Loaded(
        val prayersByCategory: Map<PrayerCategory, List<Prayer>>,
    ) : PrayerUiState
    data class Error(val message: String) : PrayerUiState
}

sealed interface PrayerDetailUiState {
    data object Loading : PrayerDetailUiState
    data class Loaded(val prayer: Prayer) : PrayerDetailUiState
    data class Error(val message: String) : PrayerDetailUiState
}
