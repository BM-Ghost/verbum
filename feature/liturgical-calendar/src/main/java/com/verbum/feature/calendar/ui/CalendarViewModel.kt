package com.verbum.feature.calendar.ui

import androidx.lifecycle.ViewModel
import com.verbum.feature.calendar.domain.GetLiturgicalDayUseCase
import com.verbum.feature.calendar.domain.GetMonthCalendarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getLiturgicalDay: GetLiturgicalDayUseCase,
    private val getMonthCalendar: GetMonthCalendarUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        val days = getMonthCalendar(yearMonth)
        val today = getLiturgicalDay(LocalDate.now())
        _uiState.value = CalendarUiState.Success(
            currentMonth = yearMonth,
            days = days,
            selectedDay = today,
            today = today,
        )
    }

    fun selectDay(date: LocalDate) {
        val current = _uiState.value
        if (current is CalendarUiState.Success) {
            val day = current.days.find { it.date == date } ?: getLiturgicalDay(date)
            _uiState.value = current.copy(selectedDay = day)
        }
    }

    fun nextMonth() {
        val current = _uiState.value
        if (current is CalendarUiState.Success) {
            loadMonth(current.currentMonth.plusMonths(1))
        }
    }

    fun previousMonth() {
        val current = _uiState.value
        if (current is CalendarUiState.Success) {
            loadMonth(current.currentMonth.minusMonths(1))
        }
    }
}
