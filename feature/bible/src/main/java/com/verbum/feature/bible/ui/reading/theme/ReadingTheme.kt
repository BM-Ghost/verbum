package com.verbum.feature.bible.ui.reading.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A pluggable reading theme that controls every visual aspect of Bible reading.
 *
 * To create a new theme, implement this interface and provide it
 * via [ReadingThemeType]. Each theme is self-contained — colours,
 * typography, decorations, and ornamental composables are all
 * defined here so themes can be swapped without touching layout code.
 */
@Immutable
interface ReadingTheme {

    /** Unique identifier for persistence. */
    val id: String

    /** Human-readable name shown in the theme picker. */
    val displayName: String

    /** One-line description for the picker. */
    val description: String

    // ── Colours ──────────────────────────────────────────────

    /** Page / canvas background. */
    val pageBackground: Color

    /** Main text colour. */
    val textColor: Color

    /** Verse-number superscript colour. */
    val verseNumberColor: Color

    /** Chapter title / header colour. */
    val chapterTitleColor: Color

    /** Accent colour for ornaments, drop caps, dividers. */
    val accentColor: Color

    /** Surface colour for toolbar / overlays when in this theme. */
    val toolbarBackground: Color

    /** Text / icon colour on the toolbar surface. */
    val toolbarContent: Color

    // ── Typography ──────────────────────────────────────────

    /** Style for the main verse body text. */
    val verseTextStyle: TextStyle

    /** Style for the superscript verse number. */
    val verseNumberStyle: TextStyle

    /** Style for chapter headings rendered inline. */
    val chapterTitleStyle: TextStyle

    /** Style for the drop-cap (first letter of a chapter). */
    val dropCapStyle: TextStyle

    // ── Spacing & dimensions ────────────────────────────────

    /** Horizontal padding of the reading surface. */
    val horizontalPadding: Dp get() = 24.dp

    /** Vertical gap between verses. */
    val verseSpacing: Dp get() = 4.dp

    /** Extra space before a chapter heading. */
    val chapterHeaderTopPadding: Dp get() = 32.dp

    /** Whether to show an ornamental drop-cap at chapter start. */
    val showDropCap: Boolean get() = false

    /** Whether to show scroll rollers / parchment fold effects in scroll mode. */
    val showScrollDecor: Boolean get() = true

    /** Whether to show book spine and gilded edges in codex mode. */
    val showBookBinding: Boolean get() = true

    /** Reading modes available for this theme. Null = all modes allowed. */
    val allowedModes: Set<String>? get() = null

    // ── Decorative tokens ───────────────────────────────────

    /** Edge color used for gilded page bands in codex mode. */
    val codexGildedEdgeColor: Color get() = accentColor

    /** Tint used for codex overlays and paper shadows. */
    val codexShadowTint: Color get() = toolbarContent

    /** Tint used for scroll overlays and parchment shadows. */
    val scrollShadowTint: Color get() = toolbarContent

    /** Tint used for shiny highlight lines in codex/scroll decorations. */
    val decorativeHighlightTint: Color get() = Color.White

    // ── Decorative composables ──────────────────────────────

    /** Optional ornament rendered above a chapter heading. */
    @Composable
    fun ChapterOrnament() {}

    /** Optional divider drawn between chapters in scroll mode. */
    @Composable
    fun ChapterDivider() {}

    /** Optional page-edge decoration for codex mode. */
    @Composable
    fun PageDecoration() {}
}

/**
 * Registry of built-in themes. Adding a new theme = adding one enum entry
 * and one [ReadingTheme] implementation — nothing else changes.
 */
enum class ReadingThemeType(
    val displayName: String,
    val description: String,
) {
    CLASSIC(
        displayName = "Classic",
        description = "Timeless elegance — aged parchment with refined serif type",
    ),
    MANUSCRIPT(
        displayName = "Manuscript",
        description = "Illuminated manuscript — medieval monastic beauty",
    ),
    MODERN(
        displayName = "Modern",
        description = "Clean & minimal — a contemporary reading experience",
    ),
    ;

    fun resolve(isDark: Boolean): ReadingTheme = when (this) {
        CLASSIC -> ClassicTheme(isDark)
        MANUSCRIPT -> ManuscriptTheme(isDark)
        MODERN -> ModernTheme(isDark)
    }
}
