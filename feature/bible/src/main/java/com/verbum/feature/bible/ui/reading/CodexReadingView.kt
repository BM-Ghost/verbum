package com.verbum.feature.bible.ui.reading

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.ui.reading.theme.ReadingTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * A codex reading view that feels like a real Catholic Bible.
 *
 * - Left margin = spine/binding with leather texture
 * - Pages flip from the RIGHT edge (like turning a real book page)
 * - Swipe LEFT (pull page left) = go to PREVIOUS chapter (turning pages back)
 * - Swipe RIGHT = go to NEXT chapter (turning to the next page)
 * - Chapter 1 going back shows a title page with a cross and book name
 * - Gilded page edges, page curl shadow, gutter shadow near spine
 * - Page numbering like a printed Bible
 */

private const val TITLE_PAGE = 0

@Composable
fun CodexReadingView(
    chapter: Int,
    totalChapters: Int,
    verses: List<Verse>,
    bookName: String,
    theme: ReadingTheme,
    onVerseClick: (Verse) -> Unit,
    onChapterChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Internal page: 0 = title page, 1..totalChapters = content
    var displayPage by remember(chapter) { mutableIntStateOf(chapter) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var turnDirection by remember { mutableIntStateOf(0) } // -1 = next, 1 = prev
    val flipProgress = remember { Animatable(0f) } // 0 = flat, 1 = fully turned
    val scope = rememberCoroutineScope()

    val spineColor = theme.accentColor.copy(alpha = 0.8f)
    val gildedEdge = theme.codexGildedEdgeColor
    val shadowTint = theme.codexShadowTint
    val highlightTint = theme.decorativeHighlightTint
    val showBinding = theme.showBookBinding

    Row(modifier = modifier.fillMaxSize()) {
        // ── Book spine / leather binding (only for book-binding themes) ──
        if (showBinding) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                spineColor.copy(alpha = 0.15f),
                                spineColor.copy(alpha = 0.5f),
                                spineColor.copy(alpha = 0.8f),
                                spineColor.copy(alpha = 0.6f),
                                spineColor.copy(alpha = 0.35f),
                            ),
                        ),
                    )
                    .drawBehind {
                        val ridgeColor = spineColor.copy(alpha = 0.2f)
                        val highlightColor = highlightTint.copy(alpha = 0.08f)
                        for (y in 0..size.height.toInt() step 18) {
                            drawLine(ridgeColor, Offset(1.dp.toPx(), y.toFloat()), Offset(size.width - 1.dp.toPx(), y.toFloat()), 0.8f)
                            drawLine(highlightColor, Offset(1.dp.toPx(), y.toFloat() + 1), Offset(size.width - 1.dp.toPx(), y.toFloat() + 1), 0.4f)
                        }
                        drawLine(
                            spineColor.copy(alpha = 0.9f),
                            Offset(size.width / 2, 0f),
                            Offset(size.width / 2, size.height),
                            1.5f,
                        )
                    },
            )
        }

        // ── Page surface ──
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Gilded edge bands (only for book-binding themes)
            if (showBinding) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    gildedEdge.copy(alpha = 0.2f),
                                    gildedEdge.copy(alpha = 0.5f),
                                    gildedEdge.copy(alpha = 0.4f),
                                    gildedEdge.copy(alpha = 0.3f),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    gildedEdge.copy(alpha = 0.15f),
                                    gildedEdge.copy(alpha = 0.4f),
                                    gildedEdge.copy(alpha = 0.35f),
                                    gildedEdge.copy(alpha = 0.2f),
                                ),
                            ),
                        ),
                )
            }

            // ── The page with diagonal corner-peel turn ──
            val edgePad = if (showBinding) 3.dp else 0.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = edgePad, bottom = edgePad)
                    .pointerInput(displayPage, totalChapters) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val threshold = size.width * 0.15f
                                when {
                                    // Swipe LEFT (right-to-left) = NEXT page
                                    dragOffset < -threshold && displayPage < totalChapters -> {
                                        val targetPage = displayPage + 1
                                        turnDirection = -1
                                        scope.launch {
                                            flipProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 500),
                                            )
                                            displayPage = targetPage
                                            onChapterChange(targetPage)
                                            flipProgress.snapTo(0f)
                                            turnDirection = 0
                                        }
                                    }
                                    // Swipe RIGHT (left-to-right) = PREVIOUS page
                                    dragOffset > threshold && displayPage > TITLE_PAGE -> {
                                        val targetPage = displayPage - 1
                                        turnDirection = 1
                                        scope.launch {
                                            flipProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 500),
                                            )
                                            displayPage = targetPage
                                            if (targetPage > TITLE_PAGE) {
                                                onChapterChange(targetPage)
                                            }
                                            flipProgress.snapTo(0f)
                                            turnDirection = 0
                                        }
                                    }
                                    // Not enough drag — snap back
                                    else -> {
                                        scope.launch {
                                            flipProgress.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                            )
                                            turnDirection = 0
                                        }
                                    }
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                dragOffset = 0f
                                scope.launch {
                                    flipProgress.snapTo(0f)
                                    turnDirection = 0
                                }
                            },
                            onHorizontalDrag = { _, delta ->
                                dragOffset += delta
                                turnDirection = if (dragOffset < 0) -1 else 1
                                val progress = (dragOffset.absoluteValue / 500f)
                                    .coerceIn(0f, 0.5f)
                                scope.launch { flipProgress.snapTo(progress) }
                            },
                        )
                    },
            ) {
                val progress = flipProgress.value
                // Diagonal page curl: main Y rotation + slight X tilt for corner effect
                val mainAngle = progress * 160f  // not full 180 so it looks like lifting
                val tiltAngle = progress * 12f   // slight top-edge lift for diagonal feel

                // Layer 1: The page being revealed underneath
                if (progress > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(theme.pageBackground)
                            .drawBehind {
                                // Soft shadow cast by the lifted page
                                val shadowIntensity = minOf(progress, 1f - progress) * 2f
                                drawRect(shadowTint.copy(alpha = 0.06f * shadowIntensity))

                                // Diagonal fold-line shadow sweeping across
                                val foldX = if (turnDirection == -1) {
                                    size.width * (1f - progress)
                                } else {
                                    size.width * progress
                                }
                                val shadowW = 60.dp.toPx()
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            shadowTint.copy(alpha = 0f),
                                            shadowTint.copy(alpha = 0.18f * shadowIntensity),
                                            shadowTint.copy(alpha = 0.06f * shadowIntensity),
                                            shadowTint.copy(alpha = 0f),
                                        ),
                                        startX = (foldX - shadowW / 2).coerceAtLeast(0f),
                                        endX = (foldX + shadowW / 2).coerceAtMost(size.width),
                                    ),
                                )

                                // Corner fold triangle — the visible folded corner
                                val cornerSize = (progress * 0.3f).coerceAtMost(0.2f)
                                if (cornerSize > 0.01f && turnDirection != 0) {
                                    val cw = size.width * cornerSize
                                    val ch = size.height * cornerSize * 0.7f
                                    val foldPath = Path()
                                    if (turnDirection == -1) {
                                        // Fold from top-right corner
                                        foldPath.moveTo(size.width, 0f)
                                        foldPath.lineTo(size.width - cw, 0f)
                                        foldPath.lineTo(size.width, ch)
                                        foldPath.close()
                                    } else {
                                        // Fold from top-left corner
                                        foldPath.moveTo(0f, 0f)
                                        foldPath.lineTo(cw, 0f)
                                        foldPath.lineTo(0f, ch)
                                        foldPath.close()
                                    }
                                    // Back side of fold — slightly darker
                                    drawPath(
                                        foldPath,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                theme.pageBackground.copy(alpha = 0.9f),
                                                shadowTint.copy(alpha = 0.08f),
                                            ),
                                        ),
                                    )
                                }
                            },
                    )
                }

                // Layer 2: The current page with diagonal lift
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            cameraDistance = 12f * density

                            when (turnDirection) {
                                -1 -> {
                                    // Turning forward: page lifts from right edge
                                    // Pivot at bottom-left for diagonal peel from top-right corner
                                    transformOrigin = TransformOrigin(0f, 0.85f)
                                    rotationY = mainAngle
                                    rotationX = -tiltAngle  // top tilts away
                                }
                                1 -> {
                                    // Turning back: page lifts from left edge
                                    // Pivot at bottom-right for diagonal peel from top-left corner
                                    transformOrigin = TransformOrigin(1f, 0.85f)
                                    rotationY = -mainAngle
                                    rotationX = -tiltAngle
                                }
                            }

                            // Fade as page turns past the midpoint
                            alpha = when {
                                mainAngle > 90f -> 1f - ((mainAngle - 90f) / 90f) * 0.7f
                                else -> 1f
                            }
                        }
                        .background(theme.pageBackground)
                        .drawBehind {
                            // Gutter shadow near spine (only for binding themes)
                            if (showBinding) {
                                val gutterWidth = 28.dp.toPx()
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            shadowTint.copy(alpha = 0.1f),
                                            shadowTint.copy(alpha = 0.03f),
                                            shadowTint.copy(alpha = 0f),
                                        ),
                                        startX = 0f,
                                        endX = gutterWidth,
                                    ),
                                    size = Size(gutterWidth, size.height),
                                )
                            }

                            // Edge shadow on the turning side
                            if (mainAngle > 3f) {
                                val edgeShadowWidth = 20.dp.toPx()
                                val edgeAlpha = 0.2f * (mainAngle / 160f)
                                if (turnDirection == -1) {
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                shadowTint.copy(alpha = 0f),
                                                shadowTint.copy(alpha = edgeAlpha),
                                            ),
                                            startX = size.width - edgeShadowWidth,
                                            endX = size.width,
                                        ),
                                        topLeft = Offset(size.width - edgeShadowWidth, 0f),
                                        size = Size(edgeShadowWidth, size.height),
                                    )
                                } else {
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                shadowTint.copy(alpha = edgeAlpha),
                                                shadowTint.copy(alpha = 0f),
                                            ),
                                            startX = 0f,
                                            endX = edgeShadowWidth,
                                        ),
                                        size = Size(edgeShadowWidth, size.height),
                                    )
                                }
                            }

                            // Darken when seeing "back" of page
                            if (mainAngle > 90f) {
                                val backAlpha = 0.15f * ((mainAngle - 90f) / 70f)
                                drawRect(shadowTint.copy(alpha = backAlpha))
                            }
                        },
                ) {
                    if (displayPage == TITLE_PAGE) {
                        TitlePage(bookName = bookName, theme = theme)
                    } else {
                        ChapterPage(
                            chapter = displayPage,
                            totalChapters = totalChapters,
                            verses = verses,
                            bookName = bookName,
                            theme = theme,
                            onVerseClick = onVerseClick,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Title page — shown before chapter 1, like the title page of a Catholic Bible.
 * Features a Latin cross, the book name, and "Holy Bible".
 */
@Composable
private fun TitlePage(
    bookName: String,
    theme: ReadingTheme,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(1f))

        // Decorative Latin cross
        val crossColor = theme.accentColor
        Canvas(modifier = Modifier.height(80.dp).width(50.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val armV = 36.dp.toPx()
            val armH = 20.dp.toPx()
            val thick = 3.dp.toPx()

            // Vertical beam
            drawRoundRect(
                color = crossColor,
                topLeft = Offset(cx - thick / 2, cy - armV),
                size = Size(thick, armV * 2),
                cornerRadius = CornerRadius(thick / 2),
            )
            // Horizontal beam (above center — Latin cross)
            drawRoundRect(
                color = crossColor,
                topLeft = Offset(cx - armH, cy - armV * 0.35f - thick / 2),
                size = Size(armH * 2, thick),
                cornerRadius = CornerRadius(thick / 2),
            )
            // Circle at intersection
            drawCircle(
                color = crossColor.copy(alpha = 0.3f),
                radius = 6.dp.toPx(),
                center = Offset(cx, cy - armV * 0.35f),
            )
        }

        Spacer(Modifier.height(32.dp))

        // Ornamental rule
        Canvas(modifier = Modifier.fillMaxWidth(0.5f).height(2.dp)) {
            drawLine(
                color = theme.accentColor.copy(alpha = 0.4f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.5f,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "THE BOOK OF",
            style = theme.verseNumberStyle.copy(
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Normal,
            ),
            color = theme.textColor.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = bookName.uppercase(),
            style = theme.chapterTitleStyle.copy(
                fontSize = 32.sp,
                letterSpacing = 3.sp,
            ),
            color = theme.chapterTitleColor,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        Canvas(modifier = Modifier.fillMaxWidth(0.5f).height(2.dp)) {
            drawLine(
                color = theme.accentColor.copy(alpha = 0.4f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.5f,
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Holy Bible",
            style = theme.verseTextStyle.copy(
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
            ),
            color = theme.textColor.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1.5f))

        Text(
            text = "\u2190 Swipe left to begin reading",
            style = theme.verseNumberStyle.copy(fontSize = 11.sp),
            color = theme.textColor.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * A chapter content page — header, ornaments, verses, page number.
 * Styled like a printed Catholic Bible page.
 */
@Composable
private fun ChapterPage(
    chapter: Int,
    totalChapters: Int,
    verses: List<Verse>,
    bookName: String,
    theme: ReadingTheme,
    onVerseClick: (Verse) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top decoration
        theme.PageDecoration()

        LazyColumn(
            contentPadding = PaddingValues(
                start = theme.horizontalPadding + if (theme.showBookBinding) 12.dp else 0.dp,
                end = theme.horizontalPadding,
                top = 12.dp,
                bottom = 16.dp,
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            // Running header (like printed Bibles)
            item(key = "running_header") {
                Text(
                    text = bookName.uppercase(),
                    style = theme.verseNumberStyle.copy(
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                    ),
                    color = theme.textColor.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }

            // Chapter heading
            item(key = "chapter_header") {
                Column(
                    modifier = Modifier.padding(
                        top = theme.chapterHeaderTopPadding / 2,
                        bottom = 16.dp,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    theme.ChapterOrnament()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "CHAPTER",
                        style = theme.verseNumberStyle.copy(
                            fontSize = 10.sp,
                            letterSpacing = 3.sp,
                        ),
                        color = theme.chapterTitleColor.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$chapter",
                        style = theme.chapterTitleStyle,
                        color = theme.chapterTitleColor,
                    )
                }
            }

            // Verses
            items(verses, key = { it.verseNumber }) { verse ->
                ThemedVerseItem(
                    verse = verse,
                    theme = theme,
                    isFirstVerse = verse.verseNumber == 1 && theme.showDropCap,
                    onClick = { onVerseClick(verse) },
                )
            }

            item(key = "bottom") {
                Spacer(Modifier.height(24.dp))
            }
        }

        // Bottom decoration
        theme.PageDecoration()

        // Page number
        Text(
            text = "\u2014 $chapter \u2014",
            style = theme.verseNumberStyle.copy(fontSize = 10.sp),
            color = theme.textColor.copy(alpha = 0.35f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        )
    }
}
