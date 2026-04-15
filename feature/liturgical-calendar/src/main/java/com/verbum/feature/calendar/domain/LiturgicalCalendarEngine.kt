package com.verbum.feature.calendar.domain

import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.feature.calendar.domain.model.CelebrationRank
import com.verbum.feature.calendar.domain.model.LiturgicalColor
import com.verbum.feature.calendar.domain.model.LiturgicalDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Computes the liturgical season and basic celebration data for a given date.
 * Uses a simplified algorithm based on Easter and fixed-date solemnities.
 */
@Singleton
class LiturgicalCalendarEngine @Inject constructor() {

    fun getLiturgicalDay(date: LocalDate): LiturgicalDay {
        val season = computeSeason(date)
        return LiturgicalDay(
            date = date,
            season = season,
            celebration = getCelebration(date, season),
            rank = getRank(date, season),
            liturgicalColor = getColor(date, season),
            saintOfDay = getSaint(date),
        )
    }

    fun computeSeason(date: LocalDate): LiturgicalSeason {
        val year = date.year
        val easter = computeEaster(year)

        val adventStart = computeAdventStart(year)
        val christmasStart = LocalDate.of(year, Month.DECEMBER, 25)
        val epiphany = computeEpiphany(year + 1)
        val ashWednesday = easter.minusDays(46)
        val pentecost = easter.plusDays(49)
        val pentecostEnd = easter.plusDays(56) // Pentecost week (octave-like)

        // Previous year's Christmas season might extend into January
        val prevEpiphany = computeEpiphany(year)

        return when {
            // Before Epiphany (still Christmas season from previous year)
            date.isBefore(prevEpiphany) -> LiturgicalSeason.CHRISTMAS

            // Lent: Ash Wednesday to Easter Vigil (Holy Saturday)
            !date.isBefore(ashWednesday) && date.isBefore(easter) -> LiturgicalSeason.LENT

            // Easter: Easter Sunday to the day before Pentecost
            !date.isBefore(easter) && date.isBefore(pentecost) -> LiturgicalSeason.EASTER

            // Pentecost: Pentecost Sunday through the following week
            !date.isBefore(pentecost) && !date.isAfter(pentecostEnd) -> LiturgicalSeason.PENTECOST

            // Advent: 4th Sunday before Christmas to Dec 24
            !date.isBefore(adventStart) && date.isBefore(christmasStart) -> LiturgicalSeason.ADVENT

            // Christmas: Dec 25 onward
            !date.isBefore(christmasStart) -> LiturgicalSeason.CHRISTMAS

            // Everything else is Ordinary Time
            else -> LiturgicalSeason.ORDINARY_TIME
        }
    }

