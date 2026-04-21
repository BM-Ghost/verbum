package com.verbum.feature.bible.ui.reading

/**
 * How the user navigates through scripture.
 *
 * - [SCROLL]  — continuous vertical scroll, chapters flow into each other like a modern reader.
 * - [CODEX]   — page-by-page experience, mimics turning physical pages of a bound codex.
 */
enum class ReadingMode(val displayName: String, val description: String) {
    SCROLL("Scroll", "Read continuously like a scroll"),
    CODEX("Codex", "Turn pages like a book"),
}
