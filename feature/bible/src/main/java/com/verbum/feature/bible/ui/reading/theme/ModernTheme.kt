package com.verbum.feature.bible.ui.reading.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbum.core.ui.theme.InterFamily

/**
 * Modern / Minimal theme.
 *
 * Clean contemporary design — white/dark background, generous whitespace,
 * sans-serif typography, and subtle material-style dividers.
 * Feels like a well-designed modern Bible app.
 */
@Immutable
class ModernTheme(private val isDark: Boolean) : ReadingTheme {

    override val id = "modern"
    override val displayName = "Modern"
    override val description = "Clean & minimal — a contemporary reading experience"

    private val colors = ReadingThemePalettes.modern(isDark)

    override val pageBackground get() = colors.pageBackground
    override val textColor get() = colors.textColor
    override val verseNumberColor get() = colors.verseNumberColor
    override val chapterTitleColor get() = colors.chapterTitleColor
    override val accentColor get() = colors.accentColor
    override val toolbarBackground get() = colors.toolbarBackground
    override val toolbarContent get() = colors.toolbarContent
    override val codexGildedEdgeColor get() = colors.codexGildedEdgeColor ?: accentColor
    override val codexShadowTint get() = colors.codexShadowTint ?: toolbarContent
    override val scrollShadowTint get() = colors.scrollShadowTint ?: toolbarContent
    override val decorativeHighlightTint get() = colors.decorativeHighlightTint

    // ── Typography (all Inter — clean sans-serif) ──

    override val verseTextStyle = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp,
    )

    override val verseNumberStyle = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
    )

    override val chapterTitleStyle = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )

    override val dropCapStyle = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 44.sp,
    )

    override val showDropCap = false
    override val showScrollDecor = false
    override val showBookBinding = false
    override val allowedModes = setOf("SCROLL")
    override val horizontalPadding = 20.dp
    override val verseSpacing = 6.dp

    @Composable
    override fun ChapterDivider() {
        val color = colors.dividerColor ?: accentColor
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .height(1.dp),
        ) {
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1f,
            )
        }
    }
}
