package com.verbum.feature.missal.domain.contract.model

data class MissalReading(
    val date: String,
    val season: String,
    val firstReading: String,
    val psalm: String,
    val gospel: String,
)
