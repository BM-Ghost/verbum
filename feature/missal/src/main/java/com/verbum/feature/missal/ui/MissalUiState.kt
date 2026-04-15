package com.verbum.feature.missal.ui

import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.feature.missal.domain.model.DailyReadings

sealed interface MissalUiState {
    data object Loading : MissalUiState
    data class Loaded(
        val dailyReadings: DailyReadings,
        val selectedDate: String,
    ) : MissalUiState
    data class Error(val message: String) : MissalUiState
}
