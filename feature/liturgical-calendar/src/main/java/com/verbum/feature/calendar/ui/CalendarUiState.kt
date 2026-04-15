package com.verbum.feature.calendar.ui

import com.verbum.feature.calendar.domain.model.LiturgicalDay
import java.time.YearMonth

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Success(
        val currentMonth: YearMonth,
        val days: List<LiturgicalDay>,
        val selectedDay: LiturgicalDay? = null,
        val today: LiturgicalDay,
    ) : CalendarUiState
}
