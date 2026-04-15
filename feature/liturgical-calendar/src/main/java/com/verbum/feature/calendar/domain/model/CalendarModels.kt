package com.verbum.feature.calendar.domain.model

import com.verbum.core.common.model.LiturgicalSeason
import java.time.LocalDate

data class LiturgicalDay(
    val date: LocalDate,
    val season: LiturgicalSeason,
    val celebration: String,
    val rank: CelebrationRank,
    val liturgicalColor: LiturgicalColor,
    val saintOfDay: String? = null,
    val optionalMemorials: List<String> = emptyList(),
)

enum class CelebrationRank {
    SOLEMNITY,
    FEAST,
    MEMORIAL,
    OPTIONAL_MEMORIAL,
    WEEKDAY,
    SUNDAY,
}

enum class LiturgicalColor(val displayName: String) {
    GREEN("Green"),
    VIOLET("Violet"),
    WHITE("White"),
    RED("Red"),
    ROSE("Rose"),
    BLACK("Black"),
    GOLD("Gold"),
}

data class LiturgicalWeek(
    val weekNumber: Int,
    val season: LiturgicalSeason,
    val days: List<LiturgicalDay>,
)
