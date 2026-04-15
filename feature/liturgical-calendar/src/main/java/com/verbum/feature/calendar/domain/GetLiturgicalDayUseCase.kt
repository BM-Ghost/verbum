package com.verbum.feature.calendar.domain

import com.verbum.feature.calendar.domain.model.LiturgicalDay
import java.time.LocalDate
import javax.inject.Inject

class GetLiturgicalDayUseCase @Inject constructor(
    private val calendarEngine: LiturgicalCalendarEngine,
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): LiturgicalDay =
        calendarEngine.getLiturgicalDay(date)
}
