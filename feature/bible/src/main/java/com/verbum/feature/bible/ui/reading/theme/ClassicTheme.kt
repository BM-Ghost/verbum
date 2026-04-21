package com.verbum.feature.bible.ui.reading.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbum.core.ui.theme.EBGaramondFamily
import com.verbum.core.ui.theme.TrajanProFamily

/**
 * Classic theme.
 *
 * Timeless elegance — warm ivory parchment, refined serif typography
 * (Trajan Pro for headings, EB Garamond for body), and understated
 * cross-motif ornaments. Evokes the tradition of printed Bibles.
 */
@Immutable
class ClassicTheme(private val isDark: Boolean) : ReadingTheme {

    override val id = "classic"
    override val displayName = "Classic"
    override val description = "Timeless elegance — aged parchment with refined serif type"

    private val colors = ReadingThemePalettes.classic(isDark)

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

    // ── Typography — Trajan Pro headings + EB Garamond body ──

    override val verseTextStyle = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.2.sp,
    )

    override val verseNumberStyle = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
    )

    override val chapterTitleStyle = TextStyle(
        fontFamily = TrajanProFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 32.sp,
        letterSpacing = 2.sp,
    )

    override val dropCapStyle = TextStyle(
        fontFamily = TrajanProFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 52.sp,
    )

    override val showDropCap = true

    // ── Ornaments — refined cross motif ──

    @Composable
    override fun ChapterOrnament() {
        val color = accentColor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp),
            ) {
                val cx = size.width / 2
                val cy = size.height / 2
                // Simple cross
                val armLen = 8.dp.toPx()
                val stroke = 1.5f
                drawLine(color, Offset(cx, cy - armLen), Offset(cx, cy + armLen), stroke)
                drawLine(color, Offset(cx - armLen * 0.7f, cy), Offset(cx + armLen * 0.7f, cy), stroke)
                // Flanking dots
                val dotR = 2.dp.toPx()
                drawCircle(color, dotR, Offset(cx - armLen * 2, cy))
                drawCircle(color, dotR, Offset(cx + armLen * 2, cy))
            }
        }
    }

    @Composable
    override fun ChapterDivider() {
        val color = accentColor.copy(alpha = 0.4f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(1.dp),
        ) {
            drawLine(
                color = color,
                start = Offset(size.width * 0.15f, 0f),
                end = Offset(size.width * 0.85f, 0f),
                strokeWidth = 1f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
            )
        }
    }

    @Composable
    override fun PageDecoration() {
        val color = accentColor.copy(alpha = 0.15f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
        ) {
            drawLine(
                color = color,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 2f,
            )
        }
    }
}
