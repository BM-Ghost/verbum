package com.verbum.feature.calendar.domain

import com.verbum.feature.calendar.domain.model.LiturgicalDay
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetMonthCalendarUseCase @Inject constructor(
    private val calendarEngine: LiturgicalCalendarEngine,
) {
    operator fun invoke(yearMonth: YearMonth): List<LiturgicalDay> =
        (1..yearMonth.lengthOfMonth()).map { day ->
            calendarEngine.getLiturgicalDay(LocalDate.of(yearMonth.year, yearMonth.month, day))
        }
}
