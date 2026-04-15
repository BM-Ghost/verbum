package com.verbum.core.common.model

/**
 * Represents the liturgical seasons of the Catholic Church.
 * Drives theme changes, prayer suggestions, and AI tone adaptation.
 */
enum class LiturgicalSeason(val displayName: String) {
    ADVENT("Advent"),
    CHRISTMAS("Christmas"),
    LENT("Lent"),
    EASTER("Easter"),
    PENTECOST("Pentecost"),
    ORDINARY_TIME("Ordinary Time");

    companion object {
        fun fromString(value: String): LiturgicalSeason {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                    it.displayName.equals(value, ignoreCase = true)
            } ?: ORDINARY_TIME
        }
    }
}
