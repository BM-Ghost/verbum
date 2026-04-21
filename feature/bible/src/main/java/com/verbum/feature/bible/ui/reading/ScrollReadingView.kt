package com.verbum.feature.bible.ui.reading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.ui.reading.theme.ReadingTheme
import kotlin.math.abs

/**
 * Represents one chapter's worth of content in the scroll view.
 */
data class ChapterBlock(
    val chapter: Int,
    val verses: List<Verse>,
)

/**
 * Continuous-scroll reading view styled like an ancient scroll.
 *
 * The top features a rolled-scroll curl that extends under the toolbar,
 * giving the illusion of parchment unrolling. A matching curl sits at
 * the bottom. The content scrolls between the two "rollers".
 */
@Composable
fun ScrollReadingView(
    chapters: List<ChapterBlock>,
    currentChapter: Int,
    bookName: String,
    theme: ReadingTheme,
    onVerseClick: (Verse) -> Unit,
    onLoadNextChapter: () -> Unit,
    onVisibleChapterChange: (Int) -> Unit,
    onVisibleVerseChange: (chapter: Int, verse: Int) -> Unit,
    targetVerse: Int? = null,
    onTargetVerseConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    // Scroll to requested chapter header when chapter changes via toolbar/chapter picker.
    LaunchedEffect(currentChapter, chapters) {
        var targetIndex = -1
        var index = 0
        chapters.forEachIndexed { chapterIndex, block ->
            if (chapterIndex > 0) index += 1 // divider
            if (block.chapter == currentChapter) {
                targetIndex = index // header item for this chapter
                return@forEachIndexed
            }
            index += 1 // header
            index += block.verses.size // verses
        }

        if (targetIndex >= 0 && abs(listState.firstVisibleItemIndex - targetIndex) > 1) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Track visible chapter + verse from the first visible item's key.
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .mapNotNull { it.key as? String }
                .firstOrNull()
        }.collect { key ->
            if (key == null) return@collect
            when {
                key.startsWith("header_") -> {
                    key.removePrefix("header_").toIntOrNull()?.let { onVisibleChapterChange(it) }
                }
                key.startsWith("v_") -> {
                    val parts = key.split("_")
                    val ch = parts.getOrNull(1)?.toIntOrNull()
                    val v = parts.getOrNull(2)?.toIntOrNull()
                    if (ch != null) onVisibleChapterChange(ch)
                    if (ch != null && v != null) onVisibleVerseChange(ch, v)
                }
                key.startsWith("divider_") -> {
                    key.removePrefix("divider_").toIntOrNull()?.let { onVisibleChapterChange(it) }
                }
            }
        }
    }

    // Scroll to requested verse in current chapter.
    LaunchedEffect(currentChapter, targetVerse, chapters) {
        val verse = targetVerse ?: return@LaunchedEffect
        var targetIndex = -1
        var index = 0
        chapters.forEachIndexed { chapterIndex, block ->
            if (chapterIndex > 0) index += 1 // divider
            index += 1 // header
            block.verses.forEach { v ->
                if (block.chapter == currentChapter && v.verseNumber == verse) {
                    targetIndex = index
                    return@forEach
                }
                index += 1
            }
            if (targetIndex >= 0) return@forEachIndexed
        }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
        onTargetVerseConsumed()
    }

    // Trigger loading next chapter when near the end
    LaunchedEffect(listState, chapters.size) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last to total
        }.collect { (lastVisible, total) ->
            if (total > 0 && lastVisible >= total - 5) {
                onLoadNextChapter()
            }
        }
    }

    // Derive scroll direction for the roller visual
    val isScrollingDown by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 0 }
    }

    val scrollRollerColor = theme.accentColor
    val shadowTint = theme.scrollShadowTint
    val highlightTint = theme.decorativeHighlightTint
    val pageBg = theme.pageBackground
    val showDecor = theme.showScrollDecor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.pageBackground),
    ) {
        // ── Top scroll roller (only for scroll-decor themes) ──
        if (showDecor) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
            ) {
                val w = size.width
                val h = size.height

                // Shadow beneath the roller
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            shadowTint.copy(alpha = 0.15f),
                            shadowTint.copy(alpha = 0.05f),
                            shadowTint.copy(alpha = 0f),
                        ),
                        startY = h * 0.55f,
                        endY = h,
                    ),
                    topLeft = Offset(0f, h * 0.55f),
                )

                // Roller body — 3D cylinder
                val rollerPath = Path().apply {
                    moveTo(0f, h * 0.25f)
                    cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, h * 0.25f)
                    lineTo(w, h * 0.6f)
                    cubicTo(w * 0.75f, h * 0.82f, w * 0.25f, h * 0.82f, 0f, h * 0.6f)
                    close()
                }
                drawPath(
                    rollerPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.12f),
                            scrollRollerColor.copy(alpha = 0.3f),
                            scrollRollerColor.copy(alpha = 0.5f),
                            scrollRollerColor.copy(alpha = 0.35f),
                            scrollRollerColor.copy(alpha = 0.12f),
                        ),
                    ),
                    style = Fill,
                )

                // Specular highlight across the roller
                drawLine(
                    color = highlightTint.copy(alpha = 0.15f),
                    start = Offset(w * 0.05f, h * 0.38f),
                    end = Offset(w * 0.95f, h * 0.38f),
                    strokeWidth = 2.5f,
                )

                // Bottom lip of roller
                val lipPath = Path().apply {
                    moveTo(0f, h * 0.6f)
                    cubicTo(w * 0.25f, h * 0.82f, w * 0.75f, h * 0.82f, w, h * 0.6f)
                }
                drawPath(
                    lipPath,
                    color = scrollRollerColor.copy(alpha = 0.3f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                )

                // Parchment unrolling curl — the sheet curving out from under the roller
                val curlPath = Path().apply {
                    moveTo(0f, h * 0.6f)
                    cubicTo(w * 0.25f, h * 0.82f, w * 0.75f, h * 0.82f, w, h * 0.6f)
                    lineTo(w, h)
                    cubicTo(w * 0.8f, h * 0.88f, w * 0.2f, h * 0.88f, 0f, h)
                    close()
                }
                drawPath(
                    curlPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.22f),
                            scrollRollerColor.copy(alpha = 0.06f),
                            scrollRollerColor.copy(alpha = 0f),
                        ),
                        startY = h * 0.6f,
                        endY = h,
                    ),
                )

                // Curl highlight — light catching on the bend
                val curlHighlight = Path().apply {
                    moveTo(0f, h * 0.7f)
                    cubicTo(w * 0.25f, h * 0.85f, w * 0.75f, h * 0.85f, w, h * 0.7f)
                }
                drawPath(
                    curlHighlight,
                    color = highlightTint.copy(alpha = 0.1f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
                )

                // Wooden dowel ends
                val dowelRadius = 6.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.6f),
                            scrollRollerColor.copy(alpha = 0.25f),
                        ),
                    ),
                    radius = dowelRadius,
                    center = Offset(dowelRadius + 2.dp.toPx(), h * 0.43f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.6f),
                            scrollRollerColor.copy(alpha = 0.25f),
                        ),
                    ),
                    radius = dowelRadius,
                    center = Offset(w - dowelRadius - 2.dp.toPx(), h * 0.43f),
                )
            }
        }

        // ── Scroll content with parchment fold ──
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = theme.horizontalPadding,
                    vertical = 16.dp,
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                chapters.forEachIndexed { chapterIndex, block ->
                    // Divider between chapters
                    if (chapterIndex > 0) {
                        item(key = "divider_${block.chapter}") {
                            theme.ChapterDivider()
                        }
                    }

                    // Chapter ornament + heading
                    item(key = "header_${block.chapter}") {
                        Column {
                            theme.ChapterOrnament()
                            Spacer(Modifier.height(theme.verseSpacing))
                            Text(
                                text = "Chapter ${block.chapter}",
                                style = theme.chapterTitleStyle,
                                color = theme.chapterTitleColor,
                                modifier = Modifier.padding(bottom = theme.verseSpacing + theme.verseSpacing),
                            )
                        }
                    }

                    // Verses
                    items(
                        items = block.verses,
                        key = { "v_${block.chapter}_${it.verseNumber}" },
                    ) { verse ->
                        ThemedVerseItem(
                            verse = verse,
                            theme = theme,
                            isFirstVerse = verse.verseNumber == 1 && theme.showDropCap,
                            onClick = { onVerseClick(verse) },
                        )
                    }
                }

                // Bottom breathing room
                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(theme.chapterHeaderTopPadding * 2))
                }
            }

            // Top parchment fold (only for scroll-decor themes)
            if (showDecor) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.TopCenter),
                ) {
                    val w = size.width
                    val h = size.height

                    // Curling parchment coming out from under the roller
                    // Outer sheet surface — the visible top of the curl
                    val curlOuter = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.35f)
                        cubicTo(w * 0.8f, h * 0.7f, w * 0.6f, h * 0.9f, w * 0.5f, h)
                        cubicTo(w * 0.4f, h * 0.9f, w * 0.2f, h * 0.7f, 0f, h * 0.35f)
                        close()
                    }
                    drawPath(
                        curlOuter,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scrollRollerColor.copy(alpha = 0.2f),
                                scrollRollerColor.copy(alpha = 0.1f),
                                pageBg.copy(alpha = 0.3f),
                                scrollRollerColor.copy(alpha = 0f),
                            ),
                        ),
                    )

                    // Inner shadow of the curl — darker inside the curve
                    val innerShadow = Path().apply {
                        moveTo(0f, h * 0.15f)
                        cubicTo(w * 0.2f, h * 0.5f, w * 0.4f, h * 0.72f, w * 0.5f, h * 0.82f)
                        cubicTo(w * 0.6f, h * 0.72f, w * 0.8f, h * 0.5f, w, h * 0.15f)
                        lineTo(w, h * 0.35f)
                        cubicTo(w * 0.8f, h * 0.7f, w * 0.6f, h * 0.9f, w * 0.5f, h)
                        cubicTo(w * 0.4f, h * 0.9f, w * 0.2f, h * 0.7f, 0f, h * 0.35f)
                        close()
                    }
                    drawPath(
                        innerShadow,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                shadowTint.copy(alpha = 0f),
                                shadowTint.copy(alpha = 0.08f),
                                shadowTint.copy(alpha = 0.14f),
                                shadowTint.copy(alpha = 0f),
                            ),
                        ),
                    )

                    // Highlight ridge at the crest of the curl
                    val ridgePath = Path().apply {
                        moveTo(w * 0.05f, h * 0.18f)
                        cubicTo(w * 0.25f, h * 0.22f, w * 0.75f, h * 0.22f, w * 0.95f, h * 0.18f)
                    }
                    drawPath(
                        ridgePath,
                        color = highlightTint.copy(alpha = 0.12f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                    )
                }
            }

            // Bottom parchment fold (only for scroll-decor themes)
            if (showDecor) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    val w = size.width
                    val h = size.height

                    // Curling parchment going into the bottom roller
                    val curlOuter = Path().apply {
                        moveTo(0f, h)
                        lineTo(w, h)
                        lineTo(w, h * 0.65f)
                        cubicTo(w * 0.8f, h * 0.3f, w * 0.6f, h * 0.1f, w * 0.5f, 0f)
                        cubicTo(w * 0.4f, h * 0.1f, w * 0.2f, h * 0.3f, 0f, h * 0.65f)
                        close()
                    }
                    drawPath(
                        curlOuter,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scrollRollerColor.copy(alpha = 0f),
                                pageBg.copy(alpha = 0.3f),
                                scrollRollerColor.copy(alpha = 0.1f),
                                scrollRollerColor.copy(alpha = 0.2f),
                            ),
                        ),
                    )

                    // Inner curl shadow
                    val innerShadow = Path().apply {
                        moveTo(0f, h * 0.85f)
                        cubicTo(w * 0.2f, h * 0.5f, w * 0.4f, h * 0.28f, w * 0.5f, h * 0.18f)
                        cubicTo(w * 0.6f, h * 0.28f, w * 0.8f, h * 0.5f, w, h * 0.85f)
                        lineTo(w, h * 0.65f)
                        cubicTo(w * 0.8f, h * 0.3f, w * 0.6f, h * 0.1f, w * 0.5f, 0f)
                        cubicTo(w * 0.4f, h * 0.1f, w * 0.2f, h * 0.3f, 0f, h * 0.65f)
                        close()
                    }
                    drawPath(
                        innerShadow,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                shadowTint.copy(alpha = 0f),
                                shadowTint.copy(alpha = 0.12f),
                                shadowTint.copy(alpha = 0.06f),
                                shadowTint.copy(alpha = 0f),
                            ),
                        ),
                    )

                    // Highlight ridge
                    val ridgePath = Path().apply {
                        moveTo(w * 0.05f, h * 0.82f)
                        cubicTo(w * 0.25f, h * 0.78f, w * 0.75f, h * 0.78f, w * 0.95f, h * 0.82f)
                    }
                    drawPath(
                        ridgePath,
                        color = highlightTint.copy(alpha = 0.08f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
                    )
                }
            }
        }

        // ── Bottom scroll roller (only for scroll-decor themes) ──
        if (showDecor) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
            ) {
                val w = size.width
                val h = size.height

                // Shadow above the roller
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            shadowTint.copy(alpha = 0f),
                            shadowTint.copy(alpha = 0.05f),
                            shadowTint.copy(alpha = 0.12f),
                        ),
                        startY = 0f,
                        endY = h * 0.4f,
                    ),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.4f),
                )

                // Bottom roller body
                val rollerPath = Path().apply {
                    moveTo(0f, h * 0.3f)
                    cubicTo(w * 0.25f, h * 0.12f, w * 0.75f, h * 0.12f, w, h * 0.3f)
                    lineTo(w, h * 0.7f)
                    cubicTo(w * 0.75f, h, w * 0.25f, h, 0f, h * 0.7f)
                    close()
                }
                drawPath(
                    rollerPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.1f),
                            scrollRollerColor.copy(alpha = 0.3f),
                            scrollRollerColor.copy(alpha = 0.45f),
                            scrollRollerColor.copy(alpha = 0.28f),
                        ),
                    ),
                    style = Fill,
                )

                // Specular highlight
                drawLine(
                    color = highlightTint.copy(alpha = 0.1f),
                    start = Offset(w * 0.05f, h * 0.45f),
                    end = Offset(w * 0.95f, h * 0.45f),
                    strokeWidth = 2f,
                )

                // Dowel ends
                val dowelRadius = 5.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.55f),
                            scrollRollerColor.copy(alpha = 0.2f),
                        ),
                    ),
                    radius = dowelRadius,
                    center = Offset(dowelRadius + 2.dp.toPx(), h * 0.5f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scrollRollerColor.copy(alpha = 0.55f),
                            scrollRollerColor.copy(alpha = 0.2f),
                        ),
                    ),
                    radius = dowelRadius,
                    center = Offset(w - dowelRadius - 2.dp.toPx(), h * 0.5f),
                )
            }
        }
    }
}

/**
 * A single verse rendered according to the active [ReadingTheme].
 */
@Composable
internal fun ThemedVerseItem(
    verse: Verse,
    theme: ReadingTheme,
    isFirstVerse: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isFirstVerse && verse.text.isNotEmpty()) {
        // Drop-cap layout: large first letter + rest of verse
        val firstChar = verse.text.first().toString()
        val rest = verse.text.drop(1)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = theme.verseSpacing),
        ) {
            Text(
                text = firstChar,
                style = theme.dropCapStyle,
                color = theme.accentColor,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = theme.verseNumberColor,
                    )) {
                        append("${verse.verseNumber} ")
                    }
                    append(rest)
                },
                style = theme.verseTextStyle,
                color = theme.textColor,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    } else {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = theme.verseNumberColor,
                )) {
                    append("${verse.verseNumber} ")
                }
                append(verse.text)
            },
            style = theme.verseTextStyle,
            color = theme.textColor,
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = theme.verseSpacing),
        )
    }
}