    /**
     * Computus algorithm (Anonymous Gregorian algorithm) for Easter date.
     */
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
        // First Sunday of Advent = 4th Sunday before Christmas
        val christmas = LocalDate.of(year, Month.DECEMBER, 25)
        val sundayBeforeChristmas = christmas.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        return sundayBeforeChristmas.minusWeeks(3)
    }

    private fun computeEpiphany(year: Int): LocalDate {
        // In many countries, Epiphany is celebrated on the Sunday between Jan 2-8
        val jan2 = LocalDate.of(year, Month.JANUARY, 2)
        return jan2.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    private fun getCelebration(date: LocalDate, season: LiturgicalSeason): String {
        // Check for fixed solemnities/feasts
        return when {
            date.month == Month.DECEMBER && date.dayOfMonth == 25 -> "The Nativity of the Lord (Christmas)"
            date.month == Month.JANUARY && date.dayOfMonth == 1 -> "Solemnity of Mary, Mother of God"
            date.month == Month.MARCH && date.dayOfMonth == 19 -> "Solemnity of St. Joseph"
            date.month == Month.MARCH && date.dayOfMonth == 25 -> "Annunciation of the Lord"
            date.month == Month.JUNE && date.dayOfMonth == 24 -> "Nativity of St. John the Baptist"
            date.month == Month.JUNE && date.dayOfMonth == 29 -> "Solemnity of Sts. Peter and Paul"
            date.month == Month.AUGUST && date.dayOfMonth == 15 -> "Assumption of the Blessed Virgin Mary"
            date.month == Month.NOVEMBER && date.dayOfMonth == 1 -> "All Saints' Day"
            date.month == Month.NOVEMBER && date.dayOfMonth == 2 -> "All Souls' Day"
            date.month == Month.DECEMBER && date.dayOfMonth == 8 -> "Immaculate Conception"
            date.dayOfWeek == DayOfWeek.SUNDAY -> "Sunday of ${season.displayName}"
            else -> "${season.displayName} Weekday"
        }
    }

    private fun getRank(date: LocalDate, season: LiturgicalSeason): CelebrationRank {
        return when {
            date.month == Month.DECEMBER && date.dayOfMonth == 25 -> CelebrationRank.SOLEMNITY
            date.month == Month.JANUARY && date.dayOfMonth == 1 -> CelebrationRank.SOLEMNITY
            date.month == Month.AUGUST && date.dayOfMonth == 15 -> CelebrationRank.SOLEMNITY
            date.month == Month.NOVEMBER && date.dayOfMonth == 1 -> CelebrationRank.SOLEMNITY
            date.month == Month.DECEMBER && date.dayOfMonth == 8 -> CelebrationRank.SOLEMNITY
            date.dayOfWeek == DayOfWeek.SUNDAY -> CelebrationRank.SUNDAY
            else -> CelebrationRank.WEEKDAY
        }
    }

    private fun getColor(date: LocalDate, season: LiturgicalSeason): LiturgicalColor {
        return when {
            date.month == Month.DECEMBER && date.dayOfMonth == 25 -> LiturgicalColor.WHITE
            date.month == Month.NOVEMBER && date.dayOfMonth == 2 -> LiturgicalColor.VIOLET
            season == LiturgicalSeason.ADVENT -> LiturgicalColor.VIOLET
            season == LiturgicalSeason.CHRISTMAS -> LiturgicalColor.WHITE
            season == LiturgicalSeason.LENT -> LiturgicalColor.VIOLET
            season == LiturgicalSeason.EASTER -> LiturgicalColor.WHITE
            season == LiturgicalSeason.PENTECOST -> LiturgicalColor.RED
            season == LiturgicalSeason.ORDINARY_TIME -> LiturgicalColor.GREEN
            else -> LiturgicalColor.GREEN
        }
    }

    private fun getSaint(date: LocalDate): String? {
        // A selection of notable saints
        return when {
            date.month == Month.JANUARY && date.dayOfMonth == 28 -> "St. Thomas Aquinas"
            date.month == Month.FEBRUARY && date.dayOfMonth == 14 -> "Sts. Cyril and Methodius"
            date.month == Month.MARCH && date.dayOfMonth == 17 -> "St. Patrick"
            date.month == Month.APRIL && date.dayOfMonth == 29 -> "St. Catherine of Siena"
            date.month == Month.MAY && date.dayOfMonth == 13 -> "Our Lady of Fatima"
            date.month == Month.JUNE && date.dayOfMonth == 13 -> "St. Anthony of Padua"
            date.month == Month.JULY && date.dayOfMonth == 11 -> "St. Benedict"
            date.month == Month.AUGUST && date.dayOfMonth == 28 -> "St. Augustine"
            date.month == Month.SEPTEMBER && date.dayOfMonth == 23 -> "St. Padre Pio"
            date.month == Month.OCTOBER && date.dayOfMonth == 1 -> "St. Thérèse of Lisieux"
            date.month == Month.OCTOBER && date.dayOfMonth == 4 -> "St. Francis of Assisi"
            date.month == Month.OCTOBER && date.dayOfMonth == 7 -> "Our Lady of the Rosary"
            date.month == Month.NOVEMBER && date.dayOfMonth == 3 -> "St. Martin de Porres"
            date.month == Month.DECEMBER && date.dayOfMonth == 6 -> "St. Nicholas"
            date.month == Month.DECEMBER && date.dayOfMonth == 12 -> "Our Lady of Guadalupe"
            else -> null
        }
    }
}
