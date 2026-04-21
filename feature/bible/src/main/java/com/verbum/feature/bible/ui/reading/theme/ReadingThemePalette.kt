package com.verbum.feature.bible.ui.reading.theme

import androidx.compose.ui.graphics.Color

data class ReadingThemeColors(
    val pageBackground: Color,
    val textColor: Color,
    val verseNumberColor: Color,
    val chapterTitleColor: Color,
    val accentColor: Color,
    val toolbarBackground: Color,
    val toolbarContent: Color,
    val ornamentAccentColor: Color? = null,
    val dividerColor: Color? = null,
    val codexGildedEdgeColor: Color? = null,
    val codexShadowTint: Color? = null,
    val scrollShadowTint: Color? = null,
    val decorativeHighlightTint: Color = Color.White,
)

object ReadingThemePalettes {
    private val classicLight = ReadingThemeColors(
        pageBackground = Color(0xFFFAF3E0),
        textColor = Color(0xFF4E3524),
        verseNumberColor = Color(0xFF7B241C),
        chapterTitleColor = Color(0xFF8B6914),
        accentColor = Color(0xFF8B6914),
        toolbarBackground = Color(0xFFF0E6D0),
        toolbarContent = Color(0xFF4E3524),
        codexGildedEdgeColor = Color(0xFFD4AF37),
        codexShadowTint = Color(0xFF2B1C12),
        scrollShadowTint = Color(0xFF2B1C12),
    )

    private val classicDark = ReadingThemeColors(
        pageBackground = Color(0xFF1F1B14),
        textColor = Color(0xFFD2B48C),
        verseNumberColor = Color(0xFFCB6D5C),
        chapterTitleColor = Color(0xFFD4A84B),
        accentColor = Color(0xFFD4A84B),
        toolbarBackground = Color(0xFF2A2318),
        toolbarContent = Color(0xFFD2B48C),
        codexGildedEdgeColor = Color(0xFFD4A84B),
        codexShadowTint = Color(0xFFE8D7B0),
        scrollShadowTint = Color(0xFFE8D7B0),
    )

    private val manuscriptLight = ReadingThemeColors(
        pageBackground = Color(0xFFEDE0C8),
        textColor = Color(0xFF1A1200),
        verseNumberColor = Color(0xFF6B1D2A),
        chapterTitleColor = Color(0xFFB8860B),
        accentColor = Color(0xFFB8860B),
        toolbarBackground = Color(0xFFDDD0B4),
        toolbarContent = Color(0xFF1A1200),
        ornamentAccentColor = Color(0xFF1A237E),
        codexGildedEdgeColor = Color(0xFFB8860B),
        codexShadowTint = Color(0xFF24190A),
        scrollShadowTint = Color(0xFF24190A),
    )

    private val manuscriptDark = ReadingThemeColors(
        pageBackground = Color(0xFF1A150E),
        textColor = Color(0xFFE8D8B8),
        verseNumberColor = Color(0xFFD4637A),
        chapterTitleColor = Color(0xFFDAA520),
        accentColor = Color(0xFFDAA520),
        toolbarBackground = Color(0xFF241C10),
        toolbarContent = Color(0xFFE8D8B8),
        ornamentAccentColor = Color(0xFF7986CB),
        codexGildedEdgeColor = Color(0xFFDAA520),
        codexShadowTint = Color(0xFFF5E7C8),
        scrollShadowTint = Color(0xFFF5E7C8),
    )

    private val modernLight = ReadingThemeColors(
        pageBackground = Color(0xFFFFFFFF),
        textColor = Color(0xFF212121),
        verseNumberColor = Color(0xFF1565C0),
        chapterTitleColor = Color(0xFF0D47A1),
        accentColor = Color(0xFF1976D2),
        toolbarBackground = Color(0xFFFAFAFA),
        toolbarContent = Color(0xFF212121),
        dividerColor = Color(0xFFE0E0E0),
        codexShadowTint = Color(0xFF1E1E1E),
        scrollShadowTint = Color(0xFF1E1E1E),
    )

    private val modernDark = ReadingThemeColors(
        pageBackground = Color(0xFF121212),
        textColor = Color(0xFFE0E0E0),
        verseNumberColor = Color(0xFF90CAF9),
        chapterTitleColor = Color(0xFFBBDEFB),
        accentColor = Color(0xFF64B5F6),
        toolbarBackground = Color(0xFF1E1E1E),
        toolbarContent = Color(0xFFE0E0E0),
        dividerColor = Color(0xFF333333),
        codexShadowTint = Color(0xFFE0E0E0),
        scrollShadowTint = Color(0xFFE0E0E0),
    )

    fun classic(isDark: Boolean): ReadingThemeColors = if (isDark) classicDark else classicLight

    fun manuscript(isDark: Boolean): ReadingThemeColors = if (isDark) manuscriptDark else manuscriptLight

    fun modern(isDark: Boolean): ReadingThemeColors = if (isDark) modernDark else modernLight
}
