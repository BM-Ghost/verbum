package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missal_readings")
data class MissalReadingEntity(
    @PrimaryKey val id: String, // "2026-04-15_first_reading"
    val date: String, // "2026-04-15"
    val readingType: String, // "first_reading", "psalm", "gospel", "second_reading"
    val title: String,
    val reference: String, // e.g. "Acts 4:23-31"
    val text: String,
    val liturgicalSeason: String,
    val feastOrMemorial: String? = null,
)
