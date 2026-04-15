package com.verbum.feature.missal.domain.model

import com.verbum.core.common.model.LiturgicalSeason

data class DailyReadings(
    val date: String,
    val season: LiturgicalSeason,
    val feastOrMemorial: String?,
    val readings: List<MissalReading>,
)

data class MissalReading(
    val id: String,
    val type: ReadingType,
    val title: String,
    val reference: String,
    val text: String,
)

enum class ReadingType(val displayName: String) {
    FIRST_READING("First Reading"),
    PSALM("Responsorial Psalm"),
    SECOND_READING("Second Reading"),
    GOSPEL("Gospel"),
}
