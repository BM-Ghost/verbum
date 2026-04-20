package com.verbum.feature.missal.domain

import com.verbum.core.common.model.LiturgicalSeason
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiturgicalCalendarEngine @Inject constructor() {

    fun compute(date: LocalDate): LiturgicalCalendarDay {
        val easter = computeEaster(date.year)
        val ashWednesday = easter.minusDays(46)
        val adventStart = computeAdventStart(date.year)
        val christmas = LocalDate.of(date.year, Month.DECEMBER, 25)
        val pentecost = easter.plusDays(49)

        val season = when {
            !date.isBefore(christmas) || date.isBefore(computeEpiphany(date.year)) -> LiturgicalSeason.CHRISTMAS
            !date.isBefore(ashWednesday) && date.isBefore(easter) -> LiturgicalSeason.LENT
            !date.isBefore(easter) && !date.isAfter(pentecost) -> LiturgicalSeason.EASTER
            !date.isBefore(adventStart) && date.isBefore(christmas) -> LiturgicalSeason.ADVENT
            else -> LiturgicalSeason.ORDINARY_TIME
        }

        val weekOfSeason = when (season) {
            LiturgicalSeason.LENT -> (ChronoUnit.DAYS.between(ashWednesday, date).toInt() / 7) + 1
            LiturgicalSeason.EASTER -> (ChronoUnit.DAYS.between(easter, date).toInt() / 7) + 1
            LiturgicalSeason.ADVENT -> (ChronoUnit.DAYS.between(adventStart, date).toInt() / 7) + 1
            LiturgicalSeason.CHRISTMAS -> 1
            LiturgicalSeason.PENTECOST -> 1
            LiturgicalSeason.ORDINARY_TIME -> computeOrdinaryWeek(date, easter)
        }.coerceAtLeast(1)

        return LiturgicalCalendarDay(
            season = season,
            weekOfSeason = weekOfSeason,
            sundayCycle = sundayCycle(date),
            weekdayCycle = weekdayCycle(date),
        )
    }

    fun computeEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }

    private fun computeAdventStart(year: Int): LocalDate {
        val christmas = LocalDate.of(year, Month.DECEMBER, 25)
        val sundayBeforeChristmas = christmas.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        return sundayBeforeChristmas.minusWeeks(3)
    }

    private fun computeEpiphany(year: Int): LocalDate {
        val jan2 = LocalDate.of(year, Month.JANUARY, 2)
        return jan2.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    private fun sundayCycle(date: LocalDate): String {
        val adventStart = computeAdventStart(date.year)
        val liturgicalYear = if (!date.isBefore(adventStart)) date.year + 1 else date.year
        return when (liturgicalYear % 3) {
            1 -> "A"
            2 -> "B"
            else -> "C"
        }
    }

    private fun weekdayCycle(date: LocalDate): String = if (date.year % 2 == 0) "II" else "I"

    private fun computeOrdinaryWeek(date: LocalDate, easter: LocalDate): Int {
        val firstOrdinaryStart = computeEpiphany(date.year).plusDays(1)
        val firstSegmentWeeks = (ChronoUnit.DAYS.between(firstOrdinaryStart, easter.minusDays(47)).toInt() / 7)
            .coerceAtLeast(0)

        val secondOrdinaryStart = easter.plusDays(50)
        return if (date.isBefore(easter.minusDays(46))) {
            (ChronoUnit.DAYS.between(firstOrdinaryStart, date).toInt() / 7) + 1
        } else {
            firstSegmentWeeks + (ChronoUnit.DAYS.between(secondOrdinaryStart, date).toInt() / 7) + 1
        }
    }
}

data class LiturgicalCalendarDay(
    val season: LiturgicalSeason,
    val weekOfSeason: Int,
    val sundayCycle: String,
    val weekdayCycle: String,
)
