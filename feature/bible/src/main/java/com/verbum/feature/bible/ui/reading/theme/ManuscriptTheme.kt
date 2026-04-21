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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbum.core.ui.theme.CormorantGaramondFamily
import com.verbum.core.ui.theme.UnifrakturCookFamily

/**
 * Manuscript / Illuminated theme.
 *
 * Inspired by medieval monastic scriptoria — deep vellum background,
 * rich burgundy and gold leaf accents, blackletter headings (UnifrakturCook),
 * and elegant Cormorant Garamond for body text. Decorative fleurons
 * and double-ruled borders evoke hand-lettered Bibles.
 */
@Immutable
class ManuscriptTheme(private val isDark: Boolean) : ReadingTheme {

    override val id = "manuscript"
    override val displayName = "Manuscript"
    override val description = "Illuminated manuscript — medieval monastic beauty"

    private val colors = ReadingThemePalettes.manuscript(isDark)

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

    // ── Typography — UnifrakturCook headings + Cormorant Garamond body ──

    override val verseTextStyle = TextStyle(
        fontFamily = CormorantGaramondFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 29.sp,
        letterSpacing = 0.15.sp,
    )

    override val verseNumberStyle = TextStyle(
        fontFamily = CormorantGaramondFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
    )

    override val chapterTitleStyle = TextStyle(
        fontFamily = UnifrakturCookFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = 1.sp,
    )

    override val dropCapStyle = TextStyle(
        fontFamily = UnifrakturCookFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 64.sp,
    )

    override val showDropCap = true
    override val horizontalPadding = 28.dp

    // ── Ornaments — illuminated manuscript motifs ──

    @Composable
    override fun ChapterOrnament() {
        val gold = accentColor
        val accent = colors.ornamentAccentColor ?: accentColor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(28.dp),
            ) {
                val cx = size.width / 2
                val cy = size.height / 2
                val arm = 10.dp.toPx()

                // Central diamond
                val diamond = Path().apply {
                    moveTo(cx, cy - arm)
                    lineTo(cx + arm * 0.6f, cy)
                    lineTo(cx, cy + arm)
                    lineTo(cx - arm * 0.6f, cy)
                    close()
                }
                drawPath(diamond, gold, style = Stroke(width = 1.5f))

                // Flanking dots
                val spacing = arm * 2.5f
                for (i in 1..2) {
                    val x = cx - spacing * i
                    val xr = cx + spacing * i
                    drawCircle(accent, 2.dp.toPx(), Offset(x, cy))
                    drawCircle(accent, 2.dp.toPx(), Offset(xr, cy))
                }

                // Inner cross in diamond
                drawLine(gold, Offset(cx, cy - arm * 0.4f), Offset(cx, cy + arm * 0.4f), 1f)
                drawLine(gold, Offset(cx - arm * 0.25f, cy), Offset(cx + arm * 0.25f, cy), 1f)
            }
        }
    }

    @Composable
    override fun ChapterDivider() {
        val gold = accentColor.copy(alpha = 0.5f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp)
                .height(12.dp),
        ) {
            val cy = size.height / 2
            val left = size.width * 0.1f
            val right = size.width * 0.9f
            val cx = size.width / 2

            // Double line with central ornament
            drawLine(gold, Offset(left, cy - 3f), Offset(cx - 20f, cy - 3f), 0.8f, cap = StrokeCap.Round)
            drawLine(gold, Offset(cx + 20f, cy - 3f), Offset(right, cy - 3f), 0.8f, cap = StrokeCap.Round)
            drawLine(gold, Offset(left, cy + 3f), Offset(cx - 20f, cy + 3f), 0.8f, cap = StrokeCap.Round)
            drawLine(gold, Offset(cx + 20f, cy + 3f), Offset(right, cy + 3f), 0.8f, cap = StrokeCap.Round)

            // Central diamond
            val d = 6f
            val dm = Path().apply {
                moveTo(cx, cy - d)
                lineTo(cx + d, cy)
                lineTo(cx, cy + d)
                lineTo(cx - d, cy)
                close()
            }
            drawPath(dm, gold)
        }
    }

    @Composable
    override fun PageDecoration() {
        val gold = accentColor.copy(alpha = 0.12f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
        ) {
            drawLine(gold, Offset(0f, 0f), Offset(size.width, 0f), 1.5f)
            drawLine(gold, Offset(0f, size.height), Offset(size.width, size.height), 1.5f)
        }
    }
}
