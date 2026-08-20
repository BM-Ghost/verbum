package com.verbum.feature.bible.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.ui.components.VerbumErrorState
import com.verbum.core.ui.components.VerbumLoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.domain.model.BibleCrossReference
import com.verbum.feature.bible.ui.reading.ChapterBlock
import com.verbum.feature.bible.ui.reading.ChapterNavigator
import com.verbum.feature.bible.ui.reading.CodexReadingView
import com.verbum.feature.bible.ui.reading.ReadingMode
import com.verbum.feature.bible.ui.reading.ReadingToolbar
import com.verbum.feature.bible.ui.reading.ScrollReadingView
import com.verbum.feature.bible.ui.reading.SearchResultsOverlay
import com.verbum.feature.bible.ui.reading.VerseSearchBar
import com.verbum.feature.bible.ui.reading.theme.ReadingThemeType
import com.verbum.feature.bible.ui.TargetVerseLocation
import timber.log.Timber

@Composable
fun BibleReaderScreen(
    onNavigateBack: () -> Unit,
    onAskAi: (String) -> Unit,
    onBookSwitchNeeded: (bookId: Int, chapter: Int, verse: Int, verses: List<com.verbum.feature.bible.domain.model.Verse>?) -> Unit = { _, _, _, _ -> },
    initialVerses: List<com.verbum.feature.bible.domain.model.Verse>? = null,
    modifier: Modifier = Modifier,
    viewModel: BibleReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Set initial verses if provided
    LaunchedEffect(initialVerses) {
        Timber.d("LaunchedEffect: initialVerses = ${initialVerses?.size ?: "null"}")
        if (initialVerses != null && initialVerses.isNotEmpty()) {
            Timber.d("Calling setTargetVerses with ${initialVerses.size} verses")
            viewModel.setTargetVerses(initialVerses)
        }
    }

    // Set up book switch callback immediately
    LaunchedEffect(Unit) {
        viewModel.setBookSwitchCallback { bookId, chapter, verse ->
            Timber.d("Book switch callback invoked: bookId=$bookId, chapter=$chapter, verse=$verse")
            // Get current target verse locations from viewModel
            val currentUiState = viewModel.uiState.value as? BibleReaderUiState.Loaded
            val verses = currentUiState?.targetVerseLocations?.map { 
                com.verbum.feature.bible.domain.model.Verse(
                    bookId = it.bookId,
                    bookName = it.bookName,
                    chapter = it.chapter,
                    verseNumber = it.verse,
                    text = ""
                )
            } ?: emptyList()
            onBookSwitchNeeded(bookId, chapter, verse, verses)
        }
    }

    BibleReaderContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onVerseClick = viewModel::onVerseSelected,
        onDismissVerseActions = viewModel::onDismissVerseActions,
        onBookmarkClick = viewModel::onToggleBookmark,
        onPreviousChapter = viewModel::onPreviousChapter,
        onNextChapter = viewModel::onNextChapter,
        onChapterSelected = viewModel::onChapterSelected,
        onLoadNextChapter = viewModel::onLoadNextChapter,
        onReadingModeChange = viewModel::onReadingModeChange,
        onThemeChange = viewModel::onThemeChange,
        onToggleChapterNav = viewModel::onToggleChapterNav,
        onToggleSearch = viewModel::onToggleSearch,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearch = viewModel::onSearch,
        onClearSearch = viewModel::onClearSearch,
          onSuggestionClick = viewModel::onSearchSuggestionClick,
        onSearchResultClick = { verse ->
            viewModel.onSearchResultClick(verse)
        },
        onVisibleChapterChange = viewModel::onVisibleChapterChange,
          onVisibleVerseChange = viewModel::onVisibleVerseChange,
          onTargetVerseConsumed = viewModel::onTargetVerseConsumed,
        onAskAi = { verse ->
            onAskAi("${verse.bookName} ${verse.chapter}:${verse.verseNumber}")
        },
        goToPreviousTargetVerse = viewModel::goToPreviousTargetVerse,
        hasPreviousTargetVerse = viewModel::hasPreviousTargetVerse,
        goToNextTargetVerse = viewModel::goToNextTargetVerse,
        hasNextTargetVerse = viewModel::hasNextTargetVerse,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BibleReaderContent(
    uiState: BibleReaderUiState,
    onNavigateBack: () -> Unit,
    onVerseClick: (Verse) -> Unit,
    onDismissVerseActions: () -> Unit,
    onBookmarkClick: (Verse) -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onChapterSelected: (Int) -> Unit,
    onLoadNextChapter: () -> Unit,
    onReadingModeChange: (ReadingMode) -> Unit,
    onThemeChange: (ReadingThemeType) -> Unit,
    onToggleChapterNav: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onSuggestionClick: (Verse) -> Unit,
    onSearchResultClick: (Verse) -> Unit,
    onVisibleChapterChange: (Int) -> Unit,
    onVisibleVerseChange: (chapter: Int, verse: Int) -> Unit,
    onTargetVerseConsumed: () -> Unit,
    onAskAi: (Verse) -> Unit,
    goToPreviousTargetVerse: () -> Unit,
    hasPreviousTargetVerse: () -> Boolean,
    goToNextTargetVerse: () -> Unit,
    hasNextTargetVerse: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is BibleReaderUiState.Loading -> VerbumLoadingIndicator()
        is BibleReaderUiState.Error -> VerbumErrorState(
            message = uiState.message,
            onRetry = {},
        )
        is BibleReaderUiState.Loaded -> {
            val isDark = isSystemInDarkTheme()
            val theme = remember(uiState.themeType, isDark) {
                uiState.themeType.resolve(isDark)
            }

            Box(modifier = modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Toolbar
                    ReadingToolbar(
                        bookName = uiState.bookName,
                        chapter = uiState.chapter,
                        totalChapters = uiState.totalChapters,
                        readingMode = uiState.readingMode,
                        currentThemeType = uiState.themeType,
                        theme = theme,
                        onNavigateBack = onNavigateBack,
                        onPreviousChapter = onPreviousChapter,
                        onNextChapter = onNextChapter,
                        onToggleSearch = onToggleSearch,
                        onToggleChapterNav = onToggleChapterNav,
                        onReadingModeChange = onReadingModeChange,
                        onThemeChange = onThemeChange,
                    )

                    AnimatedVisibility(
                        visible = uiState.showSearch,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        VerseSearchBar(
                            query = uiState.searchQuery,
                            suggestions = uiState.searchSuggestions,
                            onQueryChange = onSearchQueryChange,
                            onSearch = onSearch,
                            onClear = onClearSearch,
                            onSuggestionClick = onSuggestionClick,
                            theme = theme,
                        )
                    }

                    // Reading surface
                    when (uiState.readingMode) {
                        ReadingMode.SCROLL -> {
                            ScrollReadingView(
                                chapters = uiState.chapterBlocks,
                                currentChapter = uiState.chapter,
                                bookName = uiState.bookName,
                                theme = theme,
                                onVerseClick = onVerseClick,
                                onLoadNextChapter = onLoadNextChapter,
                                onVisibleChapterChange = onVisibleChapterChange,
                                  onVisibleVerseChange = onVisibleVerseChange,
                                  targetVerses = uiState.targetVerses,
                                  targetVerseRange = uiState.targetVerseRange,
                                  onTargetVerseConsumed = onTargetVerseConsumed,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        ReadingMode.CODEX -> {
                            CodexReadingView(
                                chapter = uiState.chapter,
                                totalChapters = uiState.totalChapters,
                                verses = uiState.verses,
                                bookName = uiState.bookName,
                                theme = theme,
                                onVerseClick = onVerseClick,
                                onChapterChange = onChapterSelected,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Chapter navigator overlay
                AnimatedVisibility(
                    visible = uiState.showChapterNav,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ChapterNavigator(
                        totalChapters = uiState.totalChapters,
                        currentChapter = uiState.chapter,
                        bookName = uiState.bookName,
                        theme = theme,
                        onChapterSelected = onChapterSelected,
                        onDismiss = onToggleChapterNav,
                    )
                }

                // Search results overlay
                SearchResultsOverlay(
                    results = uiState.searchResults,
                    theme = theme,
                    onVerseClick = onSearchResultClick,
                    onDismiss = onToggleSearch,
                    visible = uiState.searchResults.isNotEmpty(),
                )

                // Floating navigation controls for target verses
                if (uiState.targetVerseLocations.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .padding(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Previous verse button
                        IconButton(
                            onClick = goToPreviousTargetVerse,
                            enabled = hasPreviousTargetVerse(),
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                )
                                .background(
                                    color = theme.toolbarBackground,
                                    shape = CircleShape,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowUp,
                                contentDescription = "Previous target verse",
                                tint = theme.accentColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // Next verse button
                        IconButton(
                            onClick = goToNextTargetVerse,
                            enabled = hasNextTargetVerse(),
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                )
                                .background(
                                    color = theme.toolbarBackground,
                                    shape = CircleShape,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Next target verse",
                                tint = theme.accentColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // Bottom sheet for verse actions
                uiState.selectedVerse?.let { verse ->
                    ModalBottomSheet(
                        onDismissRequest = onDismissVerseActions,
                        sheetState = rememberModalBottomSheetState(),
                        dragHandle = { BottomSheetDefaults.DragHandle() },
                    ) {
                        VerseActionsSheet(
                            verse = verse,
                            crossReferences = uiState.crossReferences,
                            isLoadingCrossReferences = uiState.isLoadingCrossReferences,
                            onBookmark = { onBookmarkClick(verse) },
                            onShare = { /* share intent */ },
                            onAskAi = { onAskAi(verse) },
                            onDismiss = onDismissVerseActions,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseActionsSheet(
    verse: Verse,
    crossReferences: List<BibleCrossReference>,
    isLoadingCrossReferences: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onAskAi: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VerbumSpacing.lg),
    ) {
        Text(
            text = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(VerbumSpacing.sm))
        Text(
            text = verse.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CrimsonTextFamily),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(VerbumSpacing.lg))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onBookmark(); onDismiss() }) {
                Icon(
                    imageVector = if (verse.isBookmarked) Icons.Filled.Bookmark
                    else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                )
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text("Bookmark")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onShare(); onDismiss() }) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text("Share")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onAskAi(); onDismiss() }) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = "Ask AI")
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text("Ask Verbum AI")
            }
        }
        Text(
            text = "Related passages",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        when {
            isLoadingCrossReferences -> Text(
                text = "Loading local references...",
                style = MaterialTheme.typography.bodySmall,
            )
            crossReferences.isEmpty() -> Text(
                text = "No related passages found.",
                style = MaterialTheme.typography.bodySmall,
            )
            else -> crossReferences.forEach { reference ->
                Text(
                    text = "${reference.toBookName} ${reference.toChapter}:${reference.toVerseStart}" +
                        if (reference.toVerseEnd > reference.toVerseStart) "-${reference.toVerseEnd}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = VerbumSpacing.xs),
                )
            }
        }

        Spacer(Modifier.height(VerbumSpacing.lg))
    }
}

@Preview(showBackground = true)
@Composable
private fun BibleReaderPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ReadingThemeType.entries.forEach { themeType ->
                    Text(
                        text = "Reader Theme: ${themeType.displayName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            horizontal = VerbumSpacing.md,
                            vertical = VerbumSpacing.sm
                        ),
                    )
                    BibleReaderContent(
                        uiState = BibleReaderUiState.Loaded(
                            bookName = "John",
                            chapter = 1,
                            totalChapters = 21,
                            verses = listOf(
                                Verse(
                                    50,
                                    "John",
                                    1,
                                    1,
                                    "In the beginning was the Word, and the Word was with God, and the Word was God."
                                ),
                                Verse(50, "John", 1, 2, "He was in the beginning with God."),
                                Verse(
                                    50,
                                    "John",
                                    1,
                                    3,
                                    "All things were made through him, and without him was not any thing made that was made."
                                ),
                            ),
                            chapterBlocks = listOf(
                                ChapterBlock(
                                    chapter = 1,
                                    verses = listOf(
                                        Verse(
                                            50,
                                            "John",
                                            1,
                                            1,
                                            "In the beginning was the Word, and the Word was with God, and the Word was God."
                                        ),
                                        Verse(
                                            50,
                                            "John",
                                            1,
                                            2,
                                            "He was in the beginning with God."
                                        ),
                                        Verse(
                                            50,
                                            "John",
                                            1,
                                            3,
                                            "All things were made through him, and without him was not any thing made that was made."
                                        ),
                                    ),
                                ),
                            ),
                            themeType = themeType,
                            targetVerses = emptySet(),
                            targetVerseRange = null,
                            targetVerseLocations = emptyList(),
                            currentTargetIndex = 0,
                        ),
                        onNavigateBack = {},
                        onVerseClick = {},
                        onDismissVerseActions = {},
                        onBookmarkClick = {},
                        onPreviousChapter = {},
                        onNextChapter = {},
                        onChapterSelected = {},
                        onLoadNextChapter = {},
                        onReadingModeChange = {},
                        onThemeChange = {},
                        onToggleChapterNav = {},
                        onToggleSearch = {},
                        onSearchQueryChange = {},
                        onSearch = {},
                        onClearSearch = {},
                        onSuggestionClick = {},
                        onSearchResultClick = {},
                        onVisibleChapterChange = {},
                        onVisibleVerseChange = { _, _ -> },
                        onTargetVerseConsumed = {},
                        onAskAi = {},
                        goToPreviousTargetVerse = {},
                        hasPreviousTargetVerse = { false },
                        goToNextTargetVerse = {},
                        hasNextTargetVerse = { false },
                    )
                }
            }
        }
    }
}
