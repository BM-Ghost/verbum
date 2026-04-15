package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayers")
data class PrayerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "rosary", "devotion", "morning", "evening", "saints"
    val text: String,
    val latinText: String? = null,
    val seasonRecommendation: String? = null, // liturgical season affinity
    val orderIndex: Int = 0,
)
